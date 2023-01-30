package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class CurrencyStoreServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("CurrencyStoreService", AssemblerRegistryScope.SINGLETON) {
            DefaultCurrencyStoreService(
                DefaultCoinGeckoService(),
                KeyValueStore("CurrencyStoreService.Market"),
                KeyValueStore("CurrencyStoreService.Candle"),
                KeyValueStore("CurrencyStoreService.Metadata"),
                KeyValueStore("CurrencyStoreService.UserCurrency"),
            )
            // TODO: Review loading Cache...should not be done by the assembler
//            service.loadCaches(
//                NetworksService.Companion.supportedNetworks(),
//            )
            // return service
        }
    }
}