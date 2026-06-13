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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MoodLog
import com.example.ui.MoodViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoodViewModel,
    onNavigateBack: () -> Unit
) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()

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
