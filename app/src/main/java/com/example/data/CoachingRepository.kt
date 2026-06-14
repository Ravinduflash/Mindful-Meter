package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

data class Coach(
    val id: String,
    val name: String,
    val title: String,
    val specialty: String,
    val rating: Float,
    val experienceYears: Int,
    val avatarColorHex: String,
    val availabilityToday: String
)

data class PremiumBenefit(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
)

interface CoachingRepository {
    fun getFeaturedCoaches(): Flow<List<Coach>>
    fun getPremiumBenefits(): Flow<List<PremiumBenefit>>
    fun isPremiumSubscribed(): Flow<Boolean>
    suspend fun subscribeToPremium()
    suspend fun unsubscribeFromPremium()
}

class OfflineCoachingRepository : CoachingRepository {
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

    private val staticCoaches = listOf(
        Coach(
            id = "c_1",
            name = "Dr. Aris Thorne",
            title = "Sleep & Circadian Rhythm Specialist",
            specialty = "Insomnia Recovery, Chronobiology",
            rating = 4.9f,
            experienceYears = 12,
            avatarColorHex = "DEE9FF",
            availabilityToday = "Available 4:00 PM"
        ),
        Coach(
            id = "c_2",
            name = "Elena Rostova",
            title = "Somatic Breathwork & Trauma Guide",
            specialty = "Vagus Nerve Activation, Soft Flow",
            rating = 4.8f,
            experienceYears = 8,
            avatarColorHex = "FFE8D6",
            availabilityToday = "Available 6:15 PM"
        ),
        Coach(
            id = "c_3",
            name = "Zen Master Ryu",
            title = "Vipassana Meditation Instructor",
            specialty = "Mindfulness-Based Stress Reduction",
            rating = 5.0f,
            experienceYears = 22,
            avatarColorHex = "E2F5E2",
            availabilityToday = "Available 10:00 AM"
        ),
        Coach(
            id = "c_4",
            name = "Sarah Jenkins",
            title = "Executive Focus & Clarity Coach",
            specialty = "Cognitive Load Management, ADHD",
            rating = 4.7f,
            experienceYears = 9,
            avatarColorHex = "FFE2EB",
            availabilityToday = "Available Tomorrow"
        )
    )

    private val staticBenefits = listOf(
        PremiumBenefit(
            id = "b_1",
            title = "1-on-1 Coaching Access",
            description = "Book unlimited weekly real-time video consults with certified specialists.",
            iconName = "video"
        ),
        PremiumBenefit(
            id = "b_2",
            title = "Advanced Sleep Analytics",
            description = "Integrate biometric data streams to visualize precise heart-rate variability correlations.",
            iconName = "analytics"
        ),
        PremiumBenefit(
            id = "b_3",
            title = "Exclusive Audio Catalog",
            description = "Access scientifically vetted, ultra-low frequency brainwave synchronizing files.",
            iconName = "library"
        ),
        PremiumBenefit(
            id = "b_4",
            title = "Immersive Focus Labs",
            description = "Collaboratively practice guided deep-focus block working alongside peers.",
            iconName = "group"
        )
    )

    private val localIsPremium = MutableStateFlow(false)

    init {
        try {
            val db = firestore
            if (db != null) {
                db.collection("coaches").limit(1).get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (coach in staticCoaches) {
                            db.collection("coaches").document(coach.id).set(coach)
                        }
                    }
                }
                db.collection("benefits").limit(1).get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (benefit in staticBenefits) {
                            db.collection("benefits").document(benefit.id).set(benefit)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun getFeaturedCoaches(): Flow<List<Coach>> {
        val db = firestore ?: return kotlinx.coroutines.flow.flowOf(staticCoaches)
        return callbackFlow {
            val registration = db.collection("coaches")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val coachesList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val name = doc.getString("name") ?: ""
                                val title = doc.getString("title") ?: ""
                                val specialty = doc.getString("specialty") ?: ""
                                val rating = doc.getDouble("rating")?.toFloat() ?: 4.5f
                                val experienceYears = doc.getLong("experienceYears")?.toInt() ?: 5
                                val avatarColorHex = doc.getString("avatarColorHex") ?: "DEE9FF"
                                val availabilityToday = doc.getString("availabilityToday") ?: "Available"
                                Coach(id, name, title, specialty, rating, experienceYears, avatarColorHex, availabilityToday)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (coachesList.isNotEmpty()) {
                            trySend(coachesList)
                        } else {
                            trySend(staticCoaches)
                        }
                    } else {
                        trySend(staticCoaches)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    override fun getPremiumBenefits(): Flow<List<PremiumBenefit>> {
        val db = firestore ?: return kotlinx.coroutines.flow.flowOf(staticBenefits)
        return callbackFlow {
            val registration = db.collection("benefits")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val benefitsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val title = doc.getString("title") ?: ""
                                val description = doc.getString("description") ?: ""
                                val iconName = doc.getString("iconName") ?: ""
                                PremiumBenefit(id, title, description, iconName)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (benefitsList.isNotEmpty()) {
                            trySend(benefitsList)
                        } else {
                            trySend(staticBenefits)
                        }
                    } else {
                        trySend(staticBenefits)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    override fun isPremiumSubscribed(): Flow<Boolean> {
        val db = firestore ?: return localIsPremium
        val currentUser = auth?.currentUser ?: return localIsPremium
        return callbackFlow {
            val registration = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        val isSubscribed = snapshot.getBoolean("isPremiumSubscribed") ?: false
                        trySend(isSubscribed)
                    } else {
                        trySend(false)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun subscribeToPremium() {
        val db = firestore
        if (db == null) {
            localIsPremium.value = true
            return
        }
        val currentUser = auth?.currentUser ?: return
        try {
            db.collection("users").document(currentUser.uid)
                .set(mapOf("isPremiumSubscribed" to true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun unsubscribeFromPremium() {
        val db = firestore
        if (db == null) {
            localIsPremium.value = false
            return
        }
        val currentUser = auth?.currentUser ?: return
        try {
            db.collection("users").document(currentUser.uid)
                .set(mapOf("isPremiumSubscribed" to false))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
