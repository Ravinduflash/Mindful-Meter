package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    private val _coaches = listOf(
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

    private val _benefits = listOf(
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

    private val _isSubscribed = MutableStateFlow(false)

    override fun getFeaturedCoaches(): Flow<List<Coach>> = MutableStateFlow(_coaches)

    override fun getPremiumBenefits(): Flow<List<PremiumBenefit>> = MutableStateFlow(_benefits)

    override fun isPremiumSubscribed(): Flow<Boolean> = _isSubscribed.asStateFlow()

    override suspend fun subscribeToPremium() {
        _isSubscribed.value = true
    }

    override suspend fun unsubscribeFromPremium() {
        _isSubscribed.value = false
    }
}
