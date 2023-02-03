package com.sonsofcrypto.web3wallet.android

import android.content.Context
import android.os.Bundle
import com.sonsofcrypto.web3lib.utils.BundledAssetProviderApplication
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3walletcore.extensions.App

val assembler: Assembler = DefaultAssembler()
lateinit var appContext: Context

class MainActivity : App() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContext = this

        BundledAssetProviderApplication.setInstance(application)

        AppTheme.value = themeMiamiSunriseLight//themeMiamiSunriseDark

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            MainBootstrapper(this).boot()
        }

        //val tv: TextView = findViewById(R.id.text_view)
        //tv.text = secureRand(128).toString()
    }

    override fun onResume() {
        super.onResume()
//        Bip39Test().runAll()
//        Bip44Test().runAll()
//        KeyValueStoreTest().runAll()
//        KeyStoreTest().runAll()
//        TrieTest().runAll()
//        ProviderTest().runAll()
//        CoinGeckoTest(this.applicationContext).runAll()
//        Web3ServiceTests().runAll()
//        CurrenciesServiceTests().runAll()
//        WalletStateTest().runAll()
//        CurrencyFormatterTest().runAll()
//        CurrencyStoreServiceTest().runAll()
//        WalletServiceTest().runAll()
//        UniswapTests().runAll()
//        EncryptTest().runAll()
//        WalletCoreTests().runAll()
        println("=== all tests executed ===")
    }
}
