package com.sonsofcrypto.web3walletcore.services.degen

import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.BRIDGE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.CULT
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.DERIVATIVE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.GOVERNANCE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.LAND_BORROW
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.MIXER
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.STAKE_YIELD
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.SWAP

interface DegenService {
    fun categoriesActive(): List<DAppCategory>
    fun categoriesInactive(): List<DAppCategory>
}

class DefaultDegenService(
    private val walletService: WalletService,
): DegenService {

    override fun categoriesActive(): List<DAppCategory> {
        val selectedNetwork = walletService.selectedNetwork() ?: return listOf(SWAP)
        if (selectedNetwork != Network.ethereum()) return listOf(SWAP)
        return listOf(SWAP, CULT)
    }

    override fun categoriesInactive(): List<DAppCategory> =
        listOf(STAKE_YIELD, LAND_BORROW, DERIVATIVE, BRIDGE, MIXER, GOVERNANCE)
}