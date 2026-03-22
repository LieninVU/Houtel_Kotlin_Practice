package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.Location
import com.example.hotel_app.domain.model.RestaurantMarker
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана карты.
 * 
 * ## Clean Architecture:
 * - Данные находятся в data слое (MockHotelRepository)
 * - Модели находятся в domain слое (RestaurantMarker, Location)
 * - ViewModel только управляет UI состоянием
 */
class MapsViewModel(private val repository: HotelRepository) : ViewModel() {

    // ✅ Данные из repository (data слой)
    private val _markers = MutableStateFlow<List<RestaurantMarker>>(emptyList())
    val markers: StateFlow<List<RestaurantMarker>> = _markers.asStateFlow()

    private val _selectedMarker = MutableStateFlow<RestaurantMarker?>(null)
    val selectedMarker: StateFlow<RestaurantMarker?> = _selectedMarker.asStateFlow()

    // ✅ Локация отеля из repository (data слой)
    val hotelLocation: Location = repository.getHotelLocation()

    init {
        loadRestaurantMarkers()
    }

    /**
     * Загрузка маркеров ресторанов из repository.
     */
    private fun loadRestaurantMarkers() {
        viewModelScope.launch {
            repository.getRestaurantMarkers().collect { markers ->
                _markers.value = markers
            }
        }
    }

    /**
     * Выбор маркера ресторана.
     */
    fun selectMarker(marker: RestaurantMarker) {
        _selectedMarker.value = marker
    }

    /**
     * Очистка выбора.
     */
    fun clearSelection() {
        _selectedMarker.value = null
    }

    /**
     * Построение маршрута до ресторана.
     */
    fun buildRouteToRestaurant() {
        viewModelScope.launch {
            selectedMarker.value?.let { marker ->
                repository.getRouteToRestaurant(marker.id)
                // Маршрут построен (можно показать уведомление)
            }
        }
    }

    /**
     * Звонок в ресторан.
     */
    fun callRestaurant() {
        viewModelScope.launch {
            selectedMarker.value?.let { marker ->
                repository.callRestaurant(marker.id)
                // Звонок инициирован (можно показать уведомление)
            }
        }
    }
}
