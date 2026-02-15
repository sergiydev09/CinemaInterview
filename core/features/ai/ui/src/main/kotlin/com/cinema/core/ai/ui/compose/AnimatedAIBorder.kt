package com.cinema.core.ai.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedAIBorder(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_border")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "ai_border_rotation"
    )

    val colors = listOf(
        Color(0xFF1E88E5), // Blue
        Color(0xFFFFD600), // Gold
        Color(0xFF7C4DFF), // Purple
        Color(0xFF00E5FF), // Cyan
        Color(0xFF1E88E5)  // Blue (close the loop)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()

                val center = Offset(size.width / 2, size.height / 2)
                val angleRad = Math.toRadians(angle.toDouble())
                val offsetX = (cos(angleRad) * size.width / 4).toFloat()
                val offsetY = (sin(angleRad) * size.height / 4).toFloat()
                val gradientCenter = Offset(center.x + offsetX, center.y + offsetY)

                val brush = ShaderBrush(
                    SweepGradientShader(
                        center = gradientCenter,
                        colors = colors,
                        colorStops = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
                    )
                )

                drawRect(
                    brush = brush,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
    )
}
