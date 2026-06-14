package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// Beautiful water/rain active border colors
val WaterBluePrimary = Color(0xFF2196F3)
val WaterBlueAccent = Color(0xFF00E5FF)
val WaterBlueLight = Color(0xFFE0F7FA)

/**
 * A beautiful screen border overlay with cascading rain lines down the sides
 * and glowing bubbles floating up from the bottom.
 */
@Composable
fun RainWaterBorderAnimation(
    isActive: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    // Dismiss automatically after 7 seconds if the user does not tap
    LaunchedEffect(Unit) {
        delay(7000)
        onDismiss()
    }

    // Infinite transition for fluid animations
    val infiniteTransition = rememberInfiniteTransition(label = "water_flow")
    
    // Wave/pulsing alpha of the screen border outline
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_glow"
    )

    // Running animation time parameter (0.0f to 1.0f) for rain droplets & bubbles movement
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "droplets_flow"
    )

    // Generate constant state for bubble and rain particles so they don't jump on recomposition
    val particles = remember {
        List(25) {
            ParticleData(
                startXPercent = Random.nextFloat(),
                speedFactor = Random.nextFloat() * 0.6f + 0.5f,
                size = Random.nextFloat() * 8f + 4f,
                horizontalWobble = Random.nextFloat() * 20f - 10f
            )
        }
    }

    val rainDroplets = remember {
        List(30) {
            DropletData(
                isRightSide = Random.nextBoolean(),
                startYPercent = Random.nextFloat(),
                speedFactor = Random.nextFloat() * 0.8f + 0.6f,
                length = Random.nextFloat() * 30f + 20f,
                thickness = Random.nextFloat() * 2f + 1.5f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() } // Tap anywhere to dismiss
            .testTag("hydration_animation_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Draw Custom Canvas containing Border Glow, Rain Drops and Floating Bubbles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val borderPaddingPx = 12.dp.toPx()

            // A. Draw full-screen pulsing border highlight gradient
            val borderGlowBrush = Brush.radialGradient(
                colors = listOf(
                    WaterBlueAccent.copy(alpha = pulsingAlpha * 0.2f),
                    WaterBluePrimary.copy(alpha = pulsingAlpha * 0.4f),
                    Color.Transparent
                ),
                center = Offset(width / 2f, height / 2f),
                radius = width * 1.2f
            )
            drawRect(
                brush = borderGlowBrush,
                topLeft = Offset.Zero,
                size = size
            )

            // Draw clean border line
            drawRoundRect(
                color = WaterBluePrimary.copy(alpha = pulsingAlpha),
                topLeft = Offset(borderPaddingPx / 2, borderPaddingPx / 2),
                size = Size(width - borderPaddingPx, height - borderPaddingPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )

            // B. Draw Rising Water Bubbles from Bottom
            particles.forEach { particle ->
                // Calculate current Y coordinate: start from bottom and float upwards based on flowProgress
                val currentYFraction = (1.0f - (flowProgress * particle.speedFactor)) % 1.0f
                val yCoord = height - (currentYFraction * height)
                
                // Add minor horizontal wave/wobble for natural fluid buoyancy
                val waveOffset = kotlin.math.sin(flowProgress * Math.PI * 2f * particle.speedFactor).toFloat() * particle.horizontalWobble
                val xCoord = (particle.startXPercent * (width - 40.dp.toPx())) + 20.dp.toPx() + waveOffset

                // Bubbles fade out as they reach the top screen edge
                val bubbleAlpha = if (currentYFraction > 0.8f) {
                    (1.0f - currentYFraction) / 0.2f
                } else if (currentYFraction < 0.1f) {
                    currentYFraction / 0.1f
                } else {
                    1.0f
                }

                // Bubble fill color
                drawCircle(
                    color = WaterBlueAccent.copy(alpha = bubbleAlpha * 0.7f),
                    radius = particle.size,
                    center = Offset(xCoord, yCoord)
                )

                // Bubble outline shine ring
                drawCircle(
                    color = Color.White.copy(alpha = bubbleAlpha * 0.9f),
                    radius = particle.size + 1.dp.toPx(),
                    center = Offset(xCoord, yCoord),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // C. Draw Cascading Rain Drops down the side borders (Left and Right edges only)
            rainDroplets.forEach { droplet ->
                val currentX = if (droplet.isRightSide) {
                    width - borderPaddingPx - Random.nextFloat() * 15.dp.toPx()
                } else {
                    borderPaddingPx + Random.nextFloat() * 15.dp.toPx()
                }

                val currentYFraction = (droplet.startYPercent + (flowProgress * droplet.speedFactor)) % 1.0f
                val yTop = currentYFraction * height
                val yBottom = yTop + droplet.length

                val dropletAlpha = if (currentYFraction > 0.85f) {
                    (1.0f - currentYFraction) / 0.15f
                } else {
                    0.8f
                }

                drawLine(
                    color = WaterBluePrimary.copy(alpha = dropletAlpha * 0.8f),
                    start = Offset(currentX, yTop),
                    end = Offset(currentX, yBottom),
                    strokeWidth = droplet.thickness
                )
            }
        }

        // 2. Animated Center Card Content indicating Hydration Drink Time!
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                WaterBlueLight.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Spinning/pulsing direct water icon badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(WaterBluePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Drink Glass",
                        tint = WaterBluePrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Time to Sip! 💧",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "A quiet reminder from your Hydration Coach. Drinking water now helps replenish bodily energy and resets cellular stress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_hydration_animation")
                ) {
                    Text(
                        text = "I'm Hydrated! 💦",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Particle helper models for persistent animated states
private data class ParticleData(
    val startXPercent: Float,
    val speedFactor: Float,
    val size: Float,
    val horizontalWobble: Float
)

private data class DropletData(
    val isRightSide: Boolean,
    val startYPercent: Float,
    val speedFactor: Float,
    val length: Float,
    val thickness: Float
)
