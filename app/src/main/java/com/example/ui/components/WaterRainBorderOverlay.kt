package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// Represents a falling rain droplet along the screen border
data class RainDrop(
    var x: Float,
    var y: Float,
    val speed: Float,
    val length: Float,
    val size: Float,
    val isLeft: Boolean // left border or right border
)

@Composable
fun WaterRainBorderOverlay(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    if (!active) return

    val infiniteTransition = rememberInfiniteTransition(label = "water_border")
    
    // Wave animation offset for animated border glowing liquid
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    // Pulse alpha for a magical water glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineToSineEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Initialize rain drops running along left and right sideborders
    val rainDrops = remember {
        mutableStateListOf<RainDrop>().apply {
            // Generate 15 distinct rain droplets along both vertical borders
            repeat(16) { index ->
                add(
                    RainDrop(
                        x = if (index % 2 == 0) Random.nextFloat() * 16f else 0f, // vertical boundary distance offsets
                        y = Random.nextFloat() * 1200f,
                        speed = 12f + Random.nextFloat() * 15f,
                        length = 15f + Random.nextFloat() * 25f,
                        size = 2f + Random.nextFloat() * 3f,
                        isLeft = index % 2 == 0
                    )
                )
            }
        }
    }

    // Gentle frame update to update positions of vertical rainfall droplets
    LaunchedEffect(active) {
        while (active) {
            withFrameMillis { _ ->
                for (i in rainDrops.indices) {
                    val drop = rainDrops[i]
                    var nextY = drop.y + drop.speed
                    if (nextY > 2500f) { // recycle droplet to the top of screen
                        nextY = -drop.length
                    }
                    rainDrops[i] = drop.copy(y = nextY)
                }
            }
        }
    }

    val waterCyan = Color(0xFF33B3E4)
    val lightAqua = Color(0xFFA1F2FF)
    val deepBlue = Color(0xFF1E3A8A)

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val borderThickness = 6.dp.toPx()

        // 1. Render glowing liquid outline along all four outer margins of the mobile phone screen
        val waveAmplitude = 4.dp.toPx()
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                waterCyan.copy(alpha = glowAlpha),
                lightAqua.copy(alpha = glowAlpha),
                deepBlue.copy(alpha = glowAlpha * 0.7f),
                waterCyan.copy(alpha = glowAlpha)
            ),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )

        // Draw top wave border
        val wavePointsCount = 20
        val segmentWidth = width / wavePointsCount
        for (i in 0 until wavePointsCount) {
            val startX = i * segmentWidth
            val endX = (i + 1) * segmentWidth
            val startY = borderThickness + Math.sin((startX * 0.02 + waveOffset).toDouble()).toFloat() * waveAmplitude
            val endY = borderThickness + Math.sin((endX * 0.02 + waveOffset).toDouble()).toFloat() * waveAmplitude
            drawLine(
                brush = borderBrush,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = borderThickness * 1.5f
            )
        }

        // Draw bottom wave border
        for (i in 0 until wavePointsCount) {
            val startX = i * segmentWidth
            val endX = (i + 1) * segmentWidth
            val startY = height - borderThickness + Math.cos((startX * 0.02 + waveOffset).toDouble()).toFloat() * waveAmplitude
            val endY = height - borderThickness + Math.cos((endX * 0.02 + waveOffset).toDouble()).toFloat() * waveAmplitude
            drawLine(
                brush = borderBrush,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = borderThickness * 1.5f
            )
        }

        // Draw left side wave border glow
        drawLine(
            brush = borderBrush,
            start = Offset(borderThickness / 2f, 0f),
            end = Offset(borderThickness / 2f, height),
            strokeWidth = borderThickness
        )

        // Draw right side wave border glow
        drawLine(
            brush = borderBrush,
            start = Offset(width - borderThickness / 2f, 0f),
            end = Offset(width - borderThickness / 2f, height),
            strokeWidth = borderThickness
        )

        // 2. Renders vertical cascading RainDroplets falling down left and right borders
        rainDrops.forEach { drop ->
            val positionX = if (drop.isLeft) {
                borderThickness + drop.x
            } else {
                width - borderThickness - drop.x
            }

            // Draw droplet body
            drawLine(
                color = lightAqua.copy(alpha = 0.8f),
                start = Offset(positionX, drop.y),
                end = Offset(positionX, drop.y + drop.length),
                strokeWidth = drop.size
            )

            // Draw droplet glowing splash splash splash
            drawCircle(
                color = waterCyan.copy(alpha = 0.5f),
                radius = drop.size * 1.4f,
                center = Offset(positionX, drop.y + drop.length)
            )
        }
    }
}

private val SineToSineEasing = Easing { fraction ->
    Math.sin(fraction * Math.PI - Math.PI / 2f).toFloat() / 2f + 0.5f
}
