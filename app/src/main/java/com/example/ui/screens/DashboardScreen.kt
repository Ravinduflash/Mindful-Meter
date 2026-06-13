package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MoodLog
import com.example.ui.MoodViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.DailyIntentionViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MoodViewModel,
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    onNavigateToMood: () -> Unit,
    onNavigateToBreathing: () -> Unit,
    onNavigateToProfileChallenges: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMeditation: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToCoaching: () -> Unit,
    onNavigateToIntentions: () -> Unit
) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val isRemindersEnabled by settingsViewModel.isRemindersEnabled.collectAsStateWithLifecycle()

    // Determine welcoming greeting based on hour of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Rest well"
        }
    }

    var showQuickLogToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Dim the toast after a short delay
    LaunchedEffect(showQuickLogToast) {
        if (showQuickLogToast) {
            kotlinx.coroutines.delay(2000)
            showQuickLogToast = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.titleMedium,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "Mindful Explorer",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = BentoTextDark
                        )
                    }
                },
                actions = {
                    // Aesthetic Bento-style clickable avatar leading to Settings
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BentoAccentGreen)
                            .border(2.dp, BentoAccentGreenBorder, CircleShape)
                            .clickable(onClick = onNavigateToProfileChallenges)
                            .testTag("dashboard_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ME",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryGreenText
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BentoNavBg,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(1.dp, BentoCardBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .testTag("dashboard_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCommunity,
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Community") },
                    label = { Text("Social", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    ),
                    modifier = Modifier.testTag("nav_item_community")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCoaching,
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Coaching") },
                    label = { Text("Coach", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    ),
                    modifier = Modifier.testTag("nav_item_coaching")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToJournal,
                    icon = { Icon(Icons.Default.Book, contentDescription = "Journal") },
                    label = { Text("Journal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMeditation,
                    icon = { Icon(Icons.Default.Spa, contentDescription = "Meditate") },
                    label = { Text("Meditate", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Trends") },
                    label = { Text("Trends", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryGreenText,
                        unselectedIconColor = BentoTextMuted,
                        selectedTextColor = BentoPrimaryGreenText,
                        unselectedTextColor = BentoTextMuted,
                        indicatorColor = BentoAccentGreen
                    )
                )
            }
        },
        containerColor = BentoBg
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("dashboard_scroll_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECTION 1: Daily Mood Card (Large Bento Card)
                item {
                    BentoDailyMoodCard(
                        logs = logs,
                        onQuickLog = { mood ->
                            viewModel.logMood(mood, "Quick logged from Dashboard Bento Grid")
                            toastMessage = "Logged $mood successfully!"
                            showQuickLogToast = true
                        }
                    )
                }

                // SECTION 1.5: Daily Intentions Card (Bento Layout)
                item {
                    val dailyIntentionViewModel: DailyIntentionViewModel = viewModel(factory = DailyIntentionViewModel.Factory)
                    val intentions by dailyIntentionViewModel.currentIntentions.collectAsStateWithLifecycle()
                    val completedCount = intentions.count { it.isCompleted }
                    val totalCount = intentions.size

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToIntentions)
                            .testTag("dashboard_intentions_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BentoAccentGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = BentoPrimaryGreenText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Daily Intentions",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BentoTextDark
                                    )
                                }

                                if (intentions.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BentoAccentGreen)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$completedCount/$totalCount DONE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = BentoPrimaryGreenText
                                        )
                                    }
                                }
                            }

                            if (intentions.isEmpty()) {
                                Text(
                                    text = "Clarify your day. Set three personal morning goals to practice mindfulness and build active focus.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextMuted
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Set Goals Now",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = BentoPrimaryGreenText
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = BentoPrimaryGreenText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    intentions.forEach { intention ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    dailyIntentionViewModel.toggleIntentionCompletion(intention)
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (intention.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Toggle completion",
                                                tint = if (intention.isCompleted) BentoPrimaryGreenText else BentoTextMuted.copy(alpha = 0.5f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = intention.text,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (intention.isCompleted) BentoTextMuted else BentoTextDark,
                                                textDecoration = if (intention.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 2: Modular Sidebar-style Cards (Dual grid rows)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Bento: Breathing Exercise Card
                        BentoBreathingLauncher(
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToBreathing
                        )

                        // Right Bento: Trends Summary Card
                        BentoTrendsSummary(
                            modifier = Modifier.weight(1f),
                            logs = logs,
                            onClick = onNavigateToMood
                        )
                    }
                }

                // SECTION 2.5: Mindful Journal & Guided Meditation Launchers
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Bento: Journal Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable(onClick = onNavigateToJournal)
                                .testTag("quick_action_journal"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BentoCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BentoAccentGreen.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = BentoPrimaryGreenText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Mindful\nJournal",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp
                                        ),
                                        color = BentoTextDark
                                    )
                                    Text(
                                        text = "Write freely",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Right Bento: Guided Meditation Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable(onClick = onNavigateToMeditation)
                                .testTag("quick_action_meditation"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoAccentGreen)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = BentoPrimaryGreenText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Guided\nMeditation",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp
                                        ),
                                        color = BentoTextDark
                                    )
                                    Text(
                                        text = "Solfeggio Zen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoPrimaryGreenText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2.7: Sleep Hub and Content Library Launchers
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Bento: Sleep Hub Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable(onClick = onNavigateToSleep)
                                .testTag("quick_action_sleep"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Midnight Navy
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = Color(0xFFF1F5F9),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Mindful\nSleep",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Ambient Sounds",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Right Bento: Content Library Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable(onClick = onNavigateToLibrary)
                                .testTag("quick_action_library"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)), // Extremely light cream-yellow
                            border = BorderStroke(1.dp, Color(0xFFFDE047))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF854D0E), // Rich bronze text
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Mindful\nLibrary",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp
                                        ),
                                        color = Color(0xFF451A03)
                                    )
                                    Text(
                                        text = "Browse Guides",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF854D0E),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 3: Quick Mindful Reminders Row (Bento style strip)
                item {
                    BentoRemindersStrip(
                        isEnabled = isRemindersEnabled,
                        onToggle = { settingsViewModel.toggleReminders(it) }
                    )
                }

                // SECTION 4: Inspiring Quote Divider Row
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BentoCardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\"The present moment is the only time over which we have dominion.\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = BentoTextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // SECTION 5: Recent Logs / Empty state block
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Musings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                        if (logs.isNotEmpty()) {
                            Text(
                                text = "${logs.size} total entries",
                                style = MaterialTheme.typography.labelMedium,
                                color = BentoPrimaryGreenText
                            )
                        }
                    }
                }

                if (logs.isEmpty()) {
                    item {
                        EmptyLogsPlaceholder(onNavigateToMood = onNavigateToMood)
                    }
                } else {
                    items(
                        items = logs.take(6),
                        key = { it.id }
                    ) { log ->
                        MoodLogItem(
                            log = log,
                            onDelete = { viewModel.deleteLog(log.id) }
                        )
                    }
                }
            }

            // Quick log confirmation Toast notifier
            if (showQuickLogToast) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = BentoTextDark,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = toastMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoDailyMoodCard(
    logs: List<MoodLog>,
    onQuickLog: (String) -> Unit
) {
    // Dynamic mood analysis
    val primaryMood = remember(logs) {
        if (logs.isEmpty()) "Restful"
        else {
            logs.groupBy { it.mood }
                .maxByOrNull { it.value.size }?.key ?: "Peaceful"
        }
    }

    val explanatoryText = remember(primaryMood, logs.size) {
        if (logs.isEmpty()) {
            "No logs registered yet. A single mini-check-in starts a mindful habit."
        } else {
            when (primaryMood) {
                "Happy" -> "Your spirit feels luminous! Keep radiating positive frequencies."
                "Calm" -> "You are resting in a deep lake of peace. Breathe in this stillness."
                "Stressed" -> "Things look a little fast. Take a break & cycle deep breaths."
                "Anxious" -> "Your mind is racing. Sit steady—you are safe, supported, and okay."
                "Sad" -> "It is completely fine to feel low. Treat yourself gently today."
                else -> "Your mind is tuned beautifully. Keep checking in safely."
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trend_summary_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BentoAccentGreen),
        border = BorderStroke(1.dp, BentoAccentGreenBorder)
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
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Daily Mood",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                    Text(
                        text = "Active pattern: $primaryMood",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BentoPrimaryGreenText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = BentoPrimaryGreenText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = explanatoryText,
                style = MaterialTheme.typography.bodyMedium,
                color = BentoTextDark.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            // Horizontal interactive row of emojis (Like the Bento Design HTML)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap a face to check-in instantly:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BentoPrimaryGreenText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Triple("😊", "Happy", Color(0xFFF9E8B6)),
                        Triple("😌", "Calm", Color(0xFFC0E5DF)),
                        Triple("😬", "Stressed", Color(0xFFFAD1C5)),
                        Triple("😰", "Anxious", Color(0xFFCFE5FC)),
                        Triple("😢", "Sad", Color(0xFFD2D2ED))
                    ).forEach { (emoji, name, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { onQuickLog(name) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextDark,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoBreathingLauncher(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick)
            .testTag("quick_action_breathing"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BentoBreathingBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = BentoBreathingAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "Box\nBreathing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    ),
                    color = BentoTextDark
                )
                Text(
                    text = "4-7-8 Cycle",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoBreathingAccent,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun BentoTrendsSummary(
    modifier: Modifier = Modifier,
    logs: List<MoodLog>,
    onClick: () -> Unit
) {
    // Generate bars representing week trends
    val barHeights = remember(logs) {
        if (logs.isEmpty()) listOf(0.2f, 0.35f, 0.5f, 0.3f, 0.25f)
        else {
            val calendar = Calendar.getInstance()
            val dayCounts = IntArray(7) { 0 }
            logs.forEach { log ->
                calendar.timeInMillis = log.timestamp
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 to 7
                dayCounts[(dayOfWeek - 1) % 7] += 1
            }
            val maxCount = dayCounts.maxOrNull() ?: 1
            dayCounts.map { count ->
                if (maxCount == 0) 0.2f
                else (count.toFloat() / maxCount.toFloat()).coerceIn(0.2f, 1f)
            }.takeLast(5)
        }
    }

    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick)
            .testTag("quick_action_log_mood"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual miniature bar graph
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                barHeights.forEachIndexed { idx, weight ->
                    val barColor = if (idx == 2) BentoPrimaryGreenText else BentoAccentGreen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(weight)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor)
                    )
                }
            }

            Column {
                Text(
                    text = "Trends",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )
                Text(
                    text = if (logs.size > 2) "Consistent week" else "Build consistency",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BentoRemindersStrip(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminders_bento_strip"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoTextDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoPrimaryGreenText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Mindful Reminders",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Beautiful styled state Switch
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("reminders_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BentoPrimaryGreenText,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun MoodLogItem(
    log: MoodLog,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

    val moodConfig = remember(log.mood) {
        when (log.mood) {
            "Happy" -> Pair("😊", Color(0xFFF9E8B6))
            "Calm" -> Pair("😌", Color(0xFFC0E5DF))
            "Stressed" -> Pair("😬", Color(0xFFFAD1C5))
            "Anxious" -> Pair("😰", Color(0xFFCFE5FC))
            "Sad" -> Pair("😢", Color(0xFFD2D2ED))
            else -> Pair("🍃", Color(0xFFE2EFE7))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mood_log_item_${log.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Emoji circle container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(moodConfig.second),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = moodConfig.first,
                    fontSize = 28.sp
                )
            }

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.mood,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoTextMuted
                    )
                }

                if (log.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoTextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Delete action (touch target 48dp compliant)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_log_${log.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete log entry",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyLogsPlaceholder(onNavigateToMood: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = BentoPrimaryGreenText.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "A Peaceful Clean slate",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )
                Text(
                    text = "How are you checking in today? Take a quick second to store your state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextMuted,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onNavigateToMood,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("empty_state_action_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPrimaryGreenText,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Log Your First Mood")
            }
        }
    }
}
