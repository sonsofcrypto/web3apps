package com.sonsofcrypto.web3wallet.android.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sonsofcrypto.web3walletcore.extensions.Localized

@Composable
fun backgroundGradient(): Brush {
    return Brush.verticalGradient(listOf(theme().colors.bgGradientTop, theme().colors.bgGradientBtm))
}

@Composable
fun NavigationBar(
    title: String,
    content: @Composable() (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .background(theme().colors.navBarBackground)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(theme().shapes.padding))
        Text(
            title,
            color = theme().colors.navBarTitle,
            style = theme().fonts.navTitle,
        )
        Spacer(modifier = Modifier.height(theme().shapes.padding))
        content?.let {
            it()
            Spacer(modifier = Modifier.height(theme().shapes.padding))
        }
    }
}
@Composable
fun W3WDivider() {
    Divider(
        color = theme().colors.separatorPrimary,
        thickness = 0.5.dp
    )
}