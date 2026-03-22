package com.hotel.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotel.app.models.HotelService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {
    
    private val _Services = MutableStateFlow<List<HotelService>>(emptyList())
    val Services: StateFlow<List<HotelService>> = _Services.asStateFlow()
    
    fun addService(service: HotelService) {
        viewModelScope.launch {
            val currentList = _Services.value.toMutableList()
            currentList.add(service)
            _Services.value = currentList
        }
    }
    
    fun removeService(serviceId: String) {
        viewModelScope.launch {
            _Services.value = _Services.value.filter { it.id != serviceId }
        }
    }
}