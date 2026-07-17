package com.lc5900.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B5CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C1A62),
    onPrimaryContainer = Color(0xFFE9DEFF),
    secondary = Color(0xFF54A0FF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF172A50),
    onSecondaryContainer = Color(0xFFDCE5FF),
    tertiary = Color(0xFF50D89D),
    onTertiary = Color(0xFF002116),
    error = Color(0xFFFF5B65),
    onError = Color.White,
    background = Color(0xFF05070D),
    onBackground = Color(0xFFF5F7FC),
    surface = Color(0xFF0B101A),
    onSurface = Color(0xFFF5F7FC),
    onSurfaceVariant = Color(0xFFA7B0C0),
    surfaceContainer = Color(0xFF111824),
    surfaceContainerLow = Color(0xFF0D131E),
    surfaceContainerHigh = Color(0xFF17202E),
    outline = Color(0xFF3A4352),
    outlineVariant = Color(0xFF252E3D),
)

private val OpenTvTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
)

private val OpenTvShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Composable
fun OpenTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = OpenTvTypography,
        shapes = OpenTvShapes,
    ) {
        CompositionLocalProvider(LocalContentColor provides DarkColors.onBackground) {
            content()
        }
    }
}
