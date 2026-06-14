package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
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
import com.example.ui.ProfileViewModel
import com.example.ui.theme.*
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileChallengesScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val checkInCount by viewModel.checkInCount.collectAsStateWithLifecycle()
    val completedSessions by viewModel.completedBreathingSessions.collectAsStateWithLifecycle()

    // Determine badge states dynamically based on user logs
    val badges = remember(streakCount, checkInCount, completedSessions) {
        listOf(
            BadgeData(
                id = "first_check_in",
                title = "First Steps",
                description = "Logged your first mindful check-in.",
                icon = Icons.Default.DoneAll,
                isUnlocked = checkInCount >= 1,
                unlockedColor = BentoAccentGreen
            ),
            BadgeData(
                id = "three_day_streak",
                title = "Streak Tribe",
                description = "Maintained a solid 3-day streak.",
                icon = Icons.Default.LocalFireDepartment,
                isUnlocked = streakCount >= 3,
                unlockedColor = Color(0xFFFFD166) // Soft Amber Glow
            ),
            BadgeData(
                id = "deep_breather",
                title = "Ocean Lungs",
                description = "Completed at least one breath session.",
                icon = Icons.Default.Spa,
                isUnlocked = completedSessions >= 1,
                unlockedColor = Color(0xFF83C5BE) // Teal Blue-Green
            ),
            BadgeData(
                id = "zen_master",
                title = "Zen Master",
                description = "Achieved a legendary 7-day streak.",
                icon = Icons.Default.WorkspacePremium,
                isUnlocked = streakCount >= 7,
                unlockedColor = Color(0xFFEF476F) // Rich Soft Red Premium
            )
        )
    }

    val unlockedCount = remember(badges) { badges.count { it.isUnlocked } }
    var previousUnlockedCount by remember { mutableStateOf<Int?>(null) }
    var triggerConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(unlockedCount) {
        if (previousUnlockedCount != null && unlockedCount > previousUnlockedCount!!) {
            triggerConfetti = true
        }
        previousUnlockedCount = unlockedCount
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Progress Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_challenges_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = BentoTextDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_challenges_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
                .padding(horizontal = 24.dp)
                .testTag("profile_challenges_container"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Prominent Streak Display Bento Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_display_card"),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Pulsing Flame Container
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (streakCount > 0) {
                                        listOf(Color(0xFFFFEECC), Color(0xFFFFCC88))
                                    } else {
                                        listOf(BentoAccentGreen.copy(alpha = 0.5f), BentoBg)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (streakCount > 0) Icons.Default.LocalFireDepartment else Icons.Default.SelfImprovement,
                            contentDescription = "Flame spark",
                            tint = if (streakCount > 0) Color(0xFFF77F00) else BentoPrimaryGreenText,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${streakCount}-Day Streak",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            ),
                            color = BentoTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                streakCount >= 7 -> "Legendary focus! You are a certified Zen Master."
                                streakCount >= 3 -> "Solid continuity. Keep the momentum going!"
                                streakCount > 0 -> "Awesome check-in today. Let's make it tomorrow too."
                                else -> "Check in daily with moods or journals to ignite your streak!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section: Active Challenges
            Text(
                text = "Active Challenges",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BentoTextDark,
                modifier = Modifier.padding(top = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("challenge_container_card"),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoAccentGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = BentoPrimaryGreenText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "7-Day Mindful Breathing",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                                Text(
                                    text = "Deep relaxation practice daily",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoTextMuted
                                )
                            }
                        }

                        Text(
                            text = "$completedSessions/7",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoPrimaryGreenText
                        )
                    }

                    // Progress bar
                    val rawProgress = completedSessions.toFloat() / 7f
                    val animatedProgress by animateFloatAsState(
                        targetValue = rawProgress.coerceIn(0f, 1f),
                        animationSpec = tween(1200, easing = FastOutSlowInEasing),
                        label = "challenge_progress"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .testTag("challenge_progress_bar"),
                            color = BentoPrimaryGreenText,
                            trackColor = BentoAccentGreen.copy(alpha = 0.3f),
                        )
                        Text(
                            text = if (completedSessions >= 7) {
                                "Challenge fully unlocked! Congratulations 🎉"
                            } else {
                                "Complete ${7 - completedSessions} more breathing exercises to claim reward."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section: Unlocked Badges (Grid layout)
            Text(
                text = "Earned Achievements",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BentoTextDark,
                modifier = Modifier.padding(top = 4.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("badges_grid_view"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(badges) { badge ->
                    BadgeGridItem(
                        badge = badge,
                        onClick = {
                            if (badge.isUnlocked) {
                                triggerConfetti = true
                            }
                        }
                    )
                }
            }
        }
    }
    }

    if (triggerConfetti) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.example.R.raw.confetti))
        val lottieState = animateLottieCompositionAsState(
            composition = composition,
            isPlaying = true,
            iterations = 1,
            restartOnPlay = true
        )

        LottieAnimation(
            composition = composition,
            progress = { lottieState.progress },
            modifier = Modifier
                .fillMaxSize()
                .testTag("confetti_overlay")
        )

        if (lottieState.progress >= 1.0f) {
            triggerConfetti = false
        }
    }
}

@Composable
fun BadgeGridItem(badge: BadgeData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = badge.isUnlocked) { onClick() }
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (badge.isUnlocked) badge.unlockedColor.copy(alpha = 0.5f) else BentoCardBorder),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badge light bulb / illustration circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            badge.unlockedColor.copy(alpha = 0.15f)
                        } else {
                            BentoBg.copy(alpha = 0.4f)
                        }
                    )
                    .border(
                        1.dp,
                        if (badge.isUnlocked) badge.unlockedColor.copy(alpha = 0.4f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) badge.unlockedColor else Color.LightGray,
                    modifier = Modifier
                        .size(28.dp)
                        .alpha(if (badge.isUnlocked) 1f else 0.5f)
                )
            }

            // Description and Title details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (badge.isUnlocked) BentoTextDark else BentoTextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    maxLines = 2
                )
            }

            // Unlock tag details
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (badge.isUnlocked) {
                            BentoAccentGreen.copy(alpha = 0.5f)
                        } else {
                            BentoBg.copy(alpha = 0.5f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) BentoPrimaryGreenText else Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

data class BadgeData(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val unlockedColor: Color
)
