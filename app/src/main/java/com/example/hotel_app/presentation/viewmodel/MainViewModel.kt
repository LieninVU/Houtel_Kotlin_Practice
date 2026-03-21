package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.data.mock.ServiceCatalogMocks
import com.example.hotel_app.domain.model.HotelService
import com.example.hotel_app.domain.model.ServiceCategory
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _services = MutableStateFlow<List<HotelService>>(emptyList())
    val services: StateFlow<List<HotelService>> = _services.asStateFlow()

    private val _serviceCategories = MutableStateFlow(ServiceCatalogMocks.categories)
    val serviceCategories: StateFlow<List<ServiceCategory>> = _serviceCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isServicesLoading = MutableStateFlow(false)
    val isServicesLoading: StateFlow<Boolean> = _isServicesLoading.asStateFlow()

    init {
        loadRooms()
        loadServices()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRooms().collect {
                _rooms.value = it
                _isLoading.value = false
            }
        }
    }

    fun loadServices() {
        viewModelScope.launch {
            _isServicesLoading.value = true
            repository.getServices().collect {
                _services.value = it
                _isServicesLoading.value = false
            }
        }
    }
}
