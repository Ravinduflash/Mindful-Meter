package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.ui.RoutineViewModel
import com.example.ui.BreathingState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val context = LocalContext.current

    // Observe step 1 (Mood)
    val selectedMood by viewModel.selectedMood.collectAsStateWithLifecycle()
    val moodNote by viewModel.moodNote.collectAsStateWithLifecycle()

    // Observe step 2 (Breathing)
    val isRunning by viewModel.isBreathingRunning.collectAsStateWithLifecycle()
    val breathingTotalSeconds by viewModel.breathingTotalSecondsElapsed.collectAsStateWithLifecycle()
    val currentPhaseIndex by viewModel.currentPhaseIndex.collectAsStateWithLifecycle()
    val phaseSecondsElapsed by viewModel.phaseSecondsElapsed.collectAsStateWithLifecycle()

    // Observe step 3 (Intentions)
    val intention1 by viewModel.intention1.collectAsStateWithLifecycle()
    val intention2 by viewModel.intention2.collectAsStateWithLifecycle()
    val intention3 by viewModel.intention3.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Morning Focus Routine",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("routine_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Abort Routine",
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
        ) {
            // STEP PROGRESS INDICATOR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step header text
                val stepTitle = when (pagerState.currentPage) {
                    0 -> "Step 1 of 3: Mood Check-in ☀️"
                    1 -> "Step 2 of 3: Deep Box Breath 🧘"
                    else -> "Step 3 of 3: Daily Intentions 🎯"
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoPrimaryGreenText
                    )
                    Text(
                        text = "${((pagerState.currentPage + 1) * 33.3f).toInt()}% Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoTextMuted
                    )
                }

                // Custom segmented progress capsules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0..2) {
                        val isCurrentOrCompleted = i <= pagerState.currentPage
                        val alpha = if (isCurrentOrCompleted) 1f else 0.25f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BentoPrimaryGreenText.copy(alpha = alpha))
                        )
                    }
                }
            }

            // SEGMENTED HORIZONTAL PAGER CONTROLLER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("routine_pager")
            ) { page ->
                when (page) {
                    0 -> {
                        MoodStepPage(
                            selectedMood = selectedMood,
                            moodNote = moodNote,
                            onMoodSelected = { viewModel.selectMood(it) },
                            onNoteChanged = { viewModel.setMoodNote(it) }
                        )
                    }
                    1 -> {
                        BreathingStepPage(
                            viewModel = viewModel,
                            isRunning = isRunning,
                            totalSecondsElapsed = breathingTotalSeconds,
                            currentPhaseIndex = currentPhaseIndex,
                            phaseSecondsElapsed = phaseSecondsElapsed
                        )
                    }
                    2 -> {
                        IntentionsStepPage(
                            intention1 = intention1,
                            intention2 = intention2,
                            intention3 = intention3,
                            onIntention1Changed = { viewModel.setIntention1(it) },
                            onIntention2Changed = { viewModel.setIntention2(it) },
                            onIntention3Changed = { viewModel.setIntention3(it) }
                        )
                    }
                }
            }

            // NAVIGATION BUTTON BAR (BOTTOM)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BentoNavBg,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous step trigger (always touch target 48dp)
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("routine_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Previous", fontWeight = FontWeight.Bold)
                    }

                    // Next / Complete step trigger (always touch target 48dp)
                    if (pagerState.currentPage < 2) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryGreenText),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("routine_next_step_button")
                        ) {
                            Text("Next Step", fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeRoutine {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Morning Routine Completed! 🌟 Have a mindful day.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    onComplete()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryGreenText),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("routine_complete_routine_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Complete Routine", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodStepPage(
    selectedMood: String,
    moodNote: String,
    onMoodSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit
) {
    val moods = listOf(
        RoutineMoodItem("Happy", "😊", Color(0xFFF9E8B6), Color(0xFFE6A11D)),
        RoutineMoodItem("Calm", "😌", Color(0xFFC0E5DF), Color(0xFF1B8A7A)),
        RoutineMoodItem("Stressed", "😬", Color(0xFFFAD1C5), Color(0xFFD43B1A)),
        RoutineMoodItem("Anxious", "😰", Color(0xFFCFE5FC), Color(0xFF2365A8)),
        RoutineMoodItem("Sad", "😢", Color(0xFFD2D2ED), Color(0xFF4C439E))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Welcome to your Check-in",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = BentoTextDark
            )
            Text(
                text = "How is your general state of mind this morning? Take a pure moment to reflect honestly.",
                style = MaterialTheme.typography.bodyMedium,
                color = BentoTextMuted
            )
        }

        // Horizontal selections card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select your ambient mood",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    moods.forEach { item ->
                        val isSelected = item.name == selectedMood

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("routine_mood_${item.name}")
                                .clickable { onMoodSelected(item.name) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) item.bgSelected else item.bgSelected.copy(alpha = 0.2f))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) item.accentColor else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.emoji,
                                    fontSize = 28.sp
                                )
                            }

                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) item.accentColor else BentoTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Peaceful thoughts notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Morning reflection notes (Optional)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )

                OutlinedTextField(
                    value = moodNote,
                    onValueChange = onNoteChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("routine_mood_note"),
                    placeholder = {
                        Text(
                            text = "Write down any thoughts, dreams to remember, or anything contributing to your current mood...",
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimaryGreenText,
                        unfocusedBorderColor = BentoCardBorder
                    )
                )
            }
        }
    }
}

@Composable
fun BreathingStepPage(
    viewModel: RoutineViewModel,
    isRunning: Boolean,
    totalSecondsElapsed: Int,
    currentPhaseIndex: Int,
    phaseSecondsElapsed: Int
) {
    val currentPhase = viewModel.breathingSequence[currentPhaseIndex.coerceIn(0, 3)]
    val secondsRemaining = currentPhase.durationSeconds - phaseSecondsElapsed
    val totalSecondsRemaining = (60 - totalSecondsElapsed).coerceAtLeast(0)

    // Animation values for breathing scale
    val phaseProgress = phaseSecondsElapsed.toFloat() / currentPhase.durationSeconds.toFloat()
    val targetScale = when (currentPhase.phaseName) {
        "Inhale" -> 0.4f + 0.6f * phaseProgress
        "Hold" -> if (currentPhaseIndex == 1) 1.0f else 0.4f
        "Exhale" -> 1.0f - 0.6f * phaseProgress
        else -> 0.4f // Hold Empty
    }

    val animScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Central Bubble Scale"
    )

    val backgroundPulseColor by animateColorAsState(
        targetValue = when (currentPhase.phaseName) {
            "Inhale" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            "Hold" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            "Exhale" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(1000),
        label = "Lively background pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step description card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BentoPrimaryGreenText
                )
                Text(
                    text = "A simple 1-minute deep breathing exercise settles stress hormones, focuses brainwaves, and structures morning calmness.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoPrimaryGreenText
                )
            }
        }

        if (totalSecondsElapsed >= 60) {
            // FINISHED CELEBRATION BUBBLE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(BentoAccentGreen.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = "Breathing Completed",
                            tint = BentoPrimaryGreenText,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Text(
                        text = "Breathing Session Checked! 🧘",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = BentoPrimaryGreenText
                    )
                    Text(
                        text = "You survived 60 seconds of box tuning. Your mind has adjusted. Swipe or tap Next.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = BentoTextMuted,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            // ACTIVE BREATHING SYSTEM
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background dynamic pulse
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(backgroundPulseColor)
                )

                // Concentric dashed outer circle decoration
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        color = backgroundPulseColor.copy(alpha = 0.6f),
                        radius = size.minDimension / 2,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(15f, 12f), 0f
                            )
                        )
                    )
                }

                // Breathing visual circular bubble layer
                val lottieComposeRes = com.example.R.raw.breathing
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieComposeRes))
                
                if (composition != null) {
                    // Normalize lottie progress with sequential phases
                    val frac = phaseSecondsElapsed.toFloat() / currentPhase.durationSeconds.toFloat()
                    val lottieAnimatedProgress = when (currentPhase.phaseName) {
                        "Inhale" -> 0.0f + 0.5f * frac
                        "Hold" -> 0.5f
                        "Exhale" -> 0.5f + 0.5f * frac
                        else -> 0.0f
                    }
                    
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieAnimatedProgress },
                        modifier = Modifier
                            .size(200.dp)
                            .testTag("routine_breathing_circle")
                    )
                } else {
                    // Fallback to pure high-fidelity scale-animated vector
                    Box(
                        modifier = Modifier
                            .graphicsLayer(scaleX = animScale, scaleY = animScale)
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(BentoPrimaryGreenText.copy(alpha = 0.45f))
                            .border(3.dp, BentoPrimaryGreenText, CircleShape)
                    )
                }

                // Internal text progress values
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isRunning) secondsRemaining.toString() else "Ready",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoTextDark
                        )
                    )
                    Text(
                        text = currentPhase.phaseName.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = BentoTextDark.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // Prompt instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isRunning) currentPhase.instructionText else "Ready to align? Tap Begin below.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Time Remaining of Exercise: $totalSecondsRemaining seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )
            }

            // Circular action bar buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.resetBreathing() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("routine_breathing_reset")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Timer")
                }

                Button(
                    onClick = {
                        if (isRunning) viewModel.pauseBreathing() else viewModel.startBreathing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryGreenText),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .width(140.dp)
                        .testTag("routine_breathing_toggle")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(if (isRunning) "Pause" else "Begin")
                }
            }
        }
    }
}

@Composable
fun IntentionsStepPage(
    intention1: String,
    intention2: String,
    intention3: String,
    onIntention1Changed: (String) -> Unit,
    onIntention2Changed: (String) -> Unit,
    onIntention3Changed: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Structure Today's Focus",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = BentoTextDark
            )
            Text(
                text = "Set exactly three small, specific intents you commit to accomplish layout today. Small actions yield outstanding mental streaks.",
                style = MaterialTheme.typography.bodyMedium,
                color = BentoTextMuted
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Commitments for Today",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )

                // Input 1
                OutlinedTextField(
                    value = intention1,
                    onValueChange = onIntention1Changed,
                    label = { Text("Intention 1", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Drink 1L of room temperature water", fontSize = 13.sp) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_goal_input_1"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimaryGreenText,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    singleLine = true
                )

                // Input 2
                OutlinedTextField(
                    value = intention2,
                    onValueChange = onIntention2Changed,
                    label = { Text("Intention 2", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Read 5 pages of book during tram ride", fontSize = 13.sp) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_goal_input_2"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimaryGreenText,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    singleLine = true
                )

                // Input 3
                OutlinedTextField(
                    value = intention3,
                    onValueChange = onIntention3Changed,
                    label = { Text("Intention 3", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Send thank you letter to client", fontSize = 13.sp) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_goal_input_3"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimaryGreenText,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    singleLine = true
                )
            }
        }
    }
}

data class RoutineMoodItem(
    val name: String,
    val emoji: String,
    val bgSelected: Color,
    val accentColor: Color
)
