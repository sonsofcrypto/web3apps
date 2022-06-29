package com.sonsofcrypto.web3wallet.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sonsofcrypto.web3lib_bip39.*
import com.sonsofcrypto.web3lib_crypto.*
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        Crypto.setProvider(AndroidCryptoPrimitivesProvider())
        tv.text = Crypto.secureRand(128).toString()

        Bip39Test().runAll()
        Bip44Test().runAll()
    }
}
