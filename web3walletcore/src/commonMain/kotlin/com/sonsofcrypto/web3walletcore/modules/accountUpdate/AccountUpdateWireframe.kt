package com.sonsofcrypto.web3walletcore.modules.accountUpdate

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext

data class AccountUpdateWireframeContext(
    val signerStoreItem: SignerStoreItem,
    val updateHandler: ((SignerStoreItem) -> Unit),
    val deleteHandler: (() -> Unit),
)

sealed class AccountUpdateWireframeDestination {
    data class Authenticate(val context: AuthenticateWireframeContext): AccountUpdateWireframeDestination()
    object Dismiss: AccountUpdateWireframeDestination()
}

interface AccountUpdateWireframe {
    fun present()
    fun navigate(destination: AccountUpdateWireframeDestination)
}