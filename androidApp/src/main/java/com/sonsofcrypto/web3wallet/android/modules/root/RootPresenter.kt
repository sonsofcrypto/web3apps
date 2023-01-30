package com.sonsofcrypto.web3wallet.android.modules.root

import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.modules.root.RootView
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframe
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframeDestination

interface RootPresenter {
    fun present()
}

class DefaultRootPresenter(
    var view: WeakRef<RootView>,
    var wireframe: RootWireframe,
): RootPresenter {

    override fun present() {
        wireframe.navigate(RootWireframeDestination.DASHBOARD)
    }
}