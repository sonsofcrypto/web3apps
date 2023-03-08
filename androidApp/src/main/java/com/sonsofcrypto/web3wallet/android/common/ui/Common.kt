package com.sonsofcrypto.web3wallet.android.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.sonsofcrypto.web3wallet.android.common.extensions.half
import com.sonsofcrypto.web3wallet.android.common.theme

@Composable
fun W3WSpacerVertical(height: Dp = theme().shapes.padding) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun W3WSpacerHorizontal(width: Dp = theme().shapes.padding) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun W3WDivider(
    modifier: Modifier = Modifier,
    color: Color = theme().colors.separatorPrimary,
    thickness: Dp = 0.5.dp,
    startIndent: Dp = 0.dp
) {
    Divider(
        modifier,
        color,
        thickness,
        startIndent,
    )
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
                onClick = onClick ?: {},
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
fun W3WCardWithTitle(
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
        W3WText(
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
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
    onRightIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
            .then(modifier),
        enabled = if (isLoading) false else isEnabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            disabledBackgroundColor = theme().colors.buttonBgPrimaryDisabled,
            backgroundColor = if (isDestructive)
                theme().colors.destructive else theme().colors.buttonBgPrimary,
        )
    ) {
        if (isLoading) {
            W3WLoading(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
            )
            W3WSpacerHorizontal(theme().shapes.padding.half)
        }
        Text(
            title,
            color = theme().colors.textPrimary,
            style = theme().fonts.title3,
        )
        onRightIcon?.let {
            W3WSpacerHorizontal(theme().shapes.padding.half)
            it()
        }
    }
}

@Composable
fun W3WButtonSecondarySmall(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = Modifier
            .onSizeChanged { size = it }
            .wrapContentWidth()
            .height(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .border(
                width = 0.5.dp,
                color = theme().colors.textPrimary,
                shape = RoundedCornerShape(size.height.dp.half)
            )
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        W3WSpacerHorizontal(theme().shapes.padding.half)
        Text(
            title,
            color = theme().colors.buttonTextSecondary,
            style = theme().fonts.footnote,
        )
        W3WSpacerHorizontal(theme().shapes.padding.half)
    }
}

@Composable
fun W3WButtonSquare(
    @DrawableRes iconId: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
            .background(theme().colors.bgPrimary)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        W3WIcon(id = iconId)
        W3WSpacerVertical(theme().shapes.padding.half)
        W3WText(
            title,
            style = theme().fonts.subheadline,
        )
    }
}

@Composable
fun W3WText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = theme().colors.textPrimary,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = theme().fonts.body
) {
    Text(
        text,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        minLines,
        onTextLayout,
        style,
    )
}

@Composable
fun W3WText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = theme().colors.textPrimary,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = theme().fonts.body,
) {
    Text(
        text,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        minLines,
        inlineContent,
        onTextLayout,
        style,
    )
}

@Composable
fun W3WGifImage(
    data: Any?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = data).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .then(modifier),
    )
}

@Composable
fun W3WIcon(
    @DrawableRes id: Int,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp),
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painterResource(id = id),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = colorFilter
    )
}

@Composable
fun W3WClearIcon(onClear: () -> Unit) {
    Icon(
        Icons.Default.Clear,
        contentDescription = "clear text",
        modifier = Modifier
            .clickable { onClear() }
            .size(24.dp),
        tint = theme().colors.textSecondary,
    )
}
