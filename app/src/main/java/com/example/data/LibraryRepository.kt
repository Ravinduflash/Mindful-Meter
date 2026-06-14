package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface LibraryRepository {
    fun getLibraryItems(): Flow<List<LibraryItem>>
    fun getBookmarkedItemIds(): Flow<Set<String>>
    suspend fun toggleBookmark(itemId: String)
}

class OfflineLibraryRepository : LibraryRepository {

    private val mutableBookmarkedIds = MutableStateFlow<Set<String>>(emptySet())

    private val items = listOf(
        // Articles
        LibraryItem(
            id = "art_1",
            title = "The Science of Deep Diaphragmatic Breath",
            category = "Articles",
            estimate = "4 min read",
            description = "Explore how engaging the vagus nerve through voluntary deep breathing lowers cortisol and returns your body to an effortless state of homeostasis."
        ),
        LibraryItem(
            id = "art_2",
            title = "Sleep Hygiene: Unlocking Restorative Circadian Rhythms",
            category = "Articles",
            estimate = "6 min read",
            description = "Crucial steps to lower light exposure, optimize timing, and prepare your nervous system for cellular repair. Designed for night owls."
        ),
        LibraryItem(
            id = "art_3",
            title = "Solfeggio Frequencies: Fact versus Fiction",
            category = "Articles",
            estimate = "5 min read",
            description = "An objective look at how specific sound frequencies, like 528Hz and 432Hz, interact with neurological calmness and brainwave synchrony."
        ),
        
        // Mini-Courses
        LibraryItem(
            id = "course_1",
            title = "7-Day Foundation: The Present Observer",
            category = "Mini-Courses",
            estimate = "7 days • 10m/day",
            description = "A progressive mini-course exploring simple vipassana principles. Perfect for beginners to understand observing without reaction."
        ),
        LibraryItem(
            id = "course_2",
            title = "Overcoming the Midnight Mind Race",
            category = "Mini-Courses",
            estimate = "3 days • 15m/day",
            description = "Guided cognitive offloading exercises designed for high-stress professionals who struggle with bedtime rumination."
        ),
        
        // Guides
        LibraryItem(
            id = "guide_1",
            title = "Quick Guide: Box Breathing Protocol",
            category = "Guides",
            estimate = "3 min guide",
            description = "A visual visualizer teaching the tactical holding pattern (4s inhale, 4s hold, 4s exhale, 4s hold) used by military personnel for rapid focus."
        ),
        LibraryItem(
            id = "guide_2",
            title = "Bento Workspace Setup for Visual Mindfulness",
            category = "Guides",
            estimate = "5 min guide",
            description = "How to redesign your interactive digital dashboard workspaces using calming, high-contrast visual blocks."
        )
    )

    override fun getLibraryItems(): Flow<List<LibraryItem>> {
        return MutableStateFlow(items)
    }

    override fun getBookmarkedItemIds(): Flow<Set<String>> {
        return mutableBookmarkedIds
    }

    override suspend fun toggleBookmark(itemId: String) {
        val current = mutableBookmarkedIds.value
        mutableBookmarkedIds.value = if (current.contains(itemId)) {
            current - itemId
        } else {
            current + itemId
        }
    }
}
