package com.sonsofcrypto.web3wallet.android.modules.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardPresenter
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardView
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard), DashboardView {

    lateinit var presenter: DashboardPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.present()
    }

    override fun update(viewModel: DashboardViewModel) {
        print("render DashboardViewModel")
    }
}