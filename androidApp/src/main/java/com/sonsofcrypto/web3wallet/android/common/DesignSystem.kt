package com.sonsofcrypto.web3wallet.android.common

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun backgroundGradient(): Brush {
    return Brush.verticalGradient(listOf(theme().colors.bgGradientTop, theme().colors.bgGradientBtm))
}

@Composable
fun W3WDivider() {
    Divider(
        color = theme().colors.separatorPrimary,
        thickness = 0.5.dp
    )
}