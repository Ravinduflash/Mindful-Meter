package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.Coach
import com.example.data.CoachingRepository
import com.example.data.PremiumBenefit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoachingViewModel(private val repository: CoachingRepository) : ViewModel() {

    val coaches: StateFlow<List<Coach>> = repository.getFeaturedCoaches().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val benefits: StateFlow<List<PremiumBenefit>> = repository.getPremiumBenefits().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isSubscribed: StateFlow<Boolean> = repository.isPremiumSubscribed().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun subscribe() {
        viewModelScope.launch {
            repository.subscribeToPremium()
        }
    }

    fun unsubscribe() {
        viewModelScope.launch {
            repository.unsubscribeFromPremium()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                CoachingViewModel(application.container.coachingRepository)
            }
        }
    }
}
