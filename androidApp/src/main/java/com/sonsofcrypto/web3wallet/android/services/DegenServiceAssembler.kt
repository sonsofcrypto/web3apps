package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.services.degen.DefaultDegenService

class DegenServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("DegenService", AssemblerRegistryScope.SINGLETON) {
            DefaultDegenService(
                it.resolve("WalletService")
            )
        }
    }
}