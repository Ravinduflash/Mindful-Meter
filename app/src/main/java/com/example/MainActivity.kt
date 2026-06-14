package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WaterHydrationManager
import com.example.ui.components.WaterHydrationTrackerCard
import com.example.ui.components.WaterRainBorderOverlay
import com.example.ui.theme.MindfulTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync and initialize WorkManager reminders
        val hydrationManager = WaterHydrationManager.getInstance(this)
        
        // Handle incoming simulation request from notification tap
        if (intent?.getStringExtra("action") == "drink_water_animation") {
            hydrationManager.triggerRainBorderAnimation()
        }

        setContent {
            MindfulTheme {
                val animationActive by hydrationManager.showRainBorderAnimation.collectAsState()

                // Auto-dismiss the premium full-screen rain border overlay after 8 seconds
                LaunchedEffect(animationActive) {
                    if (animationActive) {
                        delay(8000)
                        hydrationManager.dismissRainBorderAnimation()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // MAIN APP SCAFFOLD CONTAINER PLACEHOLDER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mindfulness Hydrator",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Daily Habit Checklist combined with a physical 30-minute drink-water reminder and screen rain droplet animations.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        // Embed our newly constructed, interactive Water Hydration Tracker Card
                        WaterHydrationTrackerCard()

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "WorkManager reminders run in the background. Tap the buttons above to test simulation instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // 1. Full Screen Ambient Water Border and Falling Droplets Overlay Layer
                    WaterRainBorderOverlay(active = animationActive)

                    // 2. Beautiful floating clear dismiss action box that hovers at bottom center when active
                    AnimatedVisibility(
                        visible = animationActive,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 60.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    hydrationManager.dismissRainBorderAnimation()
                                }
                                .testTag("dismiss_rain_btn"),
                            color = Color(0xFF1976D2),
                            contentColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Got my sip! 💧",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
