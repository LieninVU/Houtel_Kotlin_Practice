package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.HotelService
import com.example.hotel_app.domain.model.ServiceCategory
import com.example.hotel_app.domain.model.getIcon
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _services = MutableStateFlow<List<HotelService>>(emptyList())
    val services: StateFlow<List<HotelService>> = _services.asStateFlow()

    private val _filteredServices = MutableStateFlow<List<HotelService>>(emptyList())
    val filteredServices: StateFlow<List<HotelService>> = _filteredServices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ServiceCategory?>(null)
    val selectedCategory: StateFlow<ServiceCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getServices().collect {
                _services.value = it
                applyFilters()
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: ServiceCategory?) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _services.value

        // Filter by category
        _selectedCategory.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filter by search query
        val query = _searchQuery.value.trim().lowercase()
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }

        _filteredServices.value = filtered
    }

    fun getServiceIcon(category: ServiceCategory): String = category.getIcon()
}
