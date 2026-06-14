package com.example.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MindfulApplication
import com.example.data.DailyHabit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyHabitChecklistCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch Repository from Application Container
    val appContainer = remember { (context.applicationContext as MindfulApplication).container }
    val habitRepository = appContainer.dailyHabitRepository

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Collect Habits Flow
    val habitsState = habitRepository.getHabitsForDate(todayStr).collectAsState(initial = emptyList())
    val habits = habitsState.value

    // Auto-prepopulate of habits if empty
    LaunchedEffect(habits) {
        if (habits.isEmpty()) {
            val defaults = listOf(
                "Drink 250ml water on rise",
                "10 minutes mindful breathing",
                "Full body stretch & scan",
                "List 3 things you are grateful for",
                "No digital screen 30-m before sleep"
            )
            defaults.forEach { name ->
                habitRepository.insertHabit(
                    DailyHabit(
                        name = name,
                        isCompleted = false,
                        dateStr = todayStr
                    )
                )
            }
        }
    }

    // Input state for custom habits
    var customHabitName by remember { mutableStateOf("") }

    // Streak tracker logic stored in SharedPrefs
    val sharedPrefs = remember { context.getSharedPreferences("mindful_prefs", Context.MODE_PRIVATE) }
    var currentStreak by remember { mutableStateOf(sharedPrefs.getInt("mindful_streak", 3)) } // standard base starting default

    // Dark/Light layout tokens
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val borderLight = if (isDark) Color(0xFF333333) else Color(0xFFE2F1AF).copy(alpha = 0.5f)

    val primaryGreen = if (isDark) Color(0xFF81C784) else Color(0xFF386B01)
    val lightGreenBg = if (isDark) Color(0xFF1B3320) else Color(0xFFEAFCEB)
    val darkText = if (isDark) Color(0xFFECEFF1) else Color(0xFF161C24)
    val textMuted = if (isDark) Color(0xFF90A4AE) else Color(0xFF637381)

    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPct = (progressFraction * 100).toInt()

    // Update streak if everything is done today!
    LaunchedEffect(completedCount, totalCount) {
        if (totalCount > 0 && completedCount == totalCount) {
            val lastStreakUpdateStr = sharedPrefs.getString("streak_last_date", "")
            if (lastStreakUpdateStr != todayStr) {
                val newStreak = currentStreak + 1
                currentStreak = newStreak
                sharedPrefs.edit()
                    .putInt("mindful_streak", newStreak)
                    .putString("streak_last_date", todayStr)
                    .apply()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_habits_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderLight)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .background(lightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Habits list checklist icon",
                            tint = primaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Self-Care Goals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = darkText
                        )
                        Text(
                            text = "Log habits to increase completion bar",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    }
                }

                // Beautiful Flaming Streak Badging
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF4E2C0F) else Color(0xFFFFF3E0))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak flame badge",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$currentStreak Days",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Habits Progress Info block
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Metre completion: $progressPct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryGreen
                    )
                    Text(
                        text = "$completedCount of $totalCount Done",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFF0F0F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(CircleShape)
                            .background(primaryGreen)
                    )
                }
            }

            // Habits Listing items from Room
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                habits.forEach { habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                coroutineScope.launch {
                                    val updated = habit.copy(isCompleted = !habit.isCompleted)
                                    habitRepository.updateHabit(updated)
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = habit.isCompleted,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        val updated = habit.copy(isCompleted = checked)
                                        habitRepository.updateHabit(updated)
                                    }
                                },
                                modifier = Modifier.testTag("habit_check_${habit.id}"),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryGreen,
                                    uncheckedColor = textMuted
                                )
                            )
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (habit.isCompleted) textMuted else darkText
                            )
                        }

                        // Trash button for custom habits or default ones
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    habitRepository.deleteHabit(habit.id)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("habit_delete_${habit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Habit",
                                tint = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Divider(color = borderLight, thickness = 0.8.dp)

            // Add Custom Habit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customHabitName,
                    onValueChange = { customHabitName = it },
                    placeholder = { Text("Log custom self-care task...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("custom_habit_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        unfocusedBorderColor = borderLight,
                        focusedTextColor = darkText,
                        unfocusedTextColor = darkText,
                        focusedPlaceholderColor = textMuted,
                        unfocusedPlaceholderColor = textMuted
                    ),
                    singleLine = true
                )

                // Add button
                Button(
                    onClick = {
                        if (customHabitName.isNotBlank()) {
                            coroutineScope.launch {
                                habitRepository.insertHabit(
                                    DailyHabit(
                                        name = customHabitName.trim(),
                                        isCompleted = false,
                                        dateStr = todayStr
                                    )
                                )
                                customHabitName = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("add_custom_habit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom checklist goal",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
