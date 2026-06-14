package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MeditationViewModel
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    viewModel: MeditationViewModel,
    onNavigateBack: () -> Unit
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    // Sound waves animation based on isPlaying
    val infiniteTransition = rememberInfiniteTransition(label = "sound_waves")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )

    // Duration config (10 minutes total track = 600s)
    val totalSeconds = 600
    val elapsedSeconds = (progress * totalSeconds).roundToInt()

    fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%d:%02d", m, s)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Zen Meditation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("meditation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = BentoTextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BentoBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .testTag("meditation_content_container"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Cover/Art Bento Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing animated ambient backgrounds
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(BentoAccentGreen.copy(alpha = 0.4f * (2f - waveScale1)))
                        )
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(BentoAccentGreen.copy(alpha = 0.2f * (2.2f - waveScale1)))
                        )
                    }

                    // Main Cover Circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(BentoAccentGreen, BentoBreathingBg)
                                )
                            )
                            .border(2.dp, BentoAccentGreenBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = BentoPrimaryGreenText,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // Subtitle / Track details
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Morning Sunshine Zenith",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                        Text(
                            text = "Guided Session • Solfeggio 528Hz",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Controls & Player Panel Bento Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Time Readout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(elapsedSeconds),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoPrimaryGreenText
                        )
                        Text(
                            text = formatTime(totalSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = BentoTextMuted
                        )
                    }

                    // Interactive Progress seeking Slider
                    Slider(
                        value = progress,
                        onValueChange = { viewModel.setProgress(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("meditation_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = BentoPrimaryGreenText,
                            activeTrackColor = BentoPrimaryGreenText,
                            inactiveTrackColor = BentoCardBorder
                        )
                    )

                    // Control Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind (-10%)
                        IconButton(
                            onClick = { viewModel.rewind() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.dp, BentoCardBorder, CircleShape)
                                .testTag("meditation_rewind_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind",
                                tint = BentoTextDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Play/Pause Master Toggle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BentoPrimaryGreenText)
                                .clickable { viewModel.togglePlayPause() }
                                .testTag("meditation_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause session" else "Play session",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Fast Forward (+10%)
                        IconButton(
                            onClick = { viewModel.fastForward() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.dp, BentoCardBorder, CircleShape)
                                .testTag("meditation_ff_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Fast forward",
                                tint = BentoTextDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
