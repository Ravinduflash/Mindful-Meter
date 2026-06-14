package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.animation.core.*
import com.example.ui.AiInsightUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MoodLog
import com.example.ui.MoodViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Favorite
import com.example.data.HealthConnectManager
import com.example.data.SleepRecordDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoodViewModel,
    onNavigateBack: () -> Unit
) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    var isAvailable by remember { mutableStateOf(healthConnectManager.isAvailable()) }
    var hasPermissions by remember { mutableStateOf(healthConnectManager.hasPermissions()) }
    var sleepRecords by remember { mutableStateOf<List<SleepRecordDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = healthConnectManager.hasPermissions()
    }

    LaunchedEffect(hasPermissions) {
        isLoading = true
        sleepRecords = healthConnectManager.fetchSleepDataPast7Days()
        isLoading = false
    }

    // Process statistics
    val totalEntries = logs.size
    val dominantMood = remember(logs) {
        if (logs.isEmpty()) "None"
        else {
            logs.groupBy { it.mood }
                .maxByOrNull { it.value.size }?.key ?: "Peaceful"
        }
    }

    val dominantEmoji = remember(dominantMood) {
        when (dominantMood) {
            "Happy" -> "😊"
            "Calm" -> "😌"
            "Stressed" -> "😬"
            "Anxious" -> "😰"
            "Sad" -> "😢"
            else -> "🍃"
        }
    }

    // Process the last 7 days of mood entries
    val last7DaysPoints = remember(logs) {
        val calendar = Calendar.getInstance()
        val scoredLogsByDay = mutableMapOf<Int, MutableList<Float>>() // day of year -> scores

        // Initialize last 7 days keys
        val now = Calendar.getInstance()
        val daysList = (0..6).map { offset ->
            val day = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            day.get(Calendar.DAY_OF_YEAR)
        }.reversed()

        logs.forEach { log ->
            calendar.timeInMillis = log.timestamp
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (dayOfYear in daysList) {
                val score = when (log.mood) {
                    "Happy" -> 5.0f
                    "Calm" -> 4.0f
                    "Stressed" -> 2.0f
                    "Anxious" -> 2.0f
                    "Sad" -> 1.0f
                    else -> 3.0f
                }
                scoredLogsByDay.getOrPut(dayOfYear) { mutableListOf() }.add(score)
            }
        }

        // Return a float score for each of the last 7 days
        daysList.map { day ->
            val scores = scoredLogsByDay[day]
            if (scores.isNullOrEmpty()) 3.0f // Neutral baseline
            else scores.average().toFloat()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stat Insights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("analytics_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("analytics_scroll_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val insightState by viewModel.aiInsightState.collectAsStateWithLifecycle()
                AiWeeklyInsightCard(
                    uiState = insightState,
                    onRefresh = { viewModel.fetchWeeklyInsight(forceRefresh = true) }
                )
            }
            // Stats Bento row (Two columns)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Bento: Dominant Emotional Card
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoAccentGreen),
                        border = BorderStroke(1.dp, BentoAccentGreenBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dominant Wave",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BentoPrimaryGreenText
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(dominantEmoji, fontSize = 34.sp)
                                Text(
                                    text = dominantMood,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                            }

                            Text(
                                text = "Based on last checking cycles",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Right Bento: Quantitative Stats Grid Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Small Box 1: Total logs
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BentoCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Total Sessions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoTextMuted
                                )
                                Text(
                                    text = "$totalEntries",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                            }
                        }

                        // Small Box 2: Habit Streaks
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BentoCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Equilibrium Index",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoTextMuted
                                )
                                Text(
                                    text = if (totalEntries == 0) "0%" else "${(totalEntries * 14).coerceAtMost(100)}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryGreenText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Health Connect Wellness Integration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_connect_sync_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasPermissions) BentoBreathingBg else Color.White
                    ),
                    border = BorderStroke(1.dp, if (hasPermissions) BentoBreathingAccent.copy(alpha = 0.3f) else BentoCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = "Sleep sync indicator",
                                    tint = if (hasPermissions) BentoBreathingAccent else BentoTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Wellness Sync Matrix",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                            }

                            // Availability Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (hasPermissions) BentoAccentGreen else BentoBg
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hasPermissions) "Connected" else "Not Synced",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (hasPermissions) BentoPrimaryGreenText else BentoTextMuted
                                )
                            }
                        }

                        if (!isAvailable) {
                            // Offline/Not available state
                            Text(
                                text = "Health Connect is unavailable on this device configuration. Install or update Google Health Connect services to bind tracking metrics.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoTextMuted,
                                lineHeight = 18.sp
                            )
                        } else if (!hasPermissions) {
                            // Onboarding state: request access
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Sync sleep cycles and heart rates from your wearables natively through Google Health Connect.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BentoTextDark.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                                
                                Button(
                                    onClick = { 
                                        permissionLauncher.launch(healthConnectManager.requiredPermissions.toTypedArray())
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("request_health_connect_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryGreenText),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "Sync Wellness Data",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // Authenticated state: Show real sleep record duration of last night
                            val lastNightSleep = sleepRecords.lastOrNull()
                            val durationMinutes = lastNightSleep?.durationMinutes ?: 480
                            val hrs = durationMinutes / 60
                            val mins = durationMinutes % 60
                            val note = lastNightSleep?.notes ?: "Restful sleep"

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "LAST NIGHT’S SLEEP DURATION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextMuted
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "${hrs}h ${mins}m",
                                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = BentoTextDark
                                    )

                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = BentoPrimaryGreenText
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Progress visual bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.5f))
                                ) {
                                    val progressFraction = (durationMinutes.toFloat() / 600f).coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressFraction)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BentoBreathingAccent)
                                    )
                                }

                                Text(
                                    text = "Equilibrium synced securely via Health Connect",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Line Graph Card (Jetpack Compose Canvas)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_canvas_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoCardBorder)
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = BentoPrimaryGreenText,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "7-Day Mood Flow",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                            }

                            // Minimal Legend
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BentoPrimaryGreenText))
                                Text("Vibration Level", style = MaterialTheme.typography.labelSmall, color = BentoTextMuted)
                            }
                        }

                        // Custom drawn line graph
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoBg.copy(alpha = 0.5f))
                                .border(1.dp, BentoCardBorder.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            if (totalEntries == 0) {
                                // Overlay tip when empty
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = BentoPrimaryGreenText.copy(alpha = 0.25f),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = "Awaiting mental metrics...",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = BentoTextMuted,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            LineGraphCanvas(
                                points = last7DaysPoints,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // X-Axis Labels Row (Relative past 6 days back to Today)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysOfWeek = remember {
                                val calendar = Calendar.getInstance()
                                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                                (0..6).map { offset ->
                                    val day = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
                                    sdf.format(day.time)
                                }.reversed()
                            }

                            daysOfWeek.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Insights interpretation strip
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoBreathingBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BentoBreathingAccent,
                            modifier = Modifier.size(24.dp)
                        )

                        Column {
                            Text(
                                text = "Cognitive Feedback",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalEntries == 0) {
                                    "Log a mood using the Home screen quick buttons to construct a feedback matrix."
                                } else {
                                    "Your equilibrium centers around \"$dominantMood\". Continue updating logs to verify consistency ranges."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoTextDark.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineGraphCanvas(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (points.isEmpty()) return@Canvas

        val maxVal = 5.0f // Happy score
        val minVal = 1.0f // Sad score
        val valRange = maxVal - minVal

        val pointsCount = points.size
        val xStep = width / (pointsCount - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        val coordinatePoints = points.mapIndexed { idx, value ->
            val x = idx * xStep
            // Invert Y direction since 0 is top
            val ratio = (value - minVal) / valRange
            val y = height - (ratio * (height - 30.dp.toPx())) - 15.dp.toPx()
            Offset(x, y.coerceIn(0f, height))
        }

        coordinatePoints.forEachIndexed { idx, point ->
            if (idx == 0) {
                path.moveTo(point.x, point.y)
                fillPath.moveTo(point.x, height)
                fillPath.lineTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
                fillPath.lineTo(point.x, point.y)
            }
            if (idx == pointsCount - 1) {
                fillPath.lineTo(point.x, height)
                fillPath.close()
            }
        }

        // 1. Draw smooth gradient filling under the flowline
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    BentoPrimaryGreenText.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        // 2. Draw background baseline grids
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = 15.dp.toPx() + (height - 30.dp.toPx()) * (i.toFloat() / gridLines)
            drawLine(
                color = BentoCardBorder.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // 3. Draw main flowline
        drawPath(
            path = path,
            color = BentoPrimaryGreenText,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 4. Draw circular anchor points
        coordinatePoints.forEach { point ->
            // white halo inner circle
            drawCircle(
                color = BentoPrimaryGreenText,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun AiWeeklyInsightCard(
    uiState: AiInsightUiState,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_weekly_insight_card")
            .clip(RoundedCornerShape(24.dp))
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8E2DE2), // Purple glow
                            Color(0xFF4A00E0), // Dark blue glow
                            Color(0xFF00C6FF)  // Cyan glow
                        )
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFE8DBFC), Color(0xFFD4BFFF))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "AI Insight Coach Logo",
                            tint = Color(0xFF4A00E0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Weekly Insight",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2C194E)
                        )
                        Text(
                            text = "Personalized Coaching",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7B6F96),
                            fontSize = 11.sp
                        )
                    }
                }
                
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Weekly Insight",
                        tint = Color(0xFF4A00E0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is AiInsightUiState.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerBar(modifier = Modifier.fillMaxWidth().height(16.dp))
                        ShimmerBar(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp))
                        ShimmerBar(modifier = Modifier.fillMaxWidth(0.75f).height(16.dp))
                    }
                }
                is AiInsightUiState.Success -> {
                    Text(
                        text = uiState.insight,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF423B5A)
                    )
                }
                is AiInsightUiState.Error -> {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerBar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = alpha))
    )
}
