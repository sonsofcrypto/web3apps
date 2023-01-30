package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.wallet.DefaultWalletService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class WalletServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("WalletService", AssemblerRegistryScope.SINGLETON) {
            DefaultWalletService(
                it.resolve("NetworksService"),
                it.resolve("CurrencyStoreService"),
                KeyValueStore("WalletService.currencies"),
                KeyValueStore("WalletService.networksState"),
                KeyValueStore("WalletService.transferLogsCache"),
            )
        }
    }
}