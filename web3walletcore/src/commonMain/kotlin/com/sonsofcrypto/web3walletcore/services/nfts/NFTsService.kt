package com.sonsofcrypto.web3walletcore.services.nfts

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.types.bignum.BigIntSerializer
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.extensions.stdJson
import com.sonsofcrypto.web3walletcore.extensions.Localized
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface NFTsServiceListener {
    /** This will be called each time the list of NFTs changes. */
    fun nftsChanged()
}

interface NFTsService {
    /** Fetch NFTs for the selected wallet & network and cache them in memory */
    @Throws(Throwable::class)
    suspend fun fetchNFTs(): List<NFTItem>
    /** Find a collection that matches the given identifier (from memory).
     *  We use the data fetched by the latest request to fetchNFTs. */
    fun collection(id: String): NFTCollection
    /** Find a nft that matches the given identifier (from memory).
     *  We use the data fetched by the latest request to fetchNFTs. */
    fun nft(collectionId: String, tokenId: String): NFTItem
    /** List of unique collections for your nfts. */
    fun collections(): List<NFTCollection>
    /** List of nfts. */
    fun nfts(): List<NFTItem>
    /** List of nfts for a given collection id. */
    fun nfts(collectionId: String): List<NFTItem>
    /** To be called when a transaction for sending an nft is successful. This
     * is so we can flag that NFT as pending confirmation so we hide it or flag
     * it in your NFTs list */
    fun nftSent(identifier: String, tokenId: String)
    /** Add a listener to the service */
    fun addListener(listener: NFTsServiceListener)
    /** Removes a listener to the service */
    fun removeListener(listener: NFTsServiceListener)
}

class OpenSeaNFTsService(
    private val networksService: NetworksService,
    private val store: KeyValueStore,
): NFTsService {
    private var listeners: MutableList<NFTsServiceListener> = mutableListOf()
    private val client: HttpClient by lazy {
        HttpClient {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) { json(stdJson, Json.withCharset(Charsets.UTF_8)) }
        }
    }

    override suspend fun fetchNFTs(): List<NFTItem> {
        val address = networksService.wallet()?.address()?.toHexStringAddress()?.hexString
            ?: return emptyList()
        val network = networksService.network ?: return emptyList()
        // NOTE: At this stage we only support Ethereum network
        if (network.id() != Network.ethereum().id()) {
            storeNFTs(emptyList())
            broadcastNFTsChanged()
            return emptyList()
        }
        return try {
            val body = client.get {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                    append("X-API-KEY", "")
                }
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.opensea.io"
                    path("api/v1/assets")
                    parameters.append("format", "json")
                    parameters.append("owner", address)
                }
            }.bodyAsText()
            val sanitizedBody = if (!body.contains("assets:")) "{assets: []}" else body
            val assets = jsonDecode<AssetList>(sanitizedBody)?.assets ?: emptyList()
            storeNFTs(updateMemPoolStatus(nftItemsFrom(assets)))
            storeCollections(nftCollections(assets))
            broadcastNFTsChanged()
            nfts()
        } catch (err: Throwable) {
            broadcastNFTsChanged()
            nfts().ifEmpty { throw err }
        }
    }

    override fun collection(id: String): NFTCollection =
        collections().first { it.identifier == id }

    override fun nft(collectionId: String, tokenId: String): NFTItem {
        return nfts().first {
            it.address == collectionId && it.identifier == tokenId
        }
    }

    override fun collections(): List<NFTCollection> =
        jsonDecode(store[collectionsKey] ?: "[]") ?: emptyList()

    override fun nfts(): List<NFTItem> = jsonDecode(store[nftsKey] ?: "[]") ?: emptyList()

    override fun nfts(collectionId: String): List<NFTItem> =
        nfts().filter { it.collectionIdentifier == collectionId }

    override fun nftSent(collectionId: String, tokenId: String) {
        val nfts = nftIdsInMemPool().toMutableList()
        nfts.add("$collectionId/$tokenId")
        storeNftIdsInMemPool(nfts)
        broadcastNFTsChanged()
    }

    override fun addListener(listener: NFTsServiceListener) { listeners.add(listener) }

    override fun removeListener(listener: NFTsServiceListener) { listeners.remove(listener) }

    private fun nftItemsFrom(assets: List<Asset>): List<NFTItem> =
        assets.map { asset ->
            NFTItem(
                asset.id.toString(),
                asset.collection.slug,
                asset.name ?: "",
                asset.traits.map { NFTItem.Property(it.traitType, it.value, "") },
                asset.imageUrl ?: "",
                null,
                null,
                asset.assetContract.address,
                asset.assetContract.schemaName,
                asset.tokenId,
                false
            )
        }

    private fun nftCollections(assets: List<Asset>): List<NFTCollection> {
        val collections = mutableListOf<NFTCollection>()
        val nftIdsPendingSent = nftIdsInMemPool()
        val assetsFiltered = assets.filter { !nftIdsPendingSent.contains(it.id.toString()) }
        for (asset in assetsFiltered) {
            if (collections.firstOrNull { it.identifier == asset.collection.slug } != null) {
                continue
            }
            collections.add(
                NFTCollection(
                    asset.collection.slug,
                    asset.collection.imageUrl ?: "",
                    asset.collection.name ?: "",
                    asset.collection.slug,
                    false,
                    "",
                    asset.collection.description ?: Localized(
                        "nft.detail.section.title.description.empty"
                    )
                )
            )
        }
        return collections
    }

    /** We update the NFTItem.broadcasting property in case we have sent a transaction that is
     *  pending to be accepted by the mempool */
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

    private fun storeNFTs(n: List<NFTItem>) { store[nftsKey] = jsonEncode(n) }
    private val nftsKey: String get() = "web3wallet.core.$addressKey.$networkKey.nfts"

    private fun storeCollections(c: List<NFTCollection>) { store[collectionsKey] = jsonEncode(c) }
    private val collectionsKey: String get() = "web3wallet.core.$addressKey.$networkKey.collections"

    private fun nftIdsInMemPool(): List<String> =
        jsonDecode(store[nftIdsInMemPoolKey] ?: "[]") ?: emptyList()
    private fun storeNftIdsInMemPool(s: List<String>) { store[nftIdsInMemPoolKey] = jsonEncode(s) }
    private val nftIdsInMemPoolKey: String get() =
        "web3wallet.core.$addressKey.$networkKey.nfts.memPool"

    private val addressKey: String get() =
        networksService.wallet()?.address()?.toHexStringAddress()?.hexString ?: "-"

    private val networkKey: String get() = networksService.network?.id() ?: "-"

    private fun broadcastNFTsChanged() =
        listeners.forEach { it.nftsChanged() }
}

@Serializable
private data class AssetList(
    val assets: List<Asset>,
)

@Serializable
private data class Asset(
    val id: Int,
    val name: String?,
    val description: String?,
    @SerialName("image_url")
    val imageUrl: String?,
    @SerialName("asset_contract")
    val assetContract: AssetContract,
    val collection: Collection,
    val traits: List<Trait>,
    @SerialName("token_id") @Serializable(with = BigIntSerializer::class)
    val tokenId: BigInt,
)

@Serializable
private data class AssetContract(
    val name: String?,
    val address: String,
    @SerialName("asset_contract_type")
    val assetContractType: String,
    @SerialName("schema_name")
    val schemaName : String,
)

@Serializable
private data class Collection(
    val slug: String, // id
    val name: String?,
    @SerialName("image_url")
    val imageUrl: String?,
    val description: String?,
)

@Serializable
private data class Trait(
    @SerialName("trait_type")
    val traitType: String,
    val value: String,
)