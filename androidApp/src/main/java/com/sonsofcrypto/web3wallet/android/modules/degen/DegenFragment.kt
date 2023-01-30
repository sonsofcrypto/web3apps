package com.sonsofcrypto.web3wallet.android.modules.degen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.common.navigationFragment
import com.sonsofcrypto.web3walletcore.modules.degen.DegenPresenter
import com.sonsofcrypto.web3walletcore.modules.degen.DegenView
import com.sonsofcrypto.web3walletcore.modules.degen.DegenViewModel

class DegenFragment : Fragment(R.layout.fragment_degen), DegenView {

    lateinit var presenter: DegenPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.present()
    }

    override fun update(viewModel: DegenViewModel) {

    }

    override fun popToRootAndRefresh() {
        navigationFragment()?.popToRoot()
        presenter.present()
    }
}