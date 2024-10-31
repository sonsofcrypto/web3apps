package com.sonsofcrypto.web3lib.integrations.uniswap.contracts

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.legacy.LegacyContract
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiEncode
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256
import kotlinx.datetime.Instant

class IV3SwapRouter(address: Address.HexStr): LegacyContract(address) {
    /**
     * struct ExactInputSingleParams {
     *     address tokenIn;
     *     address tokenOut;
     *     uint24 fee;
     *     address recipient;
     *     uint256 amountIn;
     *     uint256 amountOutMinimum;
     *     uint160 sqrtPriceLimitX96;
     * }
     */
    data class ExactInputSingleParams(
        val tokenIn: AddressHexString,
        val tokenOut: AddressHexString,
        val fee: UInt,
        val recipient: Recipient,
        val amountIn: BigInt,
        val amountOutMinimum: BigInt,
        val sqrtPriceLimitX96: BigInt
    ) {
        fun encoded(): ByteArray = abiEncode(Address.HexStr(tokenIn)) +
            abiEncode(Address.HexStr(tokenOut)) +
            abiEncode(fee) +
            recipient.abiEncoded() +
            abiEncode(amountIn) +
            abiEncode(amountOutMinimum) +
            abiEncode(sqrtPriceLimitX96)

        sealed class Recipient() {
            data class HexAddress(val address: AddressHexString): Recipient()
            object This: Recipient()
            object MsgSender: Recipient()

            fun abiEncoded(): ByteArray = when (this) {
                is HexAddress -> abiEncode(Address.HexStr(this.address))
                is This -> abiEncode(BigInt.from(2))
                is MsgSender -> abiEncode(BigInt.from(1))
            }
        }
    }

    /**
     * @notice Swaps `amountIn` of one token for as much as possible of another
     * token
     * @dev Setting `amountIn` to 0 will cause the contract to look up its own
     * balance,and swap the entire amount, enabling contracts to send tokens
     * before calling this function.
     * @param params The parameters necessary for the swap, encoded as
     * `ExactInputSingleParams` in calldata
     * @return amountOut The amount of the received token
     *
     * function exactInputSingle(
     *     ExactInputSingleParams calldata params
     * ) external payable returns (uint256 amountOut);
     */
    fun exactInputSingle(params: ExactInputSingleParams) = DataHexStr(
        keccak256(
            "exactInputSingle((address,address,uint24,address,uint256,uint256,uint160))"
                .encodeToByteArray()
        ).copyOfRange(0, 4)
        + params.encoded()
    )

    /**
     * @notice Unwraps the contract's WETH9 balance and sends it to msg.sender as ETH.
     * @dev The amountMinimum parameter prevents malicious contracts from stealing WETH9 from users.
     * @param amountMinimum The minimum amount of WETH9 to unwrap
     *
     * function unwrapWETH9(uint256 amountMinimum) external payable;
     */
    fun unwrapWETH9(amountMinimum: BigInt): DataHexStr = DataHexStr(
        keccak256("unwrapWETH9(uint256)".encodeToByteArray()).copyOfRange(0, 4) +
        abiEncode(amountMinimum)
    )

    /**
     * @notice Call multiple functions in the current contract and return the data from all of them if they all succeed
     * @dev The `msg.value` should not be trusted for any method callable from multicall.
     * @param deadline The time by which this function must be called before failing
     * @param data The encoded function data for each of the calls to make to this contract
     * @return results The results from each of the calls passed in via data
     *
     * function multicall(uint256 deadline, bytes[] calldata data) external payable returns (bytes[] memory results);
     */
    fun multicall(deadline: Instant, calldata: List<ByteArray>): DataHexStr {
        var data = keccak256(
            "multicall(uint256,bytes[])".encodeToByteArray()
        ).copyOfRange(0, 4)

        data += abiEncode(BigInt.from(deadline.epochSeconds))

        // Encode first param
        // Encode offset of array start 64 (guess from lenghts en)
        // Encode number of elements of array
        // Encode start of first string 64
        // (length of heads plust first lenght) Start of second start (for some reason length / 32 - 1)
        // Lenght of first string
        // First string
        // Lenght of second sctring
        // Second string

        // TODO: Refactor
        if (calldata.size == 1) {
            data += abiEncode(BigInt.from(64))
            data += abiEncode(BigInt.from(1))
            data += abiEncode(BigInt.from(32))
            data += abiEncode(BigInt.from(calldata[0].size))

            val remainder = calldata[0].size % 32
            if (remainder != 0) {
                var rightPadded = ByteArray(calldata[0].size + 32 - remainder)
                calldata[0].forEachIndexed { idx, byte -> rightPadded.set(idx, byte) }
                data += rightPadded
            } else data += calldata[0]

        } else if (calldata.size == 2) {
            data += abiEncode(BigInt.from(64))
            data += abiEncode(BigInt.from(2))
            data += abiEncode(BigInt.from(64))

            var remainder = calldata[0].size % 32
            val encodedFirst = if (remainder != 0) {
                var rightPadded = ByteArray(calldata[0].size + 32 - remainder)
                calldata[0].forEachIndexed { idx, byte -> rightPadded.set(idx, byte) }
                rightPadded
            } else calldata[0]

            data += abiEncode(BigInt.from(encodedFirst.size + 96))
            data += abiEncode(BigInt.from(calldata[0].size))
            data += encodedFirst

            data += abiEncode(BigInt.from(calldata[1].size))

            remainder = calldata[1].size % 32
            if (remainder != 0) {
                var rightPadded = ByteArray(calldata[1].size + 32 - remainder)
                calldata[1].forEachIndexed { idx, byte -> rightPadded.set(idx, byte) }
                data += rightPadded
            } else data += calldata[1]

        } else {
            throw Exception("Unsupported number of calldatas ${calldata.size}, needs refactor")
        }
        return DataHexStr(data)
    }
}


// MethodID: 0x5ae401dc
// 000000000000000000000000000000000000000000000000000000006313ceb1
// 0000000000000000000000000000000000000000000000000000000000000040
// 0000000000000000000000000000000000000000000000000000000000000002
// 0000000000000000000000000000000000000000000000000000000000000040
// 0000000000000000000000000000000000000000000000000000000000000160
// 00000000000000000000000000000000000000000000000000000000000000e4
// 04e45aaf0000000000000000000000006b175474e89094c44da98b954eedeac4
// 95271d0f000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead908
// 3c756cc200000000000000000000000000000000000000000000000000000000
// 000001f400000000000000000000000000000000000000000000000000000000
// 00000002000000000000000000000000000000000000000000000000128b38d5
// 46097bbe0000000000000000000000000000000000000000000000000002c9ed
// 87b447a800000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000044
// 49404b7c0000000000000000000000000000000000000000000000000002c9ed
// 87b447a800000000000000000000000058aebec033a2d55e35e44e6d7b43725b
// 069f6abc00000000000000000000000000000000000000000000000000000000

// 0x5ae401dc
// 000000000000000000000000000000000000000000000000000000006313cb51
// 0000000000000000000000000000000000000000000000000000000000000040 // 64 - next line start
// 0000000000000000000000000000000000000000000000000000000000000002 // 2 variables in array
// 0000000000000000000000000000000000000000000000000000000000000040 // 64 - next Line start
// 0000000000000000000000000000000000000000000000000000000000000160 // 352 / 32 = 11 <- lines
// 00000000000000000000000000000000000000000000000000000000000000e4 // 228 ??
// 04e45aaf0000000000000000000000006b175474e89094c44da98b954eedeac4
// 95271d0f000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead908
// 3c756cc200000000000000000000000000000000000000000000000000000000
// 000001f400000000000000000000000000000000000000000000000000000000
// 0000000200000000000000000000000000000000000000000000000045639182
// 44f40000000000000000000000000000000000000000000000000000000b2e6f
// 3a68a00000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000024
// 49616997000000000000000000000000000000000000000000000000000b2e6f
// 3a68a00000000000000000000000000000000000000000000000000000000000

// Method: 04e45aaf - exactInputSingle((address,address,uint24,address,uint256,uint256,uint160))
// 0000000000000000000000006b175474e89094c44da98b954eedeac495271d0f // tokenIn 0x6b175474e89094c44da98b954eedeac495271d0f
// 000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2 // tokenOut 0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2
// 00000000000000000000000000000000000000000000000000000000000001f4 // fee 500
// 0000000000000000000000000000000000000000000000000000000000000002 // recipient 2 ??
// 0000000000000000000000000000000000000000000000004563918244f40000 // deadline 5000000000000000000
// 000000000000000000000000000000000000000000000000000b2e6f3a68a000 // amountOut 3147280000000000
// 0000000000000000000000000000000000000000000000000000000000000000 // IGNORE?
// 0000000000000000000000000000000000000000000000000000000000000000 // IGNORE?
// 0000000000000000000000000000000000000000000000000000002449616997 // amountInMaximum 155849943447
// 000000000000000000000000000000000000000000000000000b2e6f3a68a000 // sqrtPriceLimitX96 3147280000000000

//TX: https://etherscan.io/tx/0x17f014cc2477df2a2347035057d0296f45c4b74bb0ccccd3ccc73c91339c96a8
//Method ID: 0x5ae401dc - multicall(uint256 deadline,bytes[] data)
//000000000000000000000000000000000000000000000000000000006313d03e
//0000000000000000000000000000000000000000000000000000000000000040
//0000000000000000000000000000000000000000000000000000000000000002
//0000000000000000000000000000000000000000000000000000000000000040
//0000000000000000000000000000000000000000000000000000000000000160
//00000000000000000000000000000000000000000000000000000000000000e4
//
//Method ID 2: 0x5023b4df - exactOutputSingle((address,address,uint24,address,uint256,uint256,uint160))
//000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2 address 0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2
//0000000000000000000000006b175474e89094c44da98b954eedeac495271d0f address 0x6b175474e89094c44da98b954eedeac495271d0f
//00000000000000000000000000000000000000000000000000000000000001f4 uint24 500
//00000000000000000000000058aebec033a2d55e35e44e6d7b43725b069f6abc address 0x58aebec033a2d55e35e44e6d7b43725b069f6abc
//0000000000000000000000000000000000000000000000008ac7230489e80000 uint256 10000000000000000000
//0000000000000000000000000000000000000000000000000016faa93b3541f3 uint256 6468054237397491
//0000000000000000000000000000000000000000000000000000000000000000
//0000000000000000000000000000000000000000000000000000000000000000
//0000000000000000000000000000000000000000000000000000000412210e8a uint160 17484025482
















