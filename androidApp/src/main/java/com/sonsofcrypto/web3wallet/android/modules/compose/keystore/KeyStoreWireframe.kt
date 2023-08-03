package com.sonsofcrypto.web3wallet.android.modules.compose.keystore

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.keyStore.KeyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.assembler
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.compose.dashboard.DashboardWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.mnemonicimport.MnemonicImportWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.mnemonicnew.MnemonicNewWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.mnemonicupdate.MnemonicUpdateWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.keyStore.DefaultKeyStoreInteractor
import com.sonsofcrypto.web3walletcore.modules.keyStore.DefaultKeyStorePresenter
import com.sonsofcrypto.web3walletcore.modules.keyStore.KeyStoreWireframe
import com.sonsofcrypto.web3walletcore.modules.keyStore.KeyStoreWireframeDestination
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportWireframeContext
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewWireframeContext
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeContext
import smartadapter.internal.extension.name

class DefaultKeyStoreWireframe(
    private val parent: WeakRef<Fragment>?,
    private val keyStoreService: KeyStoreService,
    private val networksService: NetworksService,
    private val mnemonicNewWireframeFactory: MnemonicNewWireframeFactory,
    private val mnemonicImportWireframeFactory: MnemonicImportWireframeFactory,
    private val mnemonicUpdateWireframeFactory: MnemonicUpdateWireframeFactory,
): KeyStoreWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        parent?.get()?.childFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.commit()
//            ?.apply {
//            add(R.id.container, fragment)
//            commitNow()
//        }
    }

    override fun navigate(destination: KeyStoreWireframeDestination) {
        when (destination) {
            is KeyStoreWireframeDestination.NewMnemonic -> {
                val context = MnemonicNewWireframeContext(destination.handler)
                mnemonicNewWireframeFactory.make(fragment?.get(), context).present()
            }
            is KeyStoreWireframeDestination.ImportMnemonic -> {
                val context = MnemonicImportWireframeContext(destination.handler)
                mnemonicImportWireframeFactory.make(fragment?.get(), context).present()
            }
            is KeyStoreWireframeDestination.EditKeyStoreItem -> {
                val context = MnemonicUpdateWireframeContext(
                    destination.item, destination.handler, destination.onDeleted
                )
                mnemonicUpdateWireframeFactory.make(fragment?.get(), context).present()
            }
            is KeyStoreWireframeDestination.Networks -> {
                val factory: DashboardWireframeFactory = assembler.resolve(
                    DashboardWireframeFactory::class.name
                )
                factory.make(fragment?.get()).present()
            }
            else -> { println("[AA] handle event $destination") }
        }
    }

    private fun wireUp(): Fragment {
        val view = KeyStoreFragment()
        val interactor = DefaultKeyStoreInteractor(
            keyStoreService,
            networksService,
        )
        val presenter = DefaultKeyStorePresenter(
            WeakRef(view),
            this,
            interactor,
        )
        view.presenter = presenter
        return NavigationFragment(view)
    }
}