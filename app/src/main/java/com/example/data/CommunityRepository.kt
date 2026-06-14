package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface CommunityRepository {
    fun getPosts(): Flow<List<CommunityPost>>
    fun getLeaderboard(): Flow<List<LeaderboardUser>>
    suspend fun supportPost(postId: String)
    suspend fun addPost(text: String)
}

class OfflineCommunityRepository : CommunityRepository {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
    
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    private val localPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    private val localLeaderboard = MutableStateFlow<List<LeaderboardUser>>(emptyList())

    init {
        // Run background initialization of standard items if Firestore has no posts
        try {
            val db = firestore
            if (db != null) {
                db.collection("posts").limit(1).get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        seedDefaultPosts()
                    }
                }
                db.collection("leaderboard").limit(1).get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        seedDefaultLeaderboard()
                    }
                }
            } else {
                seedLocalFallbackData()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            seedLocalFallbackData()
        }
    }

    private fun seedLocalFallbackData() {
        val posts = listOf(
            CommunityPost(
                id = "p_1",
                authorName = "Sophia Winters",
                authorInitials = "SW",
                authorColorHex = "DEE9FF",
                timestamp = "10 min ago",
                text = "Just completed a 10-day breathing streak! Feeling so much calmer and more centered today. The box breathing technique really helped during a stressful meeting.",
                supportCount = 8,
                isSupportedByUser = false
            ),
            CommunityPost(
                id = "p_2",
                authorName = "Liam Chen",
                authorInitials = "LC",
                authorColorHex = "E2F5E2",
                timestamp = "20 min ago",
                text = "Tested the new Cosmic Noise soundscape for a deep focused coding session. It completely isolated my thoughts from peripheral city noise. Absolute game changer!",
                supportCount = 14,
                isSupportedByUser = false
            ),
            CommunityPost(
                id = "p_3",
                authorName = "Ella Grace",
                authorInitials = "EG",
                authorColorHex = "FFE2EB",
                timestamp = "1 hour ago",
                text = "Took 5 minutes to record my morning flow. Admitting anxiety to myself through journaling makes it feel temporary. Highly recommend taking that tiny pen-and-paper pause.",
                supportCount = 5,
                isSupportedByUser = false
            ),
            CommunityPost(
                id = "p_4",
                authorName = "Marcus Aurelius",
                authorInitials = "MA",
                authorColorHex = "FFE8D6",
                timestamp = "3 hours ago",
                text = "Unplugging all screens by 10 PM tonight. Trying to reset my circadian rhythm. Let's conquer the bedtime racing thoughts together!",
                supportCount = 21,
                isSupportedByUser = false
            )
        )
        localPosts.value = posts

        val leaderboard = listOf(
            LeaderboardUser("user_1", 1, "Alexander Great", 42, 12, false),
            LeaderboardUser("user_2", 2, "Helena Troy", 31, 9, false),
            LeaderboardUser("user_3", 3, "Marcus Aurelius", 24, 8, false),
            LeaderboardUser("user_curr", 4, "You (Mindful Explorer)", 18, 5, true),
            LeaderboardUser("user_4", 5, "Sophia Winters", 10, 4, false),
            LeaderboardUser("user_5", 6, "Liam Chen", 8, 3, false),
            LeaderboardUser("user_6", 7, "Ella Grace", 6, 2, false)
        )
        localLeaderboard.value = leaderboard
    }

    private fun seedDefaultPosts() {
        val db = firestore ?: return
        val defaultPosts = listOf(
            mapOf(
                "authorName" to "Sophia Winters",
                "authorInitials" to "SW",
                "authorColorHex" to "DEE9FF",
                "text" to "Just completed a 10-day breathing streak! Feeling so much calmer and more centered today. The box breathing technique really helped during a stressful meeting.",
                "supportCount" to 8,
                "supportedBy" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis() - 600_000
            ),
            mapOf(
                "authorName" to "Liam Chen",
                "authorInitials" to "LC",
                "authorColorHex" to "E2F5E2",
                "text" to "Tested the new Cosmic Noise soundscape for a deep focused coding session. It completely isolated my thoughts from peripheral city noise. Absolute game changer!",
                "supportCount" to 14,
                "supportedBy" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis() - 1200_000
            ),
            mapOf(
                "authorName" to "Ella Grace",
                "authorInitials" to "EG",
                "authorColorHex" to "FFE2EB",
                "text" to "Took 5 minutes to record my morning flow. Admitting anxiety to myself through journaling makes it feel temporary. Highly recommend taking that tiny pen-and-paper pause.",
                "supportCount" to 5,
                "supportedBy" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis() - 3600_000
            ),
            mapOf(
                "authorName" to "Marcus Aurelius",
                "authorInitials" to "MA",
                "authorColorHex" to "FFE8D6",
                "text" to "Unplugging all screens by 10 PM tonight. Trying to reset my circadian rhythm. Let's conquer the bedtime racing thoughts together!",
                "supportCount" to 21,
                "supportedBy" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis() - 10800_000
            )
        )
        for (post in defaultPosts) {
            db.collection("posts").add(post)
        }
    }

    private fun seedDefaultLeaderboard() {
        val db = firestore ?: return
        val defaultLeaderboard = listOf(
            LeaderboardUser("user_1", 1, "Alexander Great", 42, 12, false),
            LeaderboardUser("user_2", 2, "Helena Troy", 31, 9, false),
            LeaderboardUser("user_3", 3, "Marcus Aurelius", 24, 8, false),
            LeaderboardUser("user_curr", 4, "You (Mindful Explorer)", 18, 5, true),
            LeaderboardUser("user_4", 5, "Sophia Winters", 10, 4, false),
            LeaderboardUser("user_5", 6, "Liam Chen", 8, 3, false),
            LeaderboardUser("user_6", 7, "Ella Grace", 6, 2, false)
        )
        for (user in defaultLeaderboard) {
            db.collection("leaderboard").document(user.id).set(user)
        }
    }

    override fun getPosts(): Flow<List<CommunityPost>> {
        val db = firestore ?: return localPosts
        return callbackFlow {
            val listenerRegistration = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val authorName = doc.getString("authorName") ?: "Anonymous"
                                val authorInitials = doc.getString("authorInitials") ?: "A"
                                val authorColorHex = doc.getString("authorColorHex") ?: "DEE9FF"
                                val text = doc.getString("text") ?: ""
                                val supportCount = doc.getLong("supportCount")?.toInt() ?: 0
                                @Suppress("UNCHECKED_CAST")
                                val supportedBy = doc.get("supportedBy") as? List<String> ?: emptyList()
                                
                                val currentUserId = auth?.currentUser?.uid ?: ""
                                val isSupportedByUser = currentUserId.isNotEmpty() && supportedBy.contains(currentUserId)
                                
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                val timeDiff = System.currentTimeMillis() - createdAt
                                val displayTime = when {
                                    timeDiff < 60_000 -> "Just Now"
                                    timeDiff < 3600_000 -> "${timeDiff / 60_000} min ago"
                                    timeDiff < 86400_000 -> "${timeDiff / 3600_000} hours ago"
                                    else -> {
                                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                        sdf.format(Date(createdAt))
                                    }
                                }

                                CommunityPost(
                                    id = id,
                                    authorName = authorName,
                                    authorInitials = authorInitials,
                                    authorColorHex = authorColorHex,
                                    timestamp = displayTime,
                                    text = text,
                                    supportCount = supportCount,
                                    isSupportedByUser = isSupportedByUser
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(posts)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    override fun getLeaderboard(): Flow<List<LeaderboardUser>> {
        val db = firestore ?: return localLeaderboard
        return callbackFlow {
            val currentUserId = auth?.currentUser?.uid ?: "user_curr"
            val listenerRegistration = db.collection("leaderboard")
                .orderBy("rank", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val users = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val rank = doc.getLong("rank")?.toInt() ?: 1
                                val name = doc.getString("name") ?: "Visitor"
                                val streakDays = doc.getLong("streakDays")?.toInt() ?: 0
                                val badgesCount = doc.getLong("badgesCount")?.toInt() ?: 0
                                val isCurrentUser = (id == currentUserId) || (id == "user_curr" && auth?.currentUser == null)
                                
                                LeaderboardUser(
                                    id = id,
                                    rank = rank,
                                    name = if (isCurrentUser && auth?.currentUser != null) {
                                        auth?.currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }?.let { "You ($it)" } ?: "You (Mindful Explorer)"
                                    } else {
                                        name
                                    },
                                    streakDays = streakDays,
                                    badgesCount = badgesCount,
                                    isCurrentUser = isCurrentUser
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(users)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    override suspend fun supportPost(postId: String) {
        val db = firestore
        if (db == null) {
            localPosts.value = localPosts.value.map { post ->
                if (post.id == postId) {
                    val toggled = !post.isSupportedByUser
                    val newCount = if ( toggled ) post.supportCount + 1 else (post.supportCount - 1).coerceAtLeast(0)
                    post.copy(supportCount = newCount, isSupportedByUser = toggled)
                } else {
                    post
                }
            }
            return
        }
        val currentUserId = auth?.currentUser?.uid ?: "guest_user"
        val docRef = db.collection("posts").document(postId)
        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                @Suppress("UNCHECKED_CAST")
                val supportedBy = snapshot.get("supportedBy") as? List<String> ?: emptyList()
                val currentCount = snapshot.getLong("supportCount") ?: 0
                
                val updatedSupportedBy = supportedBy.toMutableList()
                val updatedCount: Long
                if (updatedSupportedBy.contains(currentUserId)) {
                    updatedSupportedBy.remove(currentUserId)
                    updatedCount = (currentCount - 1).coerceAtLeast(0)
                } else {
                    updatedSupportedBy.add(currentUserId)
                    updatedCount = currentCount + 1
                }
                
                transaction.update(docRef, "supportedBy", updatedSupportedBy)
                transaction.update(docRef, "supportCount", updatedCount)
            }.addOnFailureListener {
                // Fallback locally if network fails
                localPosts.value = localPosts.value.map { post ->
                    if (post.id == postId) {
                        val toggled = !post.isSupportedByUser
                        val newCount = if (toggled) post.supportCount + 1 else (post.supportCount - 1).coerceAtLeast(0)
                        post.copy(supportCount = newCount, isSupportedByUser = toggled)
                    } else {
                        post
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun addPost(text: String) {
        val currentUser = auth?.currentUser
        val authorName = currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }?.let { "$it (Mindful Explorer)" } ?: "You (Mindful Explorer)"
        val initials = if (currentUser?.email != null) {
            currentUser.email!!.take(2).uppercase(Locale.ROOT)
        } else {
            "ME"
        }

        val db = firestore
        if (db == null) {
            val newPost = CommunityPost(
                id = UUID.randomUUID().toString(),
                authorName = authorName,
                authorInitials = initials,
                authorColorHex = "E2F1AF", // Bento Accent Green
                timestamp = "Just Now",
                text = text,
                supportCount = 0,
                isSupportedByUser = false
            )
            localPosts.value = listOf(newPost) + localPosts.value
            return
        }

        val postMap = hashMapOf(
            "authorName" to authorName,
            "authorInitials" to initials,
            "authorColorHex" to "E2F1AF", // Bento Accent Green
            "text" to text,
            "supportCount" to 0,
            "supportedBy" to emptyList<String>(),
            "createdAt" to System.currentTimeMillis()
        )
        try {
            db.collection("posts").add(postMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
