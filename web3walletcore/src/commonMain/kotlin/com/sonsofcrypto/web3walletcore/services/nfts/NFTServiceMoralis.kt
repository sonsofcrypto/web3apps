package com.sonsofcrypto.web3walletcore.services.nfts

import com.sonsofcrypto.web3lib.BuildKonfig
import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.extensions.stdJson
import com.sonsofcrypto.web3lib.utils.withBgCxt
import com.sonsofcrypto.web3lib.utils.withUICxt
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class NFTServiceMoralis(
    private val networksService: NetworksService,
    private val store: KeyValueStore,
): NFTsService {
    private var listeners: MutableList<NFTsServiceListener> = mutableListOf()
    private val client: HttpClient by lazy {
        HttpClient {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) { json(
                stdJson, ContentType.Application.Json.withCharset(
                    Charsets.UTF_8)) }
        }
    }

    /** Fetch NFTs for the selected wallet & network and cache them in memory */
    @Throws(Throwable::class)
    override suspend fun fetchNFTs(): List<NFTItem> {
        val walletAddress =  networksService.wallet()?.address()
        val network = networksService.network ?: return emptyList()
        val address = walletAddress?.toHexStringAddress()?.hexString
            ?: return emptyList()
        val allResps = mutableListOf<Response?>()
        try {
            var resp = fetch(address, network, null)
            var cursor: String? = resp?.cursor
            allResps.add(resp)
            while (cursor != null) {
                resp = fetch(address, network, cursor)
                cursor = resp?.cursor
                allResps.add(resp)
            }
        } catch (err: Throwable) {
            println("[NFTServiceMoralis] error: $err")
        }
        return withBgCxt {
            val results = allResps
                .filterNotNull()
                .map { it.result }
                .filterNotNull()
                .flatten()
            val normalized = transformToStantardNFTs(results)
            return@withBgCxt withUICxt {
                storeNFTs(updateMemPoolStatus(normalized.second))
                storeCollections(normalized.first)
                broadcastNFTsChanged()
                return@withUICxt nfts()
            }
        }
    }

    /** Find a collection that matches the given identifier (from memory).
     *  We use the data fetched by the latest request to fetchNFTs. */
    override fun collection(id: String): NFTCollection {
        return collections().first { it.identifier == id }
    }
    /** Find a nft that matches the given identifier (from memory).
     *  We use the data fetched by the latest request to fetchNFTs. */
    override fun nft(collectionId: String, tokenId: String): NFTItem {
        return nfts().first {
            it.address == collectionId && it.identifier == tokenId
        }
    }
    /** List of unique collections for your nfts. */
    override fun collections(): List<NFTCollection> {
        return jsonDecode(store[collectionsKey] ?: "[]") ?: emptyList()
    }
    /** List of nfts. */
    override fun nfts(): List<NFTItem> {
        return jsonDecode(store[nftsKey] ?: "[]") ?: emptyList()
    }
    /** List of nfts for a given collection id. */
    override fun nfts(collectionId: String): List<NFTItem> {
        return nfts().filter { it.address == collectionId }
    }
    /** To be called when a transaction for sending an nft is successful. This
     * is so we can flag that NFT as pending confirmation so we hide it or flag
     * it in your NFTs list */
    override fun nftSent(collectionId: String, tokenId: String) {
        val mempoolNfts = nftIdsInMemPool().toMutableList()
        mempoolNfts.add("$collectionId/$tokenId")
        storeNftIdsInMemPool(mempoolNfts)
        broadcastNFTsChanged()
    }
    /** Add a listener to the service */
    override fun addListener(listener: NFTsServiceListener) {
        listeners.add(listener)
    }
    /** Removes a listener to the service */
    override fun removeListener(listener: NFTsServiceListener) {
        listeners.remove(listener)
    }

    @Throws(Throwable::class)
    private suspend fun fetch(
        address: AddressHexString,
        network: Network,
        cursor: String?
    ): Response? {
        if (network != Network.ethereum())
            return null // NOTE: Only support ETH at the moment
        val chain: String = "eth"
        val body = client.get {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-API-KEY", BuildKonfig.moralisKey)
            }
            url {
                protocol = URLProtocol.HTTPS
                host = "deep-index.moralis.io"
                path("api/v2.2/$address/nft")
                parameters.append("chain", chain)
                parameters.append("format", "decimal")
                parameters.append("exclude_spam", "true")
                parameters.append("normalizeMetadata", "true")
                parameters.append("media_items", "true")
                if (cursor != null)
                    parameters.append("cursor", cursor)
                else Unit
            }
        }.bodyAsText()
        return jsonDecode<Response>(body)
    }

    private fun transformToStantardNFTs(
        results: List<Response.Nft>
    ): Pair<List<NFTCollection>, List<NFTItem>> {
        val nfts: List<NFTItem?> = results.map {
            val tokenId = it.tokenId
            val tokenAddress = it.tokenAddress
            val image = it.normalized?.image
            if (tokenId == null || tokenAddress == null || image == null)
                null
            else
                NFTItem(
                    identifier = "$tokenId",
                    collectionIdentifier = tokenAddress,
                    name = it.normalized?.name ?: it.name ?: it.tokenId,
                    properties = it.normalized?.attributes?.map { attr ->
                        NFTItem.Property(
                            name = attr?.traitType ?: "",
                            value = attr?.value ?: "",
                            info = "",
                        )
                    } ?: emptyList(),
                    imageUrl = image,
                    previewImageUrl = it.media?.collection?.get("high")?.url,
                    mimeType = it.media?.mimetype,
                    address = tokenAddress,
                    schemaName = it.contractType ?: "",
                    tokenId = BigInt.from(tokenId, 10),
                    inMemPool = false,
                )
        }
        val colections: List<NFTCollection?> = results
            .distinctBy { it.tokenAddress }
            .map {
            val tokenId = it.tokenId
            val tokenAddress = it.tokenAddress
            val img = it.normalized?.image
            if (tokenId == null || tokenAddress == null || img == null)
                null
            else
                NFTCollection(
                    identifier = tokenAddress,
                    coverImage = it.media?.collection?.get("high")?.url ?: img,
                    title = it.name ?: it.normalized.name ?: tokenAddress,
                    author = null,
                    isVerifiedAccount = it.normalized.verified ?: false ,
                    authorImage = null,
                    description = it.normalized.description
                        ?: "${it.normalized.name} ${it.tokenAddress}",
                )
        }
        return Pair(colections.filterNotNull(), nfts.filterNotNull())
    }

    fun testProcessing(body: String): Pair<List<NFTCollection>, List<NFTItem>> {
        val resp = jsonDecode<Response>(body)
        return transformToStantardNFTs(resp?.result ?: emptyList())
    }

    @Serializable
    private data class Response(
        val page: Int?,
        @SerialName("page_size")
        val pageSize: Int?,
        val cursor: String?,
        val result: List<Response.Nft>?
    ) {
        @Serializable
        data class Nft(
            @SerialName("token_address")
            val tokenAddress: String?,
            @SerialName("token_id")
            val tokenId: String?,
            @SerialName("token_hash")
            val tokenHash: String?,
            val amount: String?,
            @SerialName("contract_type")
            val contractType: String?,
            val name: String?,
            val symbol: String?,
            @SerialName("token_uri")
            val tokenURI: String?,
            @SerialName("normalized_metadata")
            val normalized: NormalizedMetadata?,
            val media: Media?,
        ) {
            @Serializable
            data class NormalizedMetadata(
                val name: String?,
                val description: String?,
                val image: String?,
                val attributes: List<Attribute>?,
                @SerialName("verified_collection")
                val verified: Boolean?
            ) {
                @Serializable
                data class Attribute(
                    @SerialName("trait_type")
                    val traitType: String?,
                    val value: String?,
                )
            }
            @Serializable
            data class Media(
                val mimetype: String?,
                val status: String?,
                @SerialName("media_collection")
                val collection: Map<String, Item>?,
            ) {
                @Serializable
                data class Item(
                    val url: String
                )
            }
        }
    }

    /** We update the NFTItem.broadcasting property in case we have sent a
     * transaction that is pending to be accepted by the mempool */
    private fun updateMemPoolStatus(nfts: List<NFTItem>): List<NFTItem> {
        val currentNftIdsInMemPool = nftIdsInMemPool()
        val updatedNFTs = mutableListOf<NFTItem>()
        val updatedNftIdsInMemPool = mutableListOf<String>()
        nfts.forEach {
            val memPoolId = "${it.address}/${it.tokenId}"
            if (currentNftIdsInMemPool.contains(memPoolId)) {
                it.inMemPool = true
                updatedNftIdsInMemPool.add(memPoolId)
            }
            updatedNFTs.add(it)
        }
        storeNftIdsInMemPool(updatedNftIdsInMemPool)
        return updatedNFTs
    }

    private fun storeNFTs(nfts: List<NFTItem>) {
        store[nftsKey] = jsonEncode(nfts)
    }

    private val nftsKey: String
        get() = "web3wallet.core.$addressKey.$networkKey.nfts"

    private fun storeCollections(collection: List<NFTCollection>) {
        store[collectionsKey] = jsonEncode(collection)
    }

    private val collectionsKey: String
        get() = "web3wallet.core.$addressKey.$networkKey.collections"

    private fun nftIdsInMemPool(): List<String> =
        jsonDecode(store[nftIdsInMemPoolKey] ?: "[]") ?: emptyList()

    private fun storeNftIdsInMemPool(nfts: List<String>) {
        store[nftIdsInMemPoolKey] = jsonEncode(nfts)
    }

    private val nftIdsInMemPoolKey: String get() =
        "web3wallet.core.$addressKey.$networkKey.nfts.memPool"

    private val addressKey: String get() =
        networksService.wallet()?.address()?.toHexStringAddress()?.hexString ?: "-"

    private val networkKey: String get() = networksService.network?.id() ?: "-"

    private fun broadcastNFTsChanged() =
        listeners.forEach { it.nftsChanged() }
}
