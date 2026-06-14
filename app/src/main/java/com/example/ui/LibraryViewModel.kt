package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.LibraryItem
import com.example.data.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Articles") // "Articles", "Mini-Courses", "Guides", "Bookmarks"
    val selectedCategory: StateFlow<String> = _selectedCategory

    // Combined library list enriched with bookmark state & filtered by query and category
    val filteredItems: StateFlow<List<LibraryItem>> = combine(
        repository.getLibraryItems(),
        repository.getBookmarkedItemIds(),
        _searchQuery,
        _selectedCategory
    ) { rawItems, bookmarkedIds, query, category ->
        rawItems.map { item ->
            item.copy(isBookmarked = bookmarkedIds.contains(item.id))
        }.filter { item ->
            // Category filter or Bookmark tab filter
            val matchesCategory = if (category == "Bookmarks") {
                item.isBookmarked
            } else {
                item.category == category
            }

            // Search query filter (ignore case)
            val matchesQuery = query.isEmpty() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            repository.toggleBookmark(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                LibraryViewModel(application.container.libraryRepository)
            }
        }
    }
}
