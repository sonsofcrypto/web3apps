package com.sonsofcrypto.web3wallet.android.common

data class NavigationItem(
    val title: String,
    val rightBarButton: BarButton
) {
    data class BarButton(
        val title: String,
        val lister: (BarButton)->Void
    )
}

interface NavigationItemProtocol {
    fun navigation(): NavigationItem?
}