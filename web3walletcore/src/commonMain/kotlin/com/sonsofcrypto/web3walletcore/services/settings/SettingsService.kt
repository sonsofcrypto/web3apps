package com.sonsofcrypto.web3walletcore.services.settings

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3walletcore.common.ThemeId
import com.sonsofcrypto.web3walletcore.common.ThemeVariant

enum class NFTCarouselSize(val raw: String) {
    REGULAR("regular"), LARGE("small")
}

interface SettingsService {
    var themeId: ThemeId
    var themeVariant: ThemeVariant
    var nftCarouselSize: NFTCarouselSize
    var expertMode: Boolean
}

class DefaultSettingsService(
    private val store: KeyValueStore
): SettingsService {

    override var themeId: ThemeId
        get() = ThemeId.values().firstOrNull {
            it.raw == (store.get<String>("themeId") ?: "")
        } ?: ThemeId.MIAMI
        set(id) = store.set("themeId", id.raw)

    override var themeVariant: ThemeVariant
        get() = ThemeVariant.values().firstOrNull {
            it.raw == (store.get<String>("themeVariant") ?: "")
        } ?: ThemeVariant.LIGHT
        set(mode) = store.set("themeVariant", mode.raw)

    override var nftCarouselSize: NFTCarouselSize
        get() = NFTCarouselSize.values().firstOrNull {
            it.raw == (store.get<String>("nftCarouselSize") ?: "")
        } ?: NFTCarouselSize.REGULAR
        set(size) = store.set("nftCarouselSize", size.raw)

    override var expertMode: Boolean
        get() = store.get<Boolean>("expertMode") ?: false
        set(mode) = store.set("expertMode", mode)
}

