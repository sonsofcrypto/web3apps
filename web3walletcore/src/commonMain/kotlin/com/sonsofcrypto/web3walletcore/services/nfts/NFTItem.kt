package com.sonsofcrypto.web3walletcore.services.nfts

import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.types.bignum.BigIntSerializer
import kotlinx.serialization.Serializable

@Serializable
data class NFTItem(
    /** Unique NFT identifier **/
    val identifier: String,
    /** Unique Collection identifier **/
    val collectionIdentifier: String,
    /** NFT name **/
    val name: String,
    /** List of properties **/
    val properties: List<Property>,
    /** URL pointing to the NFT image **/
    val imageUrl: String,
    /** preview image url if available (also used as fallback)*/
    val previewImageUrl: String?,
    /** mime type of asset at `imageUrl` */
    val mimeType: String?,
    /** Contract address of the NFT **/
    val address: String,
    /** Schema name **/
    val schemaName: String,
    /** Token id **/
    @Serializable(with = BigIntSerializer::class)
    val tokenId: BigInt,
    /** True if the NFT is known to be in the mempool pending for a transaction to be broadcasted */
    var inMemPool: Boolean,
) {

    val gatewayImageUrl: String
        get() = ipfsToGateway(imageUrl)

    val gatewayPreviewImageUrl: String?
        get() = if (previewImageUrl != null) ipfsToGateway(previewImageUrl)
                else null

    val fallbackText: String
        get() = when {
            name.contains(" #") -> name.replace(" #", "\n#")
            else -> name
        }

    private fun ipfsToGateway(url: String): String =
        if (!url.contains("ipfs://")) url
        else url.replace("ipfs://", "https://ipfs.io/ipfs/")

    @Serializable
    data class Property(
        /** Name **/
        val name: String,
        /** Value **/
        val value : String,
        /** Info **/
        val info: String,
    )
}
