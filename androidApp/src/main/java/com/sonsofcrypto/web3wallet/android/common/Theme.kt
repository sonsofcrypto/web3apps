package com.sonsofcrypto.web3wallet.android.common

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData

var AppTheme: MutableLiveData<Theme> = MutableLiveData()

@Composable
fun theme(): Theme {
    val theme by AppTheme.observeAsState(themeMiamiSunriseDark)
    return theme
}

//if (AppTheme.value == themeMiamiSunriseDark) {
//    AppTheme.value = themeMiamiSunriseLight
//} else {
//    AppTheme.value = themeMiamiSunriseDark
//}

data class Theme(
    val fonts: ThemeFonts,
    val colors: ThemeColors,
    val shapes: ThemeShapes,
)

data class ThemeShapes(
    val padding: Dp = 16.dp,
    val cornerRadius: Dp = 16.dp,
    val cornerRadiusSmall: Dp = 8.dp,
    val cellHeight: Dp = 64.dp,
    val cellHeightSmall: Dp = 46.dp,
)

data class ThemeFonts(
    val largeTitle: TextStyle = TextStyle(fontSize = TextUnit(34.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 41),
    val largeTitleBold: TextStyle = TextStyle(fontSize = TextUnit(34.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Bold), // line_height = 41),
    val title1: TextStyle = TextStyle(fontSize = TextUnit(28.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 34),
    val title1Bold: TextStyle = TextStyle(fontSize = TextUnit(28.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Bold), // line_height = 34),
    val title2: TextStyle = TextStyle(fontSize = TextUnit(22.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 28),
    val title2Bold: TextStyle = TextStyle(fontSize = TextUnit(22.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Bold), // line_height = 28),
    val title3: TextStyle = TextStyle(fontSize = TextUnit(20.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 25),
    val title3Bold: TextStyle = TextStyle(fontSize = TextUnit(20.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 25),
    val headline: TextStyle = TextStyle(fontSize = TextUnit(17.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 22),
    val headlineBold: TextStyle = TextStyle(fontSize = TextUnit(17.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 22),
    val subheadline: TextStyle = TextStyle(fontSize = TextUnit(15.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 20),
    val subheadlineBold: TextStyle = TextStyle(fontSize = TextUnit(15.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 20),
    val body: TextStyle = TextStyle(fontSize = TextUnit(17.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 22),
    val bodyBold: TextStyle = TextStyle(fontSize = TextUnit(17.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 22),
    val callout: TextStyle = TextStyle(fontSize = TextUnit(16.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 21),
    val calloutBold: TextStyle = TextStyle(fontSize = TextUnit(16.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 21),
    val footnote: TextStyle = TextStyle(fontSize = TextUnit(13.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 18),
    val footnoteBold: TextStyle = TextStyle(fontSize = TextUnit(13.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 18),
    val caption1: TextStyle = TextStyle(fontSize = TextUnit(12.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 16),
    val caption1Bold: TextStyle = TextStyle(fontSize = TextUnit(12.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 16),
    val caption2: TextStyle = TextStyle(fontSize = TextUnit(11.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 13),
    val caption2Bold: TextStyle = TextStyle(fontSize = TextUnit(11.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 13),
    val extraSmall: TextStyle = TextStyle(fontSize = TextUnit(8.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal),
    val extraSmallBold: TextStyle = TextStyle(fontSize = TextUnit(8.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold),
    val navTitle: TextStyle = TextStyle(fontSize = TextUnit(18.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.Normal), // line_height = 20),
    val tabBar: TextStyle = TextStyle(fontSize = TextUnit(11.toFloat(), TextUnitType.Sp), fontWeight = FontWeight.SemiBold), // line_height = 13),
)

data class ThemeColors(
    var clear: Color = Color.Transparent,
    var textPrimary: Color,
    var textSecondary: Color,
    var textTertiary: Color,
    var bgPrimary: Color,
    var bgGradientTop: Color,
    var bgGradientBtm: Color,
    var navBarBackground: Color,
    var navBarTint: Color,
    var navBarTitle: Color,
    var tabBarBackground: Color,
    var tabBarTint: Color,
    var tabBarTintSelected: Color,
    var stroke: Color,
    var separatorPrimary: Color,
    var separatorSecondary: Color,
    var buttonBgPrimary: Color,
    var buttonBgPrimaryDisabled: Color,
    var buttonTextPrimary: Color,
    var buttonBgSecondary: Color,
    var buttonTextSecondary: Color,
    var switchOnThumb: Color,
    var switchOnTint: Color,
    var switchOffThumb: Color,
    var switchOffTint: Color,
    var segmentedControlBackground: Color,
    var segmentedControlBackgroundSelected: Color,
    var segmentedControlText: Color,
    var segmentedControlTextSelected: Color,
    var priceUp: Color,
    var priceDown: Color,
    var dashboardTVCryptoBalance: Color,
    var activityIndicator: Color,
    var destructive: Color,
    var textFieldError: Color,
)

val themeMiamiSunriseLight = Theme(
    ThemeFonts(),
    ThemeMiamiSunrise().lightColors,
    ThemeShapes(),
)

val themeMiamiSunriseDark = Theme(
    ThemeFonts(),
    ThemeMiamiSunrise().darkColors,
    ThemeShapes(),
)

val themeVanillaLight = Theme(
    ThemeFonts(),
    ThemeVanilla().lightColors,
    ThemeShapes(),
)

val themeVanillaDark = Theme(
    ThemeFonts(),
    ThemeVanilla().darkColors,
    ThemeShapes(),
)

class ThemeMiamiSunrise {

    private val paletteLight = ColorPaletteLight()
    private val paletteDark = ColorPaletteDark()

    val lightColors: ThemeColors get() = ThemeColors(
        textPrimary = paletteLight.white,
        textSecondary = paletteLight.white6,
        textTertiary = paletteLight.white3,
        bgPrimary = paletteLight.white18,
        bgGradientTop = paletteLight.blue,
        bgGradientBtm = paletteLight.pinkLight,
        navBarBackground = paletteLight.lightBlack,
        navBarTint = paletteLight.orange,
        navBarTitle = paletteLight.white,
        tabBarBackground = paletteLight.lightBlack,
        tabBarTint = paletteLight.blue,
        tabBarTintSelected = paletteLight.pink,
        stroke = paletteLight.lightGray,
        separatorPrimary = paletteLight.lighterGray,
        separatorSecondary = paletteLight.white3,
        buttonBgPrimary = paletteLight.blue,
        buttonBgPrimaryDisabled = paletteLight.blue.copy(alpha = 0.5f),
        buttonTextPrimary = paletteLight.white,
        buttonBgSecondary = paletteLight.lightGray,
        buttonTextSecondary = paletteLight.white,
        switchOnThumb = paletteLight.white,
        switchOnTint = paletteLight.orange,
        switchOffThumb = paletteLight.white,
        switchOffTint = paletteLight.white6,
        segmentedControlBackground = paletteLight.lighterGray.copy(alpha = 0.1f),
        segmentedControlBackgroundSelected = paletteLight.white18,
        segmentedControlText = paletteLight.white,
        segmentedControlTextSelected = paletteLight.white,
        priceUp = paletteLight.teal,
        priceDown = paletteLight.pink,
        dashboardTVCryptoBalance = paletteLight.orange,
        activityIndicator = paletteLight.white,
        destructive = paletteLight.red,
        textFieldError = Color(0xFF7D1935),
    )

    val darkColors: ThemeColors get() = ThemeColors(
        textPrimary = paletteDark.white,
        textSecondary = paletteDark.white6,
        textTertiary = paletteDark.white3,
        bgPrimary = paletteDark.white18,
        bgGradientTop = paletteDark.pink,
        bgGradientBtm = paletteDark.purple,
        navBarBackground = paletteDark.lightBlack,
        navBarTint = paletteDark.orange,
        navBarTitle = paletteDark.white,
        tabBarBackground = paletteDark.lightBlack,
        tabBarTint = paletteDark.blue,
        tabBarTintSelected = paletteDark.pink,
        stroke = paletteDark.lightGray,
        separatorPrimary = paletteDark.lighterGray,
        separatorSecondary = paletteDark.white3,
        buttonBgPrimary = paletteDark.pink,
        buttonBgPrimaryDisabled = paletteDark.pink.copy(alpha = 0.5f),
        buttonTextPrimary = paletteDark.white,
        buttonBgSecondary = paletteDark.lightGray,
        buttonTextSecondary = paletteDark.white,
        switchOnThumb = paletteLight.white,
        switchOnTint = paletteLight.orange,
        switchOffThumb = paletteLight.white,
        switchOffTint = paletteLight.white6,
        segmentedControlBackground = paletteDark.lighterGray.copy(alpha = 0.1f),
        segmentedControlBackgroundSelected = paletteDark.white18,
        segmentedControlText = paletteDark.white,
        segmentedControlTextSelected = paletteDark.white,
        priceUp = paletteDark.teal,
        priceDown = paletteDark.pink,
        dashboardTVCryptoBalance = paletteDark.orange,
        activityIndicator = paletteDark.white,
        destructive = paletteDark.red,
        textFieldError = Color(0xFF7D1935),
    )

    private data class ColorPaletteLight(
        val black: Color = Color(0xFF000000),
        val lightBlack: Color = Color(0xFF1C1C1E),
        val white: Color = Color(0xFFFFFFFF),
        val white6: Color =  Color(0x9CFFFFFF).copy(alpha = 0.6f),
        val white3: Color = Color(0x4CFFFFFF).copy(alpha = 0.3f),
        val white18: Color = Color(0x2EFFFFFF).copy(alpha = 0.18f),
        val red: Color = Color(0xFFE6350F),
        val orange: Color = Color(0xFFF29A36),
        val teal: Color = Color(0xFF13CEC4),
        val blue: Color = Color(0xFF3770E6),
        val purple: Color = Color(0xFF9630B0),
        val pink: Color = Color(0xFFF24A9B),
        val pinkLight: Color = Color(0xFFFC78A9),
        val gray: Color = Color(0xFF8E8E92),
        val lightGray: Color = Color(0xFF787880).copy(alpha = 0.2f),
        val lighterGray: Color = Color(0xFFC6C6C8),
    )

    private data class ColorPaletteDark(
        val black: Color = Color(0xFF000000),
        val lightBlack: Color = Color(0xFF1C1C1E),
        val white: Color = Color(0xFFFFFFFF),
        val white6: Color =  Color(0x9CFFFFFF).copy(alpha = 0.6f),
        val white3: Color = Color(0x4CFFFFFF).copy(alpha = 0.3f),
        val white18: Color = Color(0x2EFFFFFF).copy(alpha = 0.18f),
        val red: Color = Color(0xFFF0421D),
        val orange: Color = Color(0xFFF08D1D),
        val teal: Color = Color(0xFF10B0A8),
        val blue: Color = Color(0xFF4E80E9),
        val purple: Color = Color(0xFF852B9C),
        val pink: Color = Color(0xFFF0328E),
        val pinkLight: Color = Color(0xFFFC78A9),
        val gray: Color = Color(0xFF8E8E92),
        val lightGray: Color = Color(0xFF787880).copy(alpha = 0.36f),
        val lighterGray: Color = Color(0xFFC6C6C8),
    )
}

class ThemeVanilla {

    private val paletteLight = ColorPaletteLight()
    private val paletteDark = ColorPaletteDark()

    val lightColors: ThemeColors get() = ThemeColors(
        textPrimary = paletteLight.white,
        textSecondary = paletteLight.white6,
        textTertiary = paletteLight.white3,
        bgPrimary = paletteLight.white18,
        bgGradientTop = paletteLight.blue,
        bgGradientBtm = paletteLight.pinkLight,
        navBarBackground = paletteLight.lightBlack,
        navBarTint = paletteLight.orange,
        navBarTitle = paletteLight.white,
        tabBarBackground = paletteLight.lightBlack,
        tabBarTint = paletteLight.blue,
        tabBarTintSelected = paletteLight.pink,
        stroke = paletteLight.lightGray,
        separatorPrimary = paletteLight.lighterGray,
        separatorSecondary = paletteLight.white3,
        buttonBgPrimary = paletteLight.blue,
        buttonBgPrimaryDisabled = paletteLight.blue.copy(alpha = 0.5f),
        buttonTextPrimary = paletteLight.white,
        buttonBgSecondary = paletteLight.lightGray,
        buttonTextSecondary = paletteLight.white,
        switchOnThumb = paletteLight.white,
        switchOnTint = paletteLight.orange,
        switchOffThumb = paletteLight.white,
        switchOffTint = paletteLight.white6,
        segmentedControlBackground = paletteLight.lighterGray.copy(alpha = 0.1f),
        segmentedControlBackgroundSelected = paletteLight.white18,
        segmentedControlText = paletteLight.white,
        segmentedControlTextSelected = paletteLight.white,
        priceUp = paletteLight.teal,
        priceDown = paletteLight.pink,
        dashboardTVCryptoBalance = paletteLight.orange,
        activityIndicator = paletteLight.white,
        destructive = paletteLight.red,
        textFieldError = Color(0xFF7D1935),
    )

    val darkColors: ThemeColors get() = ThemeColors(
        textPrimary = paletteDark.white,
        textSecondary = paletteDark.white6,
        textTertiary = paletteDark.white3,
        bgPrimary = paletteDark.white18,
        bgGradientTop = paletteDark.pink,
        bgGradientBtm = paletteDark.purple,
        navBarBackground = paletteDark.lightBlack,
        navBarTint = paletteDark.orange,
        navBarTitle = paletteDark.white,
        tabBarBackground = paletteDark.lightBlack,
        tabBarTint = paletteDark.blue,
        tabBarTintSelected = paletteDark.pink,
        stroke = paletteDark.lightGray,
        separatorPrimary = paletteDark.lighterGray,
        separatorSecondary = paletteDark.white3,
        buttonBgPrimary = paletteDark.pink,
        buttonBgPrimaryDisabled = paletteDark.pink.copy(alpha = 0.5f),
        buttonTextPrimary = paletteDark.white,
        buttonBgSecondary = paletteDark.lightGray,
        buttonTextSecondary = paletteDark.white,
        switchOnThumb = paletteLight.white,
        switchOnTint = paletteLight.orange,
        switchOffThumb = paletteLight.white,
        switchOffTint = paletteLight.white6,
        segmentedControlBackground = paletteDark.lighterGray.copy(alpha = 0.1f),
        segmentedControlBackgroundSelected = paletteDark.white18,
        segmentedControlText = paletteDark.white,
        segmentedControlTextSelected = paletteDark.white,
        priceUp = paletteDark.teal,
        priceDown = paletteDark.pink,
        dashboardTVCryptoBalance = paletteDark.orange,
        activityIndicator = paletteDark.white,
        destructive = paletteDark.red,
        textFieldError = Color(0xFF7D1935),
    )

    private data class ColorPaletteLight(
        val black: Color = Color(0xFF000000),
        val lightBlack: Color = Color(0xFF1C1C1E),
        val white: Color = Color(0xFFFFFFFF),
        val white6: Color =  Color(0x9CFFFFFF).copy(alpha = 0.6f),
        val white3: Color = Color(0x4CFFFFFF).copy(alpha = 0.3f),
        val white18: Color = Color(0x2EFFFFFF).copy(alpha = 0.18f),
        val red: Color = Color(0xFFE6350F),
        val orange: Color = Color(0xFFF29A36),
        val teal: Color = Color(0xFF13CEC4),
        val blue: Color = Color(0xFF3770E6),
        val purple: Color = Color(0xFF9630B0),
        val pink: Color = Color(0xFFF24A9B),
        val pinkLight: Color = Color(0xFFFC78A9),
        val gray: Color = Color(0xFF8E8E92),
        val lightGray: Color = Color(0xFF787880).copy(alpha = 0.2f),
        val lighterGray: Color = Color(0xFFC6C6C8),
    )

    private data class ColorPaletteDark(
        val black: Color = Color(0xFF000000),
        val lightBlack: Color = Color(0xFF1C1C1E),
        val white: Color = Color(0xFFFFFFFF),
        val white6: Color =  Color(0x9CFFFFFF).copy(alpha = 0.6f),
        val white3: Color = Color(0x4CFFFFFF).copy(alpha = 0.3f),
        val white18: Color = Color(0x2EFFFFFF).copy(alpha = 0.18f),
        val red: Color = Color(0xFFF0421D),
        val orange: Color = Color(0xFFF08D1D),
        val teal: Color = Color(0xFF10B0A8),
        val blue: Color = Color(0xFF4E80E9),
        val purple: Color = Color(0xFF852B9C),
        val pink: Color = Color(0xFFF0328E),
        val pinkLight: Color = Color(0xFFFC78A9),
        val gray: Color = Color(0xFF8E8E92),
        val lightGray: Color = Color(0xFF787880).copy(alpha = 0.36f),
        val lighterGray: Color = Color(0xFFC6C6C8),
    )
}