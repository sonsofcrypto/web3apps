package com.sonsofcrypto.web3wallet.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sonsofcrypto.web3lib_bip39.*
import com.sonsofcrypto.web3lib_crypto.*
import android.widget.TextView

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        Crypto.setProvider(AndroidCryptoPrimitivesProvider())
        tv.text = greet() + String(Crypto.secureRand(128))

        TmpTest().runAll()
    }
}
