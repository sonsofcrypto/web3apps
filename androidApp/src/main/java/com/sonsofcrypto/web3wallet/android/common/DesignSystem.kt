package com.sonsofcrypto.web3wallet.android.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalPresenterEvent

@Composable
fun backgroundGradient(): Brush {
    return Brush.verticalGradient(listOf(theme().colors.bgGradientTop, theme().colors.bgGradientBtm))
}

@Composable
fun W3WScreen(
    navBar: @Composable() (() -> Unit)? = null,
    content: @Composable() (() -> Unit),
) {
    Column(
        modifier = Modifier
            .background(backgroundGradient())
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        navBar?.let { it() }
        content()
    }
}

@Composable
fun W3WNavigationBar(
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
fun W3WSpacerVertical(height: Dp = theme().shapes.padding) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun W3WSpacerHorizontal(width: Dp = theme().shapes.padding) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun W3WDivider() {
    Divider(
        color = theme().colors.separatorPrimary,
        thickness = 0.5.dp
    )
}
@Composable
fun W3WLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient())
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun W3WImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = url,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(RoundedCornerShape(theme().shapes.cornerRadius))
            .background(theme().colors.textSecondary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick?.let { it } ?: {},
            )
            .then(modifier),
        loading = {
            CircularProgressIndicator(
                modifier = Modifier
                    .requiredSize(32.dp)
                    .align(Alignment.Center)
            )
        },
        contentDescription = contentDescription
    )
}

@Composable
fun W3WCard(
    title: String,
    content: @Composable() (() -> Unit)? = null,
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(theme().shapes.cornerRadius))
            .background(theme().colors.bgPrimary)
            .padding(theme().shapes.padding),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            title,
            color = theme().colors.textPrimary,
            style = theme().fonts.headlineBold,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(8.dp))
        W3WDivider()
        Spacer(modifier = Modifier.height(8.dp))
        content?.let { it() }
    }
}

@Composable
fun W3WButtonPrimary(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
            .then(modifier),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = theme().colors.buttonBgPrimary
        )
    ) {
        Text(
            title,
            color = theme().colors.textPrimary,
            style = theme().fonts.title3,
        )
    }
}