package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.CommunityPost
import com.example.data.CommunityRepository
import com.example.data.LeaderboardUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommunityViewModel(private val repository: CommunityRepository) : ViewModel() {

    val posts: StateFlow<List<CommunityPost>> = repository.getPosts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val leaderboard: StateFlow<List<LeaderboardUser>> = repository.getLeaderboard().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleSupport(postId: String) {
        viewModelScope.launch {
            repository.supportPost(postId)
        }
    }

    fun addReflectingPost(text: String) {
        viewModelScope.launch {
            repository.addPost(text)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                CommunityViewModel(application.container.communityRepository)
            }
        }
    }
}
