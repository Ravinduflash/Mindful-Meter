package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    onNavigateBack: () -> Unit,
    onSessionCompleted: () -> Unit = {}
) {
    // Breathing options: Box Breathing (4-4-4-4) vs. Relaxing Breathing (4-7-8)
    var isBoxBreathingMode by remember { mutableStateOf(true) }
    
    // Timer & cycle values
    val currentSequence = remember(isBoxBreathingMode) {
        if (isBoxBreathingMode) {
            listOf(
                BreathingState("Inhale", 4, "Breathe in slowly through your nose"),
                BreathingState("Hold", 4, "Retain your breath gently"),
                BreathingState("Exhale", 4, "Release the air fully through your mouth"),
                BreathingState("Hold", 4, "Pause in stillness with lungs empty")
            )
        } else {
            listOf(
                BreathingState("Inhale", 4, "Breathe in through nose"),
                BreathingState("Hold", 7, "Hold the deep, vital life force"),
                BreathingState("Exhale", 8, "Sigh and exhale through your mouth release")
            )
        }
    }

    var isRunning by remember { mutableStateOf(false) }
    var currentPhaseIndex by remember { mutableStateOf(0) }
    var secondsElapsedInPhase by remember { mutableStateOf(0) }

    val currentPhase = currentSequence[currentPhaseIndex]
    val secondsRemaining = currentPhase.durationSeconds - secondsElapsedInPhase

    // Sound alert or vibration trigger simulation state
    var triggerPulseState by remember { mutableStateOf(false) }

    // Reset loop if sequence type is toggled
    LaunchedEffect(isBoxBreathingMode) {
        isRunning = false
        currentPhaseIndex = 0
        secondsElapsedInPhase = 0
    }

    // Main timer coroutine
    LaunchedEffect(isRunning, currentPhaseIndex, secondsElapsedInPhase) {
        if (isRunning) {
            delay(1000)
            val currentMaxDuration = currentSequence[currentPhaseIndex].durationSeconds
            if (secondsElapsedInPhase + 1 >= currentMaxDuration) {
                // Advance to next phase
                triggerPulseState = true
                delay(100)
                triggerPulseState = false
                
                // If we are at the last index, the user completed a full cycle
                if (currentPhaseIndex == currentSequence.size - 1) {
                    onSessionCompleted()
                }
                
                currentPhaseIndex = (currentPhaseIndex + 1) % currentSequence.size
                secondsElapsedInPhase = 0
            } else {
                secondsElapsedInPhase += 1
            }
        }
    }

    // Animation values for breathing scale
    // We compute a normalized progress (0.0 to 1.0) of how complete the current phase is.
    val phaseProgress = secondsElapsedInPhase.toFloat() / currentPhase.durationSeconds.toFloat()
    
    val targetScale = remember(currentPhase.phaseName, phaseProgress) {
        when (currentPhase.phaseName) {
            "Inhale" -> 0.4f + 0.6f * phaseProgress
            "Hold" -> if (currentPhaseIndex == 1) 1.0f else 0.4f
            "Exhale" -> 1.0f - 0.6f * phaseProgress
            else -> 0.4f // Hold Empty
        }
    }

    // Smoothly animate between scales
    val animScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Breathing Circle Scale"
    )

    // Cosmic pulse effect when transition of state happens
    val backgroundPulseColor by animateColorAsState(
        targetValue = when (currentPhase.phaseName) {
            "Inhale" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            "Hold" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            "Exhale" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(1000),
        label = "Aesthetic background colors"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vibrant Breathwork",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("breathing_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Description of what this exercise achieves
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBoxBreathingMode) {
                            "Box Breathing: Calms the nervous system, decreases stress, and improves concentration. Standard 4-4-4-4 cycle used by special forces."
                        } else {
                            "4-7-8 Breathing: A natural tranquilizer for the nervous system, highly effective for anxiety relief and falling asleep."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Selector tabs: Box Breathing vs. 4-7-8
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isBoxBreathingMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isBoxBreathingMode = true }
                        .testTag("tab_box_breathing"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Box Breathing (4s)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isBoxBreathingMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isBoxBreathingMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isBoxBreathingMode = false }
                        .testTag("tab_478_breathing"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Relax Breathing (4-7-8)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (!isBoxBreathingMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Immersive visualizer block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background glowing dynamic layer
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .clip(RoundedCornerShape(155.dp))
                        .background(backgroundPulseColor)
                )

                // Outer decorative concentric circle (dashed)
                Canvas(modifier = Modifier.size(240.dp)) {
                    drawCircle(
                        color = backgroundPulseColor.copy(alpha = 0.6f),
                        radius = size.minDimension / 2,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(20f, 15f), 0f
                            )
                        )
                    )
                }

                // Breathing interactive central bubble circle
                val bubbleColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .testTag("breathing_circle")
                ) {
                    // Circle size is dynamically driven by smoothly calculated animScale
                    val drawRadius = (size.minDimension / 2) * animScale
                    
                    // Draw soft glowing translucent backdrop
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                bubbleColor.copy(alpha = 0.7f),
                                secondaryColor.copy(alpha = 0.1f)
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = drawRadius * 1.1f
                        ),
                        radius = drawRadius * 1.1f
                    )

                    // Draw solid central interactive bubble
                    drawCircle(
                        color = bubbleColor,
                        radius = drawRadius
                    )
                }

                // Timer numbers inside the circular dynamic visualizer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isRunning) secondsRemaining.toString() else "Ready",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        text = currentPhase.phaseName.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            // Prompt direction text below the bubble
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = if (isRunning) currentPhase.instructionText else "Press Play to begin of your mental tuning",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action interactive controls bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset (touch targets 48dp)
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        currentPhaseIndex = 0
                        secondsElapsedInPhase = 0
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("breathing_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset exercise",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Play / Pause main button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .height(64.dp)
                        .width(160.dp)
                        .testTag("breathing_toggle_btn"),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isRunning) "Pause" else "Begin",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

data class BreathingState(
    val phaseName: String,
    val durationSeconds: Int,
    val instructionText: String
)
