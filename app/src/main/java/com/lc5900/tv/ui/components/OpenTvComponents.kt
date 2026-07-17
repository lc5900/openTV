package com.lc5900.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object OpenTvColors {
    val Background = Color(0xFF05070D)
    val BackgroundElevated = Color(0xFF0B101A)
    val Surface = Color(0xFF111824)
    val SurfaceHigh = Color(0xFF17202E)
    val Outline = Color(0xFF252E3D)
    val TextPrimary = Color(0xFFF5F7FC)
    val TextSecondary = Color(0xFF8D96A8)
    val Purple = Color(0xFF7A35FF)
    val Indigo = Color(0xFF4938F5)
    val Blue = Color(0xFF2889FF)
    val Cyan = Color(0xFF57D6FF)
    val Green = Color(0xFF50D89D)
    val Red = Color(0xFFFF5B65)
    val Amber = Color(0xFFFFB552)

    val PrimaryGradient = Brush.horizontalGradient(listOf(Purple, Indigo, Blue))
    val ScreenGradient = Brush.verticalGradient(
        listOf(Color(0xFF0D1424), BackgroundElevated, Background),
    )
    val HeroGradient = Brush.linearGradient(
        listOf(Color(0xFF211052), Color(0xFF4021A7), Color(0xFF2188F5)),
    )
}

@Composable
fun OpenTvBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.background(OpenTvColors.ScreenGradient),
        content = content,
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (enabled) OpenTvColors.PrimaryGradient else Brush.horizontalGradient(
                    listOf(OpenTvColors.SurfaceHigh, OpenTvColors.SurfaceHigh),
                ),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else OpenTvColors.TextSecondary,
        )
    }
}
