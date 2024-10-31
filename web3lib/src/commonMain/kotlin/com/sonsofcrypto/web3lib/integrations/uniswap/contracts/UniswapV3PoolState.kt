package com.sonsofcrypto.web3lib.integrations.uniswap.contracts

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.legacy.LegacyContract
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiDecodeBigInt
import com.sonsofcrypto.web3lib.legacy.abiDecodeUInt
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256

class UniswapV3PoolState(address: Address.HexStr): LegacyContract(address) {
    /**
     * @notice The 0th storage slot in the pool stores many values, and is
     * exposed as a single method to save gas when accessed externally.
     * @return `sqrtPriceX96` The current price of the pool as a sqrt(token1/token0)
     * Q64.96 value
     * `tick` The current tick of the pool, i.e. according to the last tick
     * transition that was run. This value may not always be equal to
     * SqrtTickMath.getTickAtSqrtRatio(sqrtPriceX96) if the price is on a tick
     * boundary.
     * `observationIndex` The index of the last oracle observation that was
     * written
     * `observationCardinality` The current maximum number of observations
     * stored in the pool,
     * `observationCardinalityNext` The next maximum number of observations, to
     * be updated when the observation.
     * `feeProtocol` The protocol fee for both tokens of the pool. Encoded as
     * two 4 bit values, where the protocol fee of token1 is shifted 4 bits and
     * the protocol fee of token0is the lower 4 bits. Used as the denominator of
     * a fraction of the swap fee, e.g. 4 means 1/4th of the swap fee.
     * `unlocked` Whether the pool is currently locked to reentrancy
     *
     * function slot0() external view returns (
     *     uint160 sqrtPriceX96,
     *     int24 tick,
     *     uint16 observationIndex,
     *     uint16 observationCardinality,
     *     uint16 observationCardinalityNext,
     *     uint8 feeProtocol,
     *     bool unlocked
     * );
     */
    fun slot0() = DataHexStr(
        keccak256("slot0()".encodeToByteArray()).copyOfRange(0, 4)
    )

    fun decodeSlot(data: DataHexStr): Slot0 {
        val bytes = data.toByteArrayData()
        return Slot0(
            sqrtPriceX96 = bytes.copyOfRange(0, 32),
            tick = abiDecodeUInt(bytes.copyOfRange(32, 64)),
            observationIndex = abiDecodeUInt(bytes.copyOfRange(64, 96)),
            observationCardinality = abiDecodeUInt(bytes.copyOfRange(96, 128)),
            observationCardinalityNext = abiDecodeUInt(bytes.copyOfRange(128, 160)),
            feeProtocol = abiDecodeUInt(bytes.copyOfRange(160, 192)),
            unlocked = abiDecodeUInt(bytes.copyOfRange(192, 224)) != 0u,
        )
    }

    /**
     * @notice The currently in range liquidity available to the pool
     * @dev This value has no relationship to the total liquidity across all ticks
     * function liquidity() external view returns (uint128);
     */
    fun liquidity() = DataHexStr(
        keccak256("liquidity()".encodeToByteArray()).copyOfRange(0, 4)
    )

    fun decodeLiquidity(data: DataHexStr): BigInt = abiDecodeBigInt(data)

    data class Slot0(
        val sqrtPriceX96: ByteArray,
        val tick: UInt,
        val observationIndex: UInt,
        val observationCardinality: UInt,
        val observationCardinalityNext: UInt,
        val feeProtocol: UInt,
        val unlocked: Boolean
    )
}