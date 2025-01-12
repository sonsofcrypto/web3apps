package com.sonsofcrypto.web3walletcore.common.viewModels

import com.sonsofcrypto.web3walletcore.extensions.Localized

sealed class CellViewModel {
    enum class Accessory {
        NONE, DETAIL, CHECKMARK, COPY
    }

    data class Label(
        val text: String,
        val accessory: Accessory = Accessory.NONE,
        val selected: Boolean = false,
        val trailingText: String? = null,
        val image: ImageMedia? = null,
    ): CellViewModel() {
        companion object {
            fun with(
                localizedKey: String,
                image: ImageMedia? = null,
                accessory: Accessory = Accessory.DETAIL
            ): Label =
                Label(Localized(localizedKey), accessory, false, null, image)
        }
    }

    data class Text(val text: String?): CellViewModel()

    data class TextInput(
        val title: String,
        val value: String,
        val placeholder: String,
    ): CellViewModel()

    data class SwitchTextInput(
        val title: String,
        val onOff: Boolean,
        val text: String,
        val placeholder: String,
        val description: String,
        val descriptionHighlightedWords: List<String>,
    ): CellViewModel()

    data class Switch(
        val title: String,
        val onOff: Boolean,
        val image: ImageMedia? = null,
    ): CellViewModel()

    data class SegmentSelection(
        val title: String,
        val values: List<String>,
        val selectedIdx: Int,
    ): CellViewModel()

    data class SegmentWithTextAndSwitch(
        val title: String,
        val segmentOptions: List<String>,
        val selectedSegment: Int,
        val password: String,
        val passwordKeyboardType: KeyboardType,
        val placeholder: String,
        val errorMessage: String?,
        val onOffTitle: String,
        val onOff: Boolean,
    ): CellViewModel() {
        enum class KeyboardType { DEFAULT, NUMBER_PAD }
        val hasHint: Boolean get() = errorMessage != null
    }
    
    data class Button(val button: ButtonViewModel): CellViewModel()

    data class KeyValueList(
        val items: List<Item>,
        val userInfo: Map<String, Any>? = null
    ): CellViewModel() {
        data class Item(
            val key: String,
            val value: String,
            val placeholder: String? = null,
            val userInfo: Map<String, Any>? = null
        )
    }
}
