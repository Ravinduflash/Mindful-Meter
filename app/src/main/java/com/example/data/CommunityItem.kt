package com.example.data

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorInitials: String,
    val authorColorHex: String, // background color for avatar block
    val timestamp: String,
    val text: String,
    val supportCount: Int,
    val isSupportedByUser: Boolean = false
)

data class LeaderboardUser(
    val id: String,
    val rank: Int,
    val name: String,
    val streakDays: Int,
    val badgesCount: Int,
    val isCurrentUser: Boolean = false
)
