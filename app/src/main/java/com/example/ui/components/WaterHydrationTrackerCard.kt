package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WaterHydrationManager
import com.example.worker.WorkScheduler

@Composable
fun WaterHydrationTrackerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hydrationManager = remember { WaterHydrationManager.getInstance(context) }
    
    val drank by hydrationManager.currentWaterProgress.collectAsState()
    val dailyCap by hydrationManager.dailyWaterCap.collectAsState()
    
    val progressFraction = if (dailyCap > 0) (drank.toFloat() / dailyCap.toFloat()).coerceIn(0f, 1f) else 0f
    val progressPct = (progressFraction * 100).toInt()

    val isDark = isSystemInDarkTheme()

    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val borderLight = if (isDark) Color(0xFF333333) else Color(0xFFE2F1AF).copy(alpha = 0.5f)

    val primaryWaterBlue = if (isDark) Color(0xFF64B5F6) else Color(0xFF1E88E5)
    val lightWaterAqua = if (isDark) Color(0xFF0D47A1).copy(alpha = 0.45f) else Color(0xFFE3F2FD)
    val darkTextDark = if (isDark) Color(0xFFECEFF1) else Color(0xFF161C24)
    val textMuted = if (isDark) Color(0xFF90A4AE) else Color(0xFF637381)

    val sliderBg = if (isDark) Color(0xFF263238) else Color(0xFFF2F5F8)
    val capAchievedColor = if (isDark) Color(0xFF81C784) else Color(0xFF386B01)

    val buttonSimulateBg = if (isDark) Color(0xFF1F3A22) else Color(0xFFEAFCEB)
    val buttonSimulateBorder = if (isDark) Color(0xFF388E3C) else Color(0xFF81C784)
    val buttonSimulateText = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

    val buttonPlus200Text = if (isDark) Color(0xFF64B5F6) else Color(0xFF1E88E5)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_water_hydration_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderLight)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
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
                            .background(lightWaterAqua),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water Tracker",
                            tint = primaryWaterBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Hydration Meter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = darkTextDark
                        )
                        Text(
                            text = "Hit your $dailyCap ml daily water cap!",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    }
                }

                // Progress Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(lightWaterAqua)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$drank / $dailyCap ml",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFFBBDEFB) else primaryWaterBlue
                    )
                }
            }

            // Hydration Level Progress Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hydration level: $progressPct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryWaterBlue
                    )
                    if (drank >= dailyCap) {
                        Text(
                            text = "Cap Achieved! 🎉",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = capAchievedColor
                        )
                    }
                }

                // Fluid Progress Slider/Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(CircleShape)
                        .background(sliderBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(CircleShape)
                            .background(primaryWaterBlue)
                    )
                }
            }

            // Quick Logs Selection Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Log +250ml
                Button(
                    onClick = {
                        hydrationManager.recordWaterIntake(250)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("add_250_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = lightWaterAqua),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = "Cups icon",
                        tint = primaryWaterBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryWaterBlue)
                }

                // Log +500ml
                Button(
                    onClick = {
                        hydrationManager.recordWaterIntake(500)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("add_500_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryWaterBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = "Water droplet icon",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+500ml", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Interactive simulation and reminder action button
            Button(
                onClick = {
                    // Instantly schedules a notification + triggers full-screen rain border simulation!
                    WorkScheduler.triggerWaterReminderImmediately(context)
                    hydrationManager.triggerRainBorderAnimation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_remind_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = buttonSimulateBg),
                border = BorderStroke(1.2.dp, buttonSimulateBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Reminder bell icon",
                    tint = buttonSimulateText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulate 30-min Reminder (Rain Overlay)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = buttonSimulateText
                )
            }
        }
    }
}
