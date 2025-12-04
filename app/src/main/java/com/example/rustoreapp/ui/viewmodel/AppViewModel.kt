package com.example.rustoreapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rustoreapp.data.models.App
import com.example.rustoreapp.data.models.Category
import com.example.rustoreapp.data.repository.AppRepository
import com.example.rustoreapp.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    private val _apps = MutableStateFlow<List<App>>(emptyList())
    val apps: StateFlow<List<App>> = _apps.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    init {
        loadApps()
        loadOnboardingState()
    }

    private fun loadApps() {
        _apps.value = AppRepository.getAllApps()
    }

    private fun loadOnboardingState() {
        viewModelScope.launch {
            dataStoreManager.onboardingCompleted.collect { completed ->
                _onboardingCompleted.value = completed
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.saveOnboardingCompleted(true)
            _onboardingCompleted.value = true
        }
    }

    fun getAppById(id: Int): App? {
        return AppRepository.getAppById(id)
    }

    fun getCategories(): List<Category> {
        return AppRepository.getCategories()
    }

    fun filterByCategory(category: Category?) {
        _selectedCategory.value = category
        if (category == null) {
            _apps.value = AppRepository.getAllApps()
        } else {
            _apps.value = AppRepository.getAppsByCategory(category)
        }
    }
}