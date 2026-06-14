package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.DailyHabit
import com.example.data.DailyHabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DailyHabitViewModel(private val repository: DailyHabitRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(getTodayDateStr())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val defaultHabits = listOf(
        "Drank Water💧",
        "Took Meds💊",
        "Stretched Exercise🧘",
        "Walked Outside🚶",
        "Read a Book📖"
    )

    val currentHabits: StateFlow<List<DailyHabit>> = _selectedDate
        .flatMapLatest { date ->
            repository.getHabitsForDate(date).onEach { list ->
                if (list.isEmpty()) {
                    viewModelScope.launch {
                        defaultHabits.forEach { name ->
                            repository.insertHabit(DailyHabit(name = name, dateStr = date))
                        }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun addCustomHabit(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertHabit(
                DailyHabit(
                    name = name.trim(),
                    dateStr = _selectedDate.value,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleHabitCompletion(habit: DailyHabit) {
        viewModelScope.launch {
            repository.updateCompletion(habit.id, !habit.isCompleted)
        }
    }

    fun deleteHabit(id: Int) {
        viewModelScope.launch {
            repository.deleteHabitById(id)
        }
    }

    fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                DailyHabitViewModel(application.container.dailyHabitRepository)
            }
        }
    }
}
