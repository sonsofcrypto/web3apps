package com.sonsofcrypto.web3wallet.android.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.sonsofcrypto.web3wallet.android.R

class NavigationFragment(
    private val initialFragment: Fragment?
) : Fragment(R.layout.fragment_navigation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (initialFragment != null)
            push(initialFragment, false)
    }

    fun push(fragment: Fragment, animated: Boolean = true) {
        // val container = view?.findViewById<FrameLayout>(R.id.container)
        childFragmentManager.beginTransaction().apply {
            if (animated) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
            replace(R.id.container, fragment)
            //addToBackStack(null)
            commitNow()
        }
    }

    fun pop() {
        //childFragmentManager.popBackStack()
    }

    fun popToRoot() {
        //TODO("Implement")
    }

    fun presentModal(fragment: Fragment, animated: Boolean) {
        TODO("Implement")
    }
}

fun Fragment.navigationFragment(): NavigationFragment? {
    TODO("Implement")
}