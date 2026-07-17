package com.lc5900.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A78),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9DEFFD),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF445E91),
    secondaryContainer = Color(0xFFD9E2FF),
    tertiary = Color(0xFF006C51),
    background = Color(0xFFF7FAFC),
    surface = Color(0xFFF7FAFC),
    surfaceContainer = Color(0xFFEDF2F5),
    surfaceContainerLow = Color(0xFFF2F6F8),
    surfaceContainerHigh = Color(0xFFE7ECEF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF51D9ED),
    onPrimary = Color(0xFF00363E),
    primaryContainer = Color(0xFF004E59),
    secondary = Color(0xFFB2C5FF),
    tertiary = Color(0xFF55DBAD),
    background = Color(0xFF071016),
    surface = Color(0xFF071016),
    surfaceContainer = Color(0xFF141F26),
    surfaceContainerLow = Color(0xFF0E191F),
    surfaceContainerHigh = Color(0xFF1E2A31),
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
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = OpenTvTypography,
        shapes = OpenTvShapes,
        content = content,
    )
}
