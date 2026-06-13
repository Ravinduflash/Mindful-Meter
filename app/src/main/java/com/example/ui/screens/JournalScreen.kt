package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JournalEntry
import com.example.ui.JournalViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val entries by viewModel.allJournalEntries.collectAsStateWithLifecycle()
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mindful Journal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("journal_back_button")
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
                .testTag("journal_scroll_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Write New Entry Section (Bento Box)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_input_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = BentoPrimaryGreenText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Whisper to your soul",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextDark
                            )
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            placeholder = { Text("Title of this moment...", color = BentoTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder,
                                focusedContainerColor = BentoBg.copy(alpha = 0.5f),
                                unfocusedContainerColor = BentoBg.copy(alpha = 0.3f),
                                focusedTextColor = BentoTextDark,
                                unfocusedTextColor = BentoTextDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = contentText,
                            onValueChange = { contentText = it },
                            placeholder = { Text("How does the environment feel? Let thoughts flow freely without judgement...", color = BentoTextMuted) },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_content_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimaryGreenText,
                                unfocusedBorderColor = BentoCardBorder,
                                focusedContainerColor = BentoBg.copy(alpha = 0.5f),
                                unfocusedContainerColor = BentoBg.copy(alpha = 0.3f),
                                focusedTextColor = BentoTextDark,
                                unfocusedTextColor = BentoTextDark
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                if (contentText.isNotBlank() || titleText.isNotBlank()) {
                                    val finalTitle = titleText.ifBlank { "Untitled Moment" }
                                    viewModel.saveJournalEntry(finalTitle, contentText)
                                    titleText = ""
                                    contentText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = titleText.isNotBlank() || contentText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("journal_save_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimaryGreenText,
                                contentColor = Color.White,
                                disabledContainerColor = BentoCardBorder,
                                disabledContentColor = BentoTextMuted
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Lock in Entry",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Journal Timeline Header
            item {
                Text(
                    text = "Reflective Timeline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextDark,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Timeline Entries List
            if (entries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "🍃 No logs in the garden yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BentoTextDark
                            )
                            Text(
                                "Your saved insights will form on this timeline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = entries,
                    key = { it.id }
                ) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_entry_item_${entry.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BentoTextDark
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(entry.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoTextMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteJournalEntry(entry.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("journal_delete_button_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete entry",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (entry.content.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BentoTextDark.copy(alpha = 0.9f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
