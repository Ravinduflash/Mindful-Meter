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
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.LibraryItem
import com.example.ui.LibraryViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()

    val categories = listOf("Articles", "Mini-Courses", "Guides", "Bookmarks")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mindful Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("library_back_button")
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
                .padding(horizontal = 20.dp)
                .testTag("library_screen_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar Text Field
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Search articles, courses, or guides...",
                        color = BentoTextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BentoTextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = BentoTextMuted
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BentoCardBorder, RoundedCornerShape(20.dp))
                    .testTag("library_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Category Tab selector
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("library_category_tabs"),
                containerColor = BentoNavBg,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[categories.indexOf(selectedCategory).coerceAtLeast(0)]
                        ),
                        color = BentoPrimaryGreenText
                    )
                },
                divider = {}
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
                        text = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BentoPrimaryGreenText else BentoTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("category_tab_$category")
                    )
                }
            }

            // Results List
            AnimatedContent(
                targetState = items,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "library_list_transition",
                modifier = Modifier.weight(1f)
            ) { itemList ->
                if (itemList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("library_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedCategory == "Bookmarks") Icons.Default.Bookmarks else Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = BentoTextMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (selectedCategory == "Bookmarks") {
                                    "No Bookmarked Content"
                                } else {
                                    "No Resources Found"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (selectedCategory == "Bookmarks") {
                                    "Read articles across other tabs and tap the bookmark ribbon to save them here."
                                } else {
                                    "Try adjusting your spelling or category tab filters to discover mindful tutorials."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoTextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("library_items_list"),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(itemList, key = { it.id }) { item ->
                            LibraryItemCard(
                                item = item,
                                onBookmarkToggle = { viewModel.toggleBookmark(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItemCard(
    item: LibraryItem,
    onBookmarkToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("library_item_card_${item.id}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Badge & Bookmark selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category/Read estimate Tag Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BentoAccentGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.estimate.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoPrimaryGreenText,
                        letterSpacing = 0.5.sp
                    )
                }

                // Tactile Bookmark Button
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoBg)
                        .border(1.dp, BentoCardBorder, CircleShape)
                        .testTag("bookmark_button_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (item.isBookmarked) "Remove bookmark" else "Bookmark item",
                        tint = if (item.isBookmarked) BentoPrimaryGreenText else BentoTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Central details: title and description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    color = BentoTextDark
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted,
                    lineHeight = 16.sp
                )
            }

            // Quick footer Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { /* action */ }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (item.category) {
                            "Mini-Courses" -> "Join Course"
                            "Guides" -> "View Guide"
                            else -> "Read Article"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryGreenText
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = BentoPrimaryGreenText,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
