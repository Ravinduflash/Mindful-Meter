package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.MoodLog
import com.example.data.MoodRepository
import com.example.data.AiInsightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiInsightUiState {
    object Loading : AiInsightUiState
    data class Success(val insight: String) : AiInsightUiState
    data class Error(val message: String) : AiInsightUiState
}

class MoodViewModel(
    private val repository: MoodRepository,
    private val aiInsightRepository: AiInsightRepository
) : ViewModel() {

    val allLogs: StateFlow<List<MoodLog>> = repository.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _aiInsightState = MutableStateFlow<AiInsightUiState>(AiInsightUiState.Loading)
    val aiInsightState: StateFlow<AiInsightUiState> = _aiInsightState.asStateFlow()

    init {
        fetchWeeklyInsight()
    }

    fun fetchWeeklyInsight(forceRefresh: Boolean = false) {
        if (!forceRefresh && _aiInsightState.value is AiInsightUiState.Success) {
            return
        }
        viewModelScope.launch {
            _aiInsightState.value = AiInsightUiState.Loading
            try {
                val insight = aiInsightRepository.getWeeklyInsight()
                _aiInsightState.value = AiInsightUiState.Success(insight)
            } catch (e: Exception) {
                _aiInsightState.value = AiInsightUiState.Error(e.localizedMessage ?: "Failed to load insight")
            }
        }
    }

    fun logMood(mood: String, note: String) {
        viewModelScope.launch {
            repository.insertLog(MoodLog(mood = mood, note = note))
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                MoodViewModel(
                    repository = application.container.moodRepository,
                    aiInsightRepository = application.container.aiInsightRepository
                )
            }
        }
    }
}
