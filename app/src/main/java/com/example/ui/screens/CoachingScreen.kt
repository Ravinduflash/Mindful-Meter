package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Coach
import com.example.data.PremiumBenefit
import com.example.ui.CoachingViewModel
import com.example.ui.theme.*

// Premium aesthetic colors
private val PremiumDeepObsidian = Color(0xFF0F1115)
private val PremiumCardBg = Color(0xFF1E2129)
private val GoldAccent = Color(0xFFD4AF37)
private val GoldShimmerLight = Color(0xFFF3E5AB)
private val PremiumGreenAccent = Color(0xFF81C784)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingScreen(
    viewModel: CoachingViewModel,
    onNavigateBack: () -> Unit
) {
    val coaches by viewModel.coaches.collectAsStateWithLifecycle()
    val benefits by viewModel.benefits.collectAsStateWithLifecycle()
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mindful Coaching",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("coaching_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = PremiumDeepObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .testTag("coaching_screen_container"),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Subscription status header banners
            AnimatedVisibility(
                visible = isSubscribed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coaching_subscribed_banner"),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = PremiumCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Premium Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "You have unlocked all coaches and custom biomarkers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.unsubscribe()
                                android.widget.Toast.makeText(context, "Premium Canceled", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("unsubscribe_action_button")
                        ) {
                            Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Featured Coaches Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Featured Guides",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Consult certified 1-on-1 experts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "4 ONLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumGreenAccent
                        )
                    }
                }

                // Horizontal scroll of coaches
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("featured_coaches_row"),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(coaches, key = { it.id }) { coach ->
                        CoachCardItem(coach = coach)
                    }
                }
            }

            // Distinctive Premium Membership Card Section
            PremiumSubscriptionBox(
                isSubscribed = isSubscribed,
                benefits = benefits,
                onSubscribeToggle = {
                    if (isSubscribed) {
                        viewModel.unsubscribe()
                        android.widget.Toast.makeText(context, "Premium membership suspended", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.subscribe()
                        android.widget.Toast.makeText(context, "Thank you for subscribing to Mindful Premium!", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CoachCardItem(coach: Coach) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(290.dp)
            .testTag("coach_card_${coach.id}"),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(containerColor = PremiumCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile block representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Colored Coach Avatar
                val avatarColor = remember(coach.avatarColorHex) {
                    try {
                        Color(android.graphics.Color.parseColor("#" + coach.avatarColorHex))
                    } catch (e: Exception) {
                        GoldAccent
                    }
                }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = coach.name.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first() }.joinToString("")
                    Text(
                        text = initials,
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumDeepObsidian,
                        fontSize = 16.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = coach.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating stars",
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${coach.rating}",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• ${coach.experienceYears}y exp",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Coach Specialties / Info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = coach.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = coach.specialty,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }

            // Quick Book row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = coach.availabilityToday,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = PremiumGreenAccent
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Book mock */ }
                ) {
                    Text(
                        text = "Consult",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumSubscriptionBox(
    isSubscribed: Boolean,
    benefits: List<PremiumBenefit>,
    onSubscribeToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gold_pulse")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grad_shift"
    )

    // Gold themed visual background gradient
    val goldPremiumGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF503A00),
            Color(0xFF221A00)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("premium_subscription_card"),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    GoldAccent.copy(alpha = gradientShift),
                    Color.Transparent,
                    GoldShimmerLight.copy(alpha = 1f - gradientShift)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(goldPremiumGradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "MINDFUL PREMIUM",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$9.99/MO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PremiumDeepObsidian
                        )
                    }
                }

                Text(
                    text = "Gain absolute access to certified specialists, bespoke biomarker tracking charts, and deep neural soundscapes built to optimize your circadian repairs.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Benefit elements
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val iconVector = when (benefit.iconName) {
                                    "video" -> Icons.Default.VideoCall
                                    "analytics" -> Icons.Default.Timeline
                                    "library" -> Icons.Default.AudioFile
                                    "group" -> Icons.Default.Group
                                    else -> Icons.Default.Check
                                }
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = benefit.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = benefit.description,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Prime subscription button
                Button(
                    onClick = onSubscribeToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubscribed) Color.Transparent else GoldAccent,
                        contentColor = if (isSubscribed) Color.White else PremiumDeepObsidian
                    ),
                    border = if (isSubscribed) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscribe_button_cta")
                ) {
                    Text(
                        text = if (isSubscribed) "Unsubscribe from Premium" else "Subscribe Now",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
