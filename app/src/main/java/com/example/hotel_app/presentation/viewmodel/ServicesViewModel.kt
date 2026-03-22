package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.HotelService
import com.example.hotel_app.domain.model.ServiceCategory
import com.example.hotel_app.domain.model.getIcon
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.domain.repository.PaymentResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _paymentResult = MutableSharedFlow<PaymentUiState>()
    val paymentResult: SharedFlow<PaymentUiState> = _paymentResult.asSharedFlow()

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

    fun payForService(service: HotelService) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = repository.payForService(service)) {
                is PaymentResult.Success -> {
                    _paymentResult.emit(PaymentUiState.Success("Услуга '${service.title}' оплачена!"))
                }
                is PaymentResult.Error -> {
                    _paymentResult.emit(PaymentUiState.Error(result.message))
                }
            }
            
            _isLoading.value = false
        }
    }
}

sealed class PaymentUiState {
    data class Success(val message: String) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}
