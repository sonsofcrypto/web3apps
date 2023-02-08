package com.sonsofcrypto.web3lib.signer.contracts

import com.sonsofcrypto.web3lib.provider.model.DataHexString
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.utils.*


open class ContractLegacy(
    open var address: Address.HexString
) {
    open class Event()

    fun decodeAddress(value: String): Address.HexString {
        return abiDecodeAddress(value)
    }
}

class ERC20(address: Address.HexString) : ContractLegacy(address) {

    /**
     * Returns the name of the token.
     * @return public view virtual override returns (string memory)
     */
    fun name(): DataHexString = DataHexString(
        keccak256("name()".encodeToByteArray()).copyOfRange(0, 4)
    )

    /**
     * Returns the symbol of the token, usually a shorter version of the name.
     * @return public view virtual override returns (string memory)
     */
    fun symbol(): DataHexString = DataHexString(
        keccak256("symbol()".encodeToByteArray()).copyOfRange(0, 4)
    )

    /**
     * Returns the number of decimals used to get its user representation.
     * For example, if `decimals` equals `2`, a balance of `505` tokens should
     * be displayed to a user as `5.05` (`505 / 10 ** 2`).
     *
     * Tokens usually opt for a value of 18, imitating the relationship between
     * Ether and Wei. This is the value {ERC20} uses, unless this function is
     * overridden;
     *
     * @return public view virtual override returns (uint8)
     */
    fun decimals(): DataHexString = DataHexString(
        keccak256("decimals()".encodeToByteArray()).copyOfRange(0, 4)
    )

    /**
     * function transfer(address to, uint256 amount)
     * - `to` cannot be the zero address.
     * - the caller must have a balance of at least `amount`.
     * @return public virtual override returns (bool)
     */
    fun transfer(to: Address.HexString, amount: BigInt): DataHexString = DataHexString(
        keccak256("transfer(address,uint256)".encodeToByteArray()).copyOfRange(0, 4)
            + abiEncode(to)
            + abiEncode(amount)
    )

    /**
     * @dev See {IERC20-balanceOf}.
     * @return public view virtual override returns (uint256)
     */
    fun balanceOf(account: Address.HexString): DataHexString = DataHexString(
        keccak256("balanceOf(address)".encodeToByteArray()).copyOfRange(0, 4) +
            abiEncode(account)
    )

    /**
     * @dev Returns the remaining number of tokens that `spender` will be
     * allowed to spend on behalf of `owner` through {transferFrom}. This is
     * zero by default.
     *
     * This value changes when {approve} or {transferFrom} are called.
     *
     * function allowance(address owner, address spender) → uint256
     */
    fun allowance(owner: Address.HexString, spender: Address.HexString): DataHexString = DataHexString(
        keccak256("allowance(address,address)".encodeToByteArray()).copyOfRange(0, 4) +
            abiEncode(owner) +
            abiEncode(spender)
    )

    fun decodeAllowance(data: DataHexString): BigInt = abiDecodeBigInt(data)

    /**
     * @dev Sets `amount` as the allowance of `spender` over the caller's tokens.
     *
     * Returns a boolean value indicating whether the operation succeeded.
     *
     * IMPORTANT: Beware that changing an allowance with this method brings the risk
     * that someone may use both the old and the new allowance by unfortunate
     * transaction ordering. One possible solution to mitigate this race
     * condition is to first reduce the spender's allowance to 0 and set the
     * desired value afterwards:
     * https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729
     *
     * Emits an {Approval} event.
     * function approve(address spender, uint256 amount) external returns (bool);
     */
    fun approve(spender: Address.HexString, amount: BigInt): DataHexString = DataHexString(
        keccak256("approve(address,uint256)".encodeToByteArray()).copyOfRange(0, 4) +
            abiEncode(spender) +
            abiEncode(amount)
    )

    fun decodeApprove(data: DataHexString): BigInt = abiDecodeBigInt(data)
}

class ERC721(address: Address.HexString): ContractLegacy(address) {

    /**
     * function transferFrom(address from, address to, uint256 tokenId)
     */
    fun transferFrom(
        from: Address.HexString,
        to: Address.HexString,
        tokenId: BigInt,
    ) = DataHexString(
        keccak256("transferFrom(address,address,uint256)".encodeToByteArray())
            .copyOfRange(0, 4) +
            abiEncode(from) +
            abiEncode(to) +
            abiEncode(tokenId)
    )
}

class CultGovernor: ContractLegacy(
    Address.HexString("0x0831172B9b136813b0B35e7cc898B1398bB4d7e7")
) {
    /**
     * @notice Cast a vote for a proposal
     * @param proposalId The id of the proposal to vote on
     * @param support The support value for the vote. 0=against, 1=for, 2=abstain
     */
    fun castVote(proposalId: UInt, support: UInt) = DataHexString(
        keccak256("castVote(uint256,uint8)".encodeToByteArray()).copyOfRange(0, 4) +
            abiEncode(proposalId) +
            abiEncode(support)
    )
}
