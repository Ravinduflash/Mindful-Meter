package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CommunityPost
import com.example.data.LeaderboardUser
import com.example.ui.CommunityViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onNavigateBack: () -> Unit
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Feed") } // "Feed" or "Leaderboard"
    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mindful Community",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("community_back_button")
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
        floatingActionButton = {
            if (activeTab == "Feed") {
                FloatingActionButton(
                    onClick = { showShareDialog = true },
                    containerColor = BentoPrimaryGreenText,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .testTag("community_share_reflection_fab")
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "Share Reflection")
                        Text("Share Reflection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = BentoBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("community_screen_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High fidelity Tab selection pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoNavBg)
                    .padding(6.dp)
                    .testTag("community_tab_selector"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Feed", "Leaderboard").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BentoAccentGreen else Color.Transparent)
                            .clickable { activeTab = tab }
                            .testTag("community_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (tab == "Feed") Icons.Default.Forum else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (isSelected) BentoPrimaryGreenText else BentoTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (tab == "Feed") "Reflections" else "Top Explorers",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoPrimaryGreenText else BentoTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "community_tab_transition",
                modifier = Modifier.weight(1f)
            ) { currentTab ->
                if (currentTab == "Feed") {
                    FeedTabContent(
                        posts = posts,
                        onSupportToggle = { viewModel.toggleSupport(it) }
                    )
                } else {
                    LeaderboardTabContent(leaderboard = leaderboard)
                }
            }
        }

        // Beautiful Dialog for sharing a Reflection
        if (showShareDialog) {
            ShareReflectionDialog(
                onDismiss = { showShareDialog = false },
                onPost = { text ->
                    viewModel.addReflectingPost(text)
                    showShareDialog = false
                }
            )
        }
    }
}

@Composable
fun FeedTabContent(
    posts: List<CommunityPost>,
    onSupportToggle: (String) -> Unit
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = BentoTextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Text("No reflections shared yet", color = BentoTextMuted)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("community_posts_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // extra padding for FAB
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onSupportClick = { onSupportToggle(post.id) }
                )
            }
        }
    }
}

@Composable
fun PostCard(
    post: CommunityPost,
    onSupportClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_card_${post.id}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Author / Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Colored Avatar custom box
                val avatarColor = remember(post.authorColorHex) {
                    try {
                        Color(android.graphics.Color.parseColor("#" + post.authorColorHex))
                    } catch (e: Exception) {
                        BentoAccentGreen
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorInitials,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryGreenText,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                    Text(
                        text = post.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoTextMuted
                    )
                }
                
                // Small double bubble decorative icon
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = BentoTextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Post Text
            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = BentoTextDark
            )

            // Bottom Actions: Support Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like / Support Action Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (post.isSupportedByUser) BentoAccentGreen else BentoBg)
                        .border(1.dp, BentoCardBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onSupportClick)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("post_support_button_${post.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (post.isSupportedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Support post",
                        tint = if (post.isSupportedByUser) Color(0xFFC2185B) else BentoTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (post.isSupportedByUser) "Supported" else "Support",
                        fontWeight = FontWeight.Bold,
                        color = if (post.isSupportedByUser) Color(0xFF880E4F) else BentoTextMuted,
                        fontSize = 12.sp
                    )
                }

                // Support Counter Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = BentoTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${post.supportCount} loved this",
                        color = BentoTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardTabContent(
    leaderboard: List<LeaderboardUser>
) {
    val currentUser = remember(leaderboard) { leaderboard.find { it.isCurrentUser } }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prompter of current user rank highlighted
        if (currentUser != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_current_user_highlight"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, BentoPrimaryGreenText),
                colors = CardDefaults.cardColors(containerColor = BentoAccentGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Huge circle rank
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BentoPrimaryGreenText),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${currentUser.rank}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Current Standing",
                            style = MaterialTheme.typography.labelMedium,
                            color = BentoPrimaryGreenText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoTextDark
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentUser.streakDays}d",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = BentoPrimaryGreenText
                            )
                            Text("Streak", fontSize = 10.sp, color = BentoTextMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentUser.badgesCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = BentoPrimaryGreenText
                            )
                            Text("Badges", fontSize = 10.sp, color = BentoTextMuted)
                        }
                    }
                }
            }
        }

        Text(
            text = "Leaderboard Standings",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BentoTextDark
        )

        // Ranked list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("leaderboard_users_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(leaderboard, key = { it.id }) { user ->
                LeaderboardUserItem(user = user)
            }
        }
    }
}

@Composable
fun LeaderboardUserItem(user: LeaderboardUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_user_row_${user.id}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (user.isCurrentUser) BentoPrimaryGreenText.copy(alpha = 0.5f) else BentoCardBorder),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) BentoAccentGreen.copy(alpha = 0.4f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank counter container
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (user.rank) {
                            1 -> Color(0xFFFFD700) // Gold shimmer
                            2 -> Color(0xFFC0C0C0) // Silver shimmer
                            3 -> Color(0xFFCD7F32) // Bronze shimmer
                            else -> BentoNavBg
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user.rank}",
                    fontWeight = FontWeight.Bold,
                    color = if (user.rank in 1..3) Color.White else BentoTextMuted,
                    fontSize = 14.sp
                )
            }

            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                ),
                color = BentoTextDark,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Mindful streak days",
                        tint = Color(0xFFE2C044),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${user.streakDays}d",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextDark
                    )
                }

                // Badges indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Badges",
                        tint = BentoPrimaryGreenText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${user.badgesCount}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextDark
                    )
                }
            }
        }
    }
}

@Composable
fun ShareReflectionDialog(
    onDismiss: () -> Unit,
    onPost: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = null,
                    tint = BentoPrimaryGreenText
                )
                Text("Share Reflection", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Announce your achievements, mindful highlights, or a gratitude note to help other explorers stay motivated.",
                    fontSize = 12.sp,
                    color = BentoTextMuted,
                    lineHeight = 16.sp
                )

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("What did you practice today? Describe your feelings...", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BentoCardBorder, RoundedCornerShape(12.dp))
                        .testTag("reflection_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BentoBg,
                        unfocusedContainerColor = BentoBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("reflection_cancel")) {
                Text("Cancel", color = BentoTextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onPost(text) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryGreenText),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("reflection_submit")
            ) {
                Text("Post Reflection", color = Color.White)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
