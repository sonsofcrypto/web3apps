package com.sonsofcrypto.web3wallet.android.modules.root

import androidx.appcompat.app.AppCompatActivity
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3wallet.android.modules.dashboard.DashboardWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframe

interface RootWireframeFactory {
    fun make(parent: AppCompatActivity?): RootWireframe
}

class DefaultRootWireframeFactory(
    private var dashboardWireframeFactory: DashboardWireframeFactory,
): RootWireframeFactory {

    override fun make(parent: AppCompatActivity?): RootWireframe = DefaultRootWireframe(
        parent,
        dashboardWireframeFactory,
    )
}

class RootAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {
        to.register("RootWireframeFactory", AssemblerRegistryScope.INSTANCE) {
            DefaultRootWireframeFactory(
                it.resolve("DashboardWireframeFactory")
            )
        }
    }
}