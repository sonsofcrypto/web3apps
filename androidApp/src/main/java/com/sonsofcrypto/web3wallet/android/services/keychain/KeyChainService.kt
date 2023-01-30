package com.sonsofcrypto.web3wallet.android.services.keychain

import com.sonsofcrypto.web3lib.services.keyStore.KeyChainService
import com.sonsofcrypto.web3lib.services.keyStore.ServiceType
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class DefaultKeyChainService: KeyChainService {

    override fun get(id: String, type: ServiceType): ByteArray {
        TODO("Not yet implemented")
    }

    override fun set(id: String, data: ByteArray, type: ServiceType, icloud: Boolean) {
        TODO("Not yet implemented")
    }

    override fun remove(id: String, type: ServiceType) {
        TODO("Not yet implemented")
    }

    override fun biometricsSupported(): Boolean {
        TODO("Not yet implemented")
    }

    override fun biometricsAuthenticate(title: String, handler: (Boolean, Error?) -> Unit) {
        TODO("Not yet implemented")
    }
}

class KeyChainServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("KeyChainService", AssemblerRegistryScope.SINGLETON) {
            DefaultKeyChainService()
        }
    }
}