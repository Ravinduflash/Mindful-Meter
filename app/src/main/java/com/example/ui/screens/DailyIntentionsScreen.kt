package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyIntention
import com.example.ui.DailyIntentionViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyIntentionsScreen(
    viewModel: DailyIntentionViewModel,
    onNavigateBack: () -> Unit
) {
    val intentions by viewModel.currentIntentions.collectAsStateWithLifecycle()
    val allIntentions by viewModel.historicalIntentions.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var goal1 by remember { mutableStateOf("") }
    var goal2 by remember { mutableStateOf("") }
    var goal3 by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Group all intentions by date (except today) for history view
    val historicalLogs = remember(allIntentions, selectedDate) {
        allIntentions
            .filter { it.dateStr != selectedDate }
            .groupBy { it.dateStr }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Intentions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("intentions_back_button")
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("daily_intentions_container"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Introductory Card detailing purpose
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BentoAccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = BentoPrimaryGreenText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Morning Focus",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                        Text(
                            text = "Setting goals at start of day primes your attentional system to spot opportunities for calmness and clarity.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = BentoTextMuted
                        )
                    }
                }
            }

            // MAIN INTERACTIVE PANEL
            if (intentions.isEmpty()) {
                // GOALS FORM PANEL
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_intentions_card"),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, BentoCardBorder),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Set Today's Intentions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                        Text(
                            text = "What three actions are you committing to today? Keep them specific, actionable, and small.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted
                        )

                        // Input 1
                        OutlinedTextField(
                            value = goal1,
                            onValueChange = { goal1 = it },
                            label = { Text("Intention 1", fontSize = 12.sp) },
                            placeholder = { Text("e.g., Spend 10 mins reading", fontSize = 13.sp) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_input_1"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder
                            ),
                            singleLine = true
                        )

                        // Input 2
                        OutlinedTextField(
                            value = goal2,
                            onValueChange = { goal2 = it },
                            label = { Text("Intention 2", fontSize = 12.sp) },
                            placeholder = { Text("e.g., Do box breathing after lunch", fontSize = 13.sp) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_input_2"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder
                            ),
                            singleLine = true
                        )

                        // Input 3
                        OutlinedTextField(
                            value = goal3,
                            onValueChange = { goal3 = it },
                            label = { Text("Intention 3", fontSize = 12.sp) },
                            placeholder = { Text("e.g., No laptop after 9:30 PM", fontSize = 13.sp) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_input_3"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val buttonEnabled = goal1.isNotBlank() || goal2.isNotBlank() || goal3.isNotBlank()
                        Button(
                            onClick = {
                                viewModel.setIntentions(goal1, goal2, goal3)
                                // reset local inputs
                                goal1 = ""
                                goal2 = ""
                                goal3 = ""
                                android.widget.Toast.makeText(context, "Good morning! Intentions set.", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            enabled = buttonEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimaryGreenText,
                                disabledContainerColor = BentoCardBorder
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("goal_submit_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null)
                                Text("Commit to Intentions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                // FOCUS TRACKING PANEL (CHECKBOX LAYOUT)
                val totalCount = intentions.size
                val completedCount = intentions.count { it.isCompleted }
                val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(500),
                    label = "progress"
                )

                // Large Progress Bento Box
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Journey",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BentoTextDark
                                )
                                Text(
                                    text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoAccentGreen)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$completedCount/$totalCount DONE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = BentoPrimaryGreenText
                                )
                            }
                        }

                        // Progress bar indicator
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .testTag("completion_progress_indicator"),
                                color = BentoPrimaryGreenText,
                                trackColor = BentoNavBg,
                            )
                            
                            // Dynamic Encouragement Text
                            val encourageText = when {
                                completedCount == 0 -> "Tap items below as you complete them throughout the day."
                                completedCount < totalCount -> "You are making steady progress. Keep going!"
                                else -> "Outstanding! You've accomplished all of your intentions."
                            }
                            Text(
                                text = encourageText,
                                fontSize = 12.sp,
                                color = BentoTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Celebrate perfection state animation
                        AnimatedVisibility(
                            visible = completedCount == totalCount && totalCount > 0,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoAccentGreen.copy(alpha = 0.5f))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = BentoPrimaryGreenText
                                    )
                                    Text(
                                        text = "Mindful Day Achieved! 🎉 Keep up the focus.",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = BentoPrimaryGreenText
                                    )
                                }
                            }
                        }
                    }
                }

                // CHECKBOX ROWS CONTAINER
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("intentions_list_container")
                ) {
                    intentions.forEachIndexed { index, intention ->
                        val isDone = intention.isCompleted
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleIntentionCompletion(intention) }
                                .testTag("intention_row_$index"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isDone) BentoAccentGreenBorder else BentoCardBorder
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) BentoAccentGreen.copy(alpha = 0.2f) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Beautiful circular custom checkbox
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("intention_checkbox_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = "Toggle completion",
                                        tint = if (isDone) BentoPrimaryGreenText else BentoTextMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = intention.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Bold,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (isDone) BentoTextMuted.copy(alpha = 0.7f) else BentoTextDark
                                    )
                                }

                                // Delete option button
                                IconButton(
                                    onClick = { viewModel.deleteIntention(intention.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete goal",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tiny Clear Option row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            intentions.forEach { viewModel.deleteIntention(it.id) }
                            android.widget.Toast.makeText(context, "Today's intentions cleared.", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = BentoTextMuted)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Reset Today's Intentions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // HISTORICAL LOGS SECTION
            if (historicalLogs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Reflection History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )

                    historicalLogs.take(5).forEach { (dateStr, list) ->
                        val completed = list.count { it.isCompleted }
                        val total = list.size
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BentoCardBorder),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Parse and format dateStr beautified
                                    val formattedDateStr = remember(dateStr) {
                                        try {
                                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                            if (date != null) {
                                                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(date)
                                            } else dateStr
                                        } catch (e: Exception) {
                                            dateStr
                                        }
                                    }

                                    Text(
                                        text = formattedDateStr,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoTextDark
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (completed == total) BentoPrimaryGreenText else BentoTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "$completed/$total",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (completed == total) BentoPrimaryGreenText else BentoTextMuted
                                        )
                                    }
                                }

                                // List small bullets
                                list.forEach { intention ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (intention.isCompleted) BentoPrimaryGreenText else BentoTextMuted.copy(alpha = 0.4f))
                                        )
                                        Text(
                                            text = intention.text,
                                            fontSize = 12.sp,
                                            color = if (intention.isCompleted) BentoTextDark else BentoTextMuted,
                                            textDecoration = if (intention.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
