package com.lc5900.tv.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF315DA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E5FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF006B5F),
    tertiary = Color(0xFF00876D),
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
    surfaceContainer = Color(0xFFF0F2FA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    primaryContainer = Color(0xFF124585),
    secondary = Color(0xFF54DBC8),
    tertiary = Color(0xFF59DDB8),
    background = Color(0xFF101318),
    surface = Color(0xFF101318),
    surfaceContainer = Color(0xFF1C2026),
)

@Composable
fun OpenTvTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(colorScheme = colors, content = content)
}
