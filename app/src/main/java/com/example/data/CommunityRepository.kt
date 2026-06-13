package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface CommunityRepository {
    fun getPosts(): Flow<List<CommunityPost>>
    fun getLeaderboard(): Flow<List<LeaderboardUser>>
    suspend fun supportPost(postId: String)
    suspend fun addPost(text: String)
}

class OfflineCommunityRepository : CommunityRepository {
    private val _posts = MutableStateFlow(
        listOf(
            CommunityPost(
                id = "post_1",
                authorName = "Sophia Winters",
                authorInitials = "SW",
                authorColorHex = "DEE9FF",
                timestamp = "Just Now",
                text = "Just completed a 10-day breathing streak! Feeling so much calmer and more centered today. The box breathing technique really helped during a stressful meeting.",
                supportCount = 8,
                isSupportedByUser = false
            ),
            CommunityPost(
                id = "post_2",
                authorName = "Liam Chen",
                authorInitials = "LC",
                authorColorHex = "E2F5E2",
                timestamp = "20 min ago",
                text = "Tested the new Cosmic Noise soundscape for a deep focused coding session. It completely isolated my thoughts from peripheral city noise. Absolute game changer!",
                supportCount = 14,
                isSupportedByUser = true
            ),
            CommunityPost(
                id = "post_3",
                authorName = "Ella Grace",
                authorInitials = "EG",
                authorColorHex = "FFE2EB",
                timestamp = "1 hour ago",
                text = "Took 5 minutes to record my morning flow. Admitting anxiety to myself through journaling makes it feel temporary. Highly recommend taking that tiny pen-and-paper pause.",
                supportCount = 5,
                isSupportedByUser = false
            ),
            CommunityPost(
                id = "post_4",
                authorName = "Marcus Aurelius",
                authorInitials = "MA",
                authorColorHex = "FFE8D6",
                timestamp = "3 hours ago",
                text = "Unplugging all screens by 10 PM tonight. Trying to reset my circadian rhythm. Let's conquer the bedtime racing thoughts together!",
                supportCount = 21,
                isSupportedByUser = false
            ),
        )
    )

    private val _leaderboard = MutableStateFlow(
        listOf(
            LeaderboardUser("user_1", 1, "Alexander Great", 42, 12, false),
            LeaderboardUser("user_2", 2, "Helena Troy", 31, 9, false),
            LeaderboardUser("user_3", 3, "Marcus Aurelius", 24, 8, false),
            LeaderboardUser("user_curr", 4, "You (Mindful Explorer)", 18, 5, true), // Current User
            LeaderboardUser("user_4", 5, "Sophia Winters", 10, 4, false),
            LeaderboardUser("user_5", 6, "Liam Chen", 8, 3, false),
            LeaderboardUser("user_6", 7, "Ella Grace", 6, 2, false)
        )
    )

    override fun getPosts(): Flow<List<CommunityPost>> = _posts.asStateFlow()

    override fun getLeaderboard(): Flow<List<LeaderboardUser>> = _leaderboard.asStateFlow()

    override suspend fun supportPost(postId: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.id == postId) {
                    val supported = !post.isSupportedByUser
                    val countChange = if (supported) 1 else -1
                    post.copy(
                        isSupportedByUser = supported,
                        supportCount = post.supportCount + countChange
                    )
                } else post
            }
        }
    }

    override suspend fun addPost(text: String) {
        val newPost = CommunityPost(
            id = "user_post_${System.currentTimeMillis()}",
            authorName = "You (Mindful Explorer)",
            authorInitials = "ME",
            authorColorHex = "E2F1AF", // Bento Accent Green
            timestamp = "Just Now",
            text = text,
            supportCount = 0,
            isSupportedByUser = false
        )
        _posts.update { listOf(newPost) + it }
    }
}
