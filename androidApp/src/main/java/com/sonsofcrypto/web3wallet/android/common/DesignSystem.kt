package com.sonsofcrypto.web3wallet.android.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

@Composable
fun backgroundGradient(): Brush {
    return Brush.verticalGradient(listOf(theme().colors.bgGradientTop, theme().colors.bgGradientBtm))
}
