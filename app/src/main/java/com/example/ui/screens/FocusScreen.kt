package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FocusViewModel
import com.example.ui.FocusSoundscape
import androidx.compose.foundation.border
import java.util.Locale
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = viewModel(factory = FocusViewModel.Factory),
    onNavigateBack: () -> Unit = {}
) {
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val timerDurationTotalSeconds by viewModel.timerDurationTotalSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val sessionType by viewModel.sessionType.collectAsStateWithLifecycle()
    val selectedSoundscape by viewModel.selectedSoundscape.collectAsStateWithLifecycle()
    val isMediaPlaying by viewModel.isMediaPlaying.collectAsStateWithLifecycle()
    val totalFocusMins by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()

    // Circular progress fraction
    val progressFraction = if (timerDurationTotalSeconds > 0) {
        timerSecondsRemaining.toFloat() / timerDurationTotalSeconds.toFloat()
    } else 0f

    // Format remaining time as MM:SS safely
    val minutesVal = timerSecondsRemaining / 60
    val secondsVal = timerSecondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutesVal, secondsVal)

    // Cosmic Theme Specific Colors
    val cosmicAccent = Color(0xFF64FFDA) // Glowing Cyan-Teal
    val cosmicSoftTeal = Color(0xFF99CDD8)
    val cosmicBackground = Color(0xFF111413)
    val cosmicPanel = Color(0xFF1B201D)
    val cosmicMuted = Color(0xFF9EA3A0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(cosmicBackground, Color(0xFF070908))
                )
            )
            .statusBarsPadding()
            .testTag("focus_hub_screen")
    ) {
        // 1. Subtle Background Pulsing Ambient Light behind timer
        val breathingTransition = rememberInfiniteTransition(label = "breathing")
        val glowScale by breathingTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowing_aura"
        )

        val glowAlpha by breathingTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowing_alpha"
        )

        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            cosmicAccent.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main distraction-free scrollable Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Immersive Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(cosmicPanel, CircleShape)
                        .testTag("focus_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "DEEP FOCUS HUB",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    ),
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(cosmicPanel, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "Cosmic Mind",
                        tint = cosmicAccent
                    )
                }
            }

            // Quick Interval Segment Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cosmicPanel)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val configOptions = listOf(
                    Triple(25, "Focus", "Focus 25m"),
                    Triple(50, "Focus", "Deep Focus 50m"),
                    Triple(5, "Break", "Short Break 5m"),
                    Triple(15, "Break", "Long Break 15m")
                )

                configOptions.forEach { (mins, type, titleLabel) ->
                    val isSelected = timerDurationTotalSeconds == mins * 60 && sessionType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) cosmicAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) cosmicAccent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setTimerConfiguration(mins, type) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = titleLabel.substringBefore(" "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) cosmicAccent else Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Beautiful Fluid Circular Progress Timer
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .testTag("pomodoro_circular_timer"),
                contentAlignment = Alignment.Center
            ) {
                // Background track circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val strokeWidth = 14.dp.toPx()

                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius - strokeWidth / 2f,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                }

                // Fluid active progressive countdown curve
                val outerSwipeProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(500, easing = LinearOutSlowInEasing),
                    label = "circular_glow_pulse"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val dimensions = size.minDimension - strokeWidth
                    val centerOffset = strokeWidth / 2f

                    // Draw glowing progressive gradient arc
                    drawArc(
                         brush = Brush.sweepGradient(
                             colors = listOf(cosmicSoftTeal, cosmicAccent, cosmicSoftTeal)
                         ),
                         startAngle = 270f,
                         sweepAngle = -360f * outerSwipeProgress,
                         useCenter = false,
                         style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                         topLeft = Offset(centerOffset, centerOffset),
                         size = Size(dimensions, dimensions)
                    )
                }

                // Inner immersive timer display and type details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = sessionType.uppercase(Locale.getDefault()),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = cosmicAccent
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeFormatted,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = cosmicAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedSoundscape.name,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Simple Distraction-free Controls (Pause / Start / Surrender)
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surrender (Stop) Button
                IconButton(
                    onClick = { viewModel.stopOrSurrenderTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .background(cosmicPanel, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .testTag("surrender_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop focusing (Surrender)",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Main Play/Pause Focus Button
                Button(
                    onClick = {
                        if (isTimerRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(min = 140.dp)
                        .testTag("toggle_timer_playback_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) Color.White else cosmicAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(32.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "StartFocusing",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isTimerRunning) "PAUSE" else "FOCUS NOW",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Skip / Reset configurations trigger
                IconButton(
                    onClick = { viewModel.toggleMediaPlayback() },
                    modifier = Modifier
                        .size(52.dp)
                        .background(cosmicPanel, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .testTag("toggle_audio_stream_button")
                ) {
                    Icon(
                        imageVector = if (isMediaPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Ambient Playback",
                        tint = if (isMediaPlaying) cosmicAccent else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Media session controls (rewind, fast forward)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(cosmicPanel)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.rewindMedia() },
                    modifier = Modifier.testTag("rewind_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind background ambient Stream",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Ambient Soundscape Controls",
                    fontSize = 11.sp,
                    color = cosmicMuted,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.forwardMedia() },
                    modifier = Modifier.testTag("forward_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Fast forward background ambient Stream",
                        tint = Color.White
                    )
                }
            }

            // Stat panel in Hub
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cosmicPanel)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(cosmicAccent.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = cosmicAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Total Focus Achievement",
                        fontSize = 11.sp,
                        color = cosmicMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$totalFocusMins Deep Focus Minutes Logged",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 4. Immersive Ambient Soundscapes Selection Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SELECT BACKGROUND SOUNDSCAPE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(viewModel.soundscapes) { soundscape ->
                        val isSelected = selectedSoundscape.id == soundscape.id
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) cosmicAccent.copy(alpha = 0.15f) else cosmicPanel)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) cosmicAccent else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectSoundscape(soundscape) }
                                .padding(16.dp)
                                .testTag("soundscape_${soundscape.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = soundscape.icon,
                                    fontSize = 28.sp
                                )

                                Text(
                                    text = soundscape.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) cosmicAccent else Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
