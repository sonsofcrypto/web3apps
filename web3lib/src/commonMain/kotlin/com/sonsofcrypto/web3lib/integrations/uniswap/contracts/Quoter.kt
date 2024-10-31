package com.sonsofcrypto.web3lib.integrations.uniswap.contracts

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.legacy.LegacyContract
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiEncode
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256

class Quoter(address: Address.HexStr): LegacyContract(address) {

    /**
     * /// @inheritdoc IQuoter
     * function quoteExactInputSingle(
     *     address tokenIn,
     *     address tokenOut,
     *     uint24 fee,
     *     uint256 amountIn,
     *     uint160 sqrtPriceLimitX96
     * ) public override returns (uint256 amountOut)
     */

    fun quoteExactInputSingle(
        tokenIn: Address.HexStr,
        tokenOut: Address.HexStr,
        fee: UInt,
        amountIn: BigInt,
        sqrtPriceLimitX96: UInt,
    ):DataHexStr = DataHexStr(
        keccak256("quoteExactInputSingle(address,address,uint24,uint256,uint160)"
            .encodeToByteArray()).copyOfRange(0, 4) +
        abiEncode(tokenIn) +
        abiEncode(tokenOut) +
        abiEncode(fee) +
        abiEncode(amountIn) +
        abiEncode(sqrtPriceLimitX96)
    )
}