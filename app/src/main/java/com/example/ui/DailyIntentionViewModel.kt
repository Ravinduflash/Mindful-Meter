package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.DailyIntention
import com.example.data.DailyIntentionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DailyIntentionViewModel(private val repository: DailyIntentionRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(getTodayDateStr())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val currentIntentions: StateFlow<List<DailyIntention>> = _selectedDate
        .flatMapLatest { date ->
            repository.getIntentionsForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historicalIntentions: StateFlow<List<DailyIntention>> = repository.getAllIntentions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setIntentions(goal1: String, goal2: String, goal3: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            // We insert three distinct intentions for this day
            if (goal1.isNotBlank()) repository.insertIntention(DailyIntention(text = goal1, dateStr = date))
            if (goal2.isNotBlank()) repository.insertIntention(DailyIntention(text = goal2, dateStr = date))
            if (goal3.isNotBlank()) repository.insertIntention(DailyIntention(text = goal3, dateStr = date))
        }
    }

    fun toggleIntentionCompletion(intention: DailyIntention) {
        viewModelScope.launch {
            repository.updateCompletion(intention.id, !intention.isCompleted)
        }
    }

    fun deleteIntention(id: Int) {
        viewModelScope.launch {
            repository.deleteIntentionById(id)
        }
    }

    fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                DailyIntentionViewModel(application.container.dailyIntentionRepository)
            }
        }
    }
}
