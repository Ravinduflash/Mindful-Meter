package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SleepViewModel
import com.example.ui.Soundscape
import com.example.ui.theme.*

// Sleep theme specific deep midnight colors
private val SleepBg = Color(0xFF090D1A)
private val SleepSurface = Color(0xFF121829)
private val SleepCardActive = Color(0xFF1E294B)
private val SleepTextMuted = Color(0xFF8E9BB5)
private val SleepAccentGold = Color(0xFFE2C044)
private val SleepAccentSky = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onNavigateBack: () -> Unit
) {
    val soundscapes by viewModel.soundscapes.collectAsStateWithLifecycle()
    val playingId by viewModel.playingId.collectAsStateWithLifecycle()
    val timerOption by viewModel.timerOption.collectAsStateWithLifecycle()
    val secondsRemaining by viewModel.secondsRemaining.collectAsStateWithLifecycle()

    val countsDown = secondsRemaining > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sleep Soundscapes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sleep_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = SleepBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .testTag("sleep_screen_container"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Sound Status Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sleep_timer_panel"),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                colors = CardDefaults.cardColors(containerColor = SleepSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val activeSound = soundscapes.find { it.id == playingId }

                    if (activeSound != null) {
                        // High fidelity indicator of playback
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(SleepAccentSky),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }

                            Text(
                                text = "Playing: ${activeSound.title}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        // Countdown formatting
                        if (countsDown) {
                            val minutes = secondsRemaining / 60
                            val seconds = secondsRemaining % 60
                            val formattedTime = String.format("%02d:%02d", minutes, seconds)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = SleepAccentGold,
                                    modifier = Modifier.testTag("timer_countdown_text")
                                )
                                Text(
                                    text = "Auto-stops ambient session",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleepTextMuted
                                )
                            }
                        } else {
                            Text(
                                text = "Continuous simulated stream playback (Timer Off)",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleepTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { viewModel.stopPlayback() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("stop_all_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Playback", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                    } else {
                        // Sleep idle placeholder
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = SleepAccentGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "A calming atmosphere awaits",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Select an ambient soundscape below and set a bedtime sleep timer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleepTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Sleep Timer Duration Selector Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bedtime Sleep Timer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_pills_container"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val options = listOf("Off", "15m", "30m", "1h")
                    options.forEach { option ->
                        val isSelected = timerOption == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) SleepAccentGold else SleepSurface)
                                .clickable { viewModel.setTimerOption(option) }
                                .testTag("timer_pill_$option"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SleepBg else Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ambient Soundscapes Grid Title
            Text(
                text = "Ambient Soundscapes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            // Dynamic grid layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("soundscapes_grid"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(soundscapes) { sound ->
                    val isPlaying = sound.id == playingId
                    SoundscapeGridItem(
                        sound = sound,
                        isPlaying = isPlaying,
                        onClick = { viewModel.togglePlaying(sound.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SoundscapeGridItem(
    sound: Soundscape,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("soundscape_card_${sound.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPlaying) SleepAccentSky.copy(alpha = borderAlpha) else Color(0xFF1E293B)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) SleepCardActive else SleepSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) SleepAccentSky.copy(alpha = 0.15f) else Color(0xFF1E293B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = when (sound.iconName) {
                    "rain" -> Icons.Default.WaterDrop
                    "waves" -> Icons.Default.Waves
                    "noise" -> Icons.Default.GraphicEq
                    "fire" -> Icons.Default.LocalFireDepartment
                    else -> Icons.Default.MusicNote
                }

                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isPlaying) SleepAccentSky else SleepTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sound.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = sound.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = SleepTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    maxLines = 2
                )
            }

            // Small play/pause indicator pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPlaying) SleepAccentSky.copy(alpha = 0.2f) else Color(0xFF1E293B)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) SleepAccentSky else SleepTextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = if (isPlaying) "PLAYING" else "IDLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) SleepAccentSky else SleepTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
