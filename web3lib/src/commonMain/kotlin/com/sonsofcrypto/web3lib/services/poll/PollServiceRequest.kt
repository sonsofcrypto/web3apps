package com.sonsofcrypto.web3lib.services.poll

import com.sonsofcrypto.web3lib.abi.Call3
import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.types.AddressHexString

interface PollServiceRequest {
    val id: String
    val handler: (result: List<Any>, request: PollServiceRequest)->Unit
    val callCount: Int
    val userInfo: Any?
    fun calls(): List<Call3>
}

class FnPollServiceRequest(
    override val id: String,
    val address: AddressHexString,
    val iface: Interface,
    val fnName: String,
    val values: List<Any> = emptyList(),
    override val handler: (result: List<Any>, request: PollServiceRequest)->Unit,
    override val userInfo: Any? = null,
): PollServiceRequest {

    override val callCount: Int = 1

    override fun calls(): List<Call3> = listOf(
        Call3(
            address,
            true,
            iface.encodeFunction(iface.function(fnName), values)
        )
    )
}

class GroupPollServiceRequest(
    override val id: String,
    val calls: List<Call3>,
    override val handler: (result: List<Any>, request: PollServiceRequest)->Unit,
    override val userInfo: Any? = null,
): PollServiceRequest {

    override val callCount: Int by lazy { calls.count() }

    override fun calls(): List<Call3> = this.calls
}