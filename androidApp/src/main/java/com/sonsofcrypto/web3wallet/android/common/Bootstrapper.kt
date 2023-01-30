package com.sonsofcrypto.web3wallet.android.common

import androidx.appcompat.app.AppCompatActivity
import com.sonsofcrypto.web3wallet.android.assembler
import com.sonsofcrypto.web3wallet.android.modules.dashboard.DashboardWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.degen.DegenWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.root.RootWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.root.RootWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.services.*
import com.sonsofcrypto.web3wallet.android.services.keychain.KeyChainServiceAssembler

interface Bootstrapper {
    fun boot()
}

class MainBootstrapper(
    private val parent: AppCompatActivity?,
): Bootstrapper {

    override fun boot() {
        listOf(
            AssemblerBootstrapper(),
            UIBootstrapper(parent)
        ).forEach { it.boot() }
    }
}

private class AssemblerBootstrapper: Bootstrapper {

    override fun boot() {

        assembler.configure(
            listOf(
                // Services
                KeyStoreServiceAssembler(),
                KeyChainServiceAssembler(),
                ActionsServiceAssembler(),
                CurrencyStoreServiceAssembler(),
                NetworksServiceAssembler(),
                NFTsServiceAssembler(),
                WalletServiceAssembler(),
                NodeServiceAssembler(),
                DegenServiceAssembler(),
                // Modules
                RootWireframeFactoryAssembler(),
                DashboardWireframeFactoryAssembler(),
                DegenWireframeFactoryAssembler(),
            )
        )
    }
}

private class UIBootstrapper(
    private val parent: AppCompatActivity?,
): Bootstrapper {

    override fun boot() {
        val rootWireframeFactory: RootWireframeFactory = assembler.resolve("RootWireframeFactory")
        rootWireframeFactory.make(parent).present()
    }
}