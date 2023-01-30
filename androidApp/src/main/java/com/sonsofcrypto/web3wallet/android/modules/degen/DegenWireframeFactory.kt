package com.sonsofcrypto.web3wallet.android.modules.degen

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframe
import com.sonsofcrypto.web3walletcore.services.degen.DegenService

interface DegenWireframeFactory {
    fun make(parent: Fragment?): DegenWireframe
}

class DefaultDegenWireframeFactory(
    private val degenService: DegenService,
    private val networksService: NetworksService,
): DegenWireframeFactory {

    override fun make(parent: Fragment?): DegenWireframe = DegenWireframe(
        parent,
        degenService,
        networksService
    )
}

class DegenWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("DegenWireframeFactory", AssemblerRegistryScope.INSTANCE) {
            DefaultDegenWireframeFactory(
                it.resolve("DegenService"),
                it.resolve("NetworksService"),
            )
        }
    }
}