package com.example.data

data class LibraryItem(
    val id: String,
    val title: String,
    val category: String, // "Articles", "Mini-Courses", "Guides"
    val estimate: String, // e.g., "5 min read", "3-day course", "10 min guide"
    val description: String,
    val sourceUrl: String = "",
    val isBookmarked: Boolean = false
)
