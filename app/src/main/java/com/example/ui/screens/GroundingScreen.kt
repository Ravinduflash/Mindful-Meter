package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundingScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) } // 1 to 5 steps, 6 is fully complete
    
    // Checkable states for interactive nodes
    val checkedSee = remember { mutableStateListOf(false, false, false, false, false) }
    val checkedTouch = remember { mutableStateListOf(false, false, false, false) }
    val checkedHear = remember { mutableStateListOf(false, false, false) }
    val checkedSmell = remember { mutableStateListOf(false, false) }
    val checkedTaste = remember { mutableStateListOf(false) }

    // Text inputs if the user wants to type actual things they notice
    val typedSee = remember { mutableStateListOf("", "", "", "", "") }
    val typedTouch = remember { mutableStateListOf("", "", "", "") }
    val typedHear = remember { mutableStateListOf("", "", "") }
    val typedSmell = remember { mutableStateListOf("", "") }
    val typedTaste = remember { mutableStateListOf("") }

    // Breathing guidance sub-state during transit
    var breathingPhase by remember { mutableStateOf("Ready") } // Ready, Inhale, Hold, Exhale
    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingProgress by remember { mutableFloatStateOf(0f) }

    // Simulate breathing cadence when clicking continue
    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            breathingPhase = "Breathe In..."
            var count = 0
            while (count < 4) {
                breathingProgress = (count + 1) / 4f
                kotlinx.coroutines.delay(1000)
                count++
            }
            
            breathingPhase = "Hold..."
            count = 0
            while (count < 2) {
                kotlinx.coroutines.delay(1000)
                count++
            }
            
            breathingPhase = "Release..."
            count = 0
            while (count < 4) {
                breathingProgress = 1f - ((count + 1) / 4f)
                kotlinx.coroutines.delay(1000)
                count++
            }
            
            isBreathingActive = false
            breathingPhase = "Ready"
            if (currentStep < 6) {
                currentStep += 1
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Spa,
                            contentDescription = "Spa Icon",
                            tint = BentoPrimaryGreenText
                        )
                        Text(
                            text = "Calming Grounding Space",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("grounding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BentoTextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = BentoBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("grounding_screen_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Introductory Banner card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BentoBreathingBg.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Take a Soft Breath",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoBreathingAccent
                        )
                        Text(
                            text = "When anxiety feels overwhelming, the 5-4-3-2-1 technique helps anchor your mind by bringing attention back to your senses. Let's practice now, slowly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoTextMuted,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Step Progress Indicator
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoNavBg)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentStep <= 5) "Step $currentStep of 5 Sensory Tasks" else "Grounding Complete!",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BentoPrimaryGreenText
                            )
                            Text(
                                text = "${((currentStep - 1) * 20).coerceIn(0, 100)}% Anchored",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextMuted
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { (currentStep - 1) / 5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BentoPrimaryGreenText,
                            trackColor = BentoCardBorder
                        )
                    }
                }
            }

            // --- SENSORY BLOCK 5: SEE (Show if currentStep >= 1) ---
            item {
                AnimatedVisibility(
                    visible = currentStep >= 1,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SensoryCard(
                        title = "5 Things You Can See",
                        colorTheme = BentoAccentGreen,
                        icon = Icons.Default.Visibility,
                        instructions = "Look around and spot 5 distinct visual details. Tap to check them off.",
                        count = 5,
                        checkedStates = checkedSee,
                        typedValues = typedSee,
                        placeholderGenerator = { index ->
                            listOf("Something green", "A source of light", "An object on a table", "A colorful label", "A shape in your room")[index]
                        },
                        isActive = currentStep == 1
                    )
                }
            }

            // --- SENSORY BLOCK 4: TOUCH (Show if currentStep >= 2) ---
            item {
                AnimatedVisibility(
                    visible = currentStep >= 2,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SensoryCard(
                        title = "4 Things You Can Touch",
                        colorTheme = BentoBreathingBg,
                        icon = Icons.Default.BackHand,
                        instructions = "Bring full awareness to physical sensations. Tap 4 to anchor.",
                        count = 4,
                        checkedStates = checkedTouch,
                        typedValues = typedTouch,
                        placeholderGenerator = { index ->
                            listOf("The surface under you", "Your clothing fabric", "Something cold/metal", "Your keyboard or phone rim")[index]
                        },
                        isActive = currentStep == 2
                    )
                }
            }

            // --- SENSORY BLOCK 3: HEAR (Show if currentStep >= 3) ---
            item {
                AnimatedVisibility(
                    visible = currentStep >= 3,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SensoryCard(
                        title = "3 Things You Can Hear",
                        colorTheme = BentoAccentGreen.copy(alpha = 0.6f),
                        icon = Icons.Default.Hearing,
                        instructions = "Close your eyes for 5 seconds. Identify 3 external sounds.",
                        count = 3,
                        checkedStates = checkedHear,
                        typedValues = typedHear,
                        placeholderGenerator = { index ->
                            listOf("A low mechanical hum", "Wind or rustling leaves", "Your own steady breath")[index]
                        },
                        isActive = currentStep == 3
                    )
                }
            }

            // --- SENSORY BLOCK 2: SMELL (Show if currentStep >= 4) ---
            item {
                AnimatedVisibility(
                    visible = currentStep >= 4,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SensoryCard(
                        title = "2 Things You Can Smell",
                        colorTheme = BentoBreathingBg.copy(alpha = 0.7f),
                        icon = Icons.Default.Air,
                        instructions = "Inhale deeply. Notice 2 subtle elements present in the local air aroma.",
                        count = 2,
                        checkedStates = checkedSmell,
                        typedValues = typedSmell,
                        placeholderGenerator = { index ->
                            listOf("A clean room draft", "An essential oil or snack item")[index]
                        },
                        isActive = currentStep == 4
                    )
                }
            }

            // --- SENSORY BLOCK 1: TASTE (Show if currentStep >= 5) ---
            item {
                AnimatedVisibility(
                    visible = currentStep >= 5,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SensoryCard(
                        title = "1 Thing You Can Taste",
                        colorTheme = BentoAccentGreen,
                        icon = Icons.Default.Restaurant,
                        instructions = "Take a sip of water or scan your mouth. Identify 1 sensation.",
                        count = 1,
                        checkedStates = checkedTaste,
                        typedValues = typedTaste,
                        placeholderGenerator = { index ->
                            listOf("Clean cool liquid / fresh lingering toothpaste")[index]
                        },
                        isActive = currentStep == 5
                    )
                }
            }

            // Breathing action transition button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (currentStep <= 5) {
                    Button(
                        onClick = {
                            isBreathingActive = true
                        },
                        enabled = !isBreathingActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimaryGreenText,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("grounding_breathe_continue_button")
                    ) {
                        AnimatedContent(
                            targetState = isBreathingActive,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "breathingButtonContent"
                        ) { isBreathing ->
                            if (isBreathing) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Breathing Animation",
                                        modifier = Modifier.scale(1.2f)
                                    )
                                    Text(
                                        text = "$breathingPhase",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Spa,
                                        contentDescription = "Spa icon"
                                    )
                                    Text(
                                        text = "Breathe & Unlock Next Sensory Step",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Reset or celebration button
                    Button(
                        onClick = {
                            currentStep = 1
                            checkedSee.fill(false)
                            checkedTouch.fill(false)
                            checkedHear.fill(false)
                            checkedSmell.fill(false)
                            checkedTaste.fill(false)
                            typedSee.fill("")
                            typedTouch.fill("")
                            typedHear.fill("")
                            typedSmell.fill("")
                            typedTaste.fill("")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimaryGreenText
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart")
                            Text("Practice Grounding Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- THE CRISIS DIALER PANEL ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("emergency_sos_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFFFC1C1)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "Emergency Icon",
                                tint = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "Instant Emergency & Crisis Support",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC62828)
                            )
                        }

                        Text(
                            text = "If you are in danger or need immediate emotional guidance, please reach out. Tapping below will load the telephone directly, but will not dial automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button 1: 988 Suicide & Crisis Lifeline
                            Button(
                                onClick = {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:988")
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open dialer.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sos_dial_988_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone icon",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call 988", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            // Button 2: Standard Emergency (911)
                            Button(
                                onClick = {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:911")
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open dialer.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFFD32F2F)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sos_dial_911_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPhone,
                                        contentDescription = "Standard Local emergency",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call 911", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensoryCard(
    title: String,
    colorTheme: Color,
    icon: ImageVector,
    instructions: String,
    count: Int,
    checkedStates: List<Boolean>,
    typedValues: List<String>,
    placeholderGenerator: (Int) -> String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) BentoPrimaryGreenText else BentoCardBorder
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) colorTheme else BentoBg
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isActive) BentoPrimaryGreenText else BentoNavBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) Color.White else BentoTextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark
                )
            }

            Text(
                text = instructions,
                style = MaterialTheme.typography.bodySmall,
                color = BentoTextMuted
            )

            // Dynamic checkboxes and small fields to record details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until count) {
                    val isChecked = checkedStates.getOrElse(i) { false }
                    val typedValue = typedValues.getOrElse(i) { "" }
                    val placeholderText = placeholderGenerator(i)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChecked) BentoAccentGreen.copy(alpha = 0.4f) else Color.Transparent)
                            .clickable {
                                if (checkedStates is MutableList) {
                                    checkedStates[i] = !isChecked
                                }
                            }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { change ->
                                if (checkedStates is MutableList) {
                                    checkedStates[i] = change
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BentoPrimaryGreenText,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.testTag("see_checkbox_${i + 1}")
                        )

                        OutlinedTextField(
                            value = typedValue,
                            onValueChange = { newValue ->
                                if (typedValues is MutableList) {
                                    typedValues[i] = newValue
                                }
                            },
                            placeholder = {
                                Text(
                                    text = placeholderText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextMuted.copy(alpha = 0.7f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = BentoTextDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BentoBg,
                                unfocusedContainerColor = BentoBg,
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )
                    }
                }
            }
        }
    }
}
