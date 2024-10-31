package com.sonsofcrypto.web3walletcore.modules.degenCultProposals

import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3walletcore.services.cult.CultService
import com.sonsofcrypto.web3walletcore.services.cult.CultServiceResponse

interface CultProposalsInteractor {
    @Throws(Throwable::class)
    suspend fun fetch(): CultServiceResponse
    val hasCultBalance: Boolean
    fun isVoidSigner(): Boolean
}

class DefaultCultProposalsInteractor(
    private val cultService: CultService,
    private val walletService: WalletService,
): CultProposalsInteractor {

    override suspend fun fetch(): CultServiceResponse = cultService.fetch()

    override val hasCultBalance: Boolean get() {
        val balance = walletService.balance(Network.ethereum(), Currency.cult())
        return balance.isGreaterThan(BigInt.zero)
    }

    override fun isVoidSigner(): Boolean =
        walletService.isSelectedVoidSigner()
}