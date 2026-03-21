package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapsViewModel : ViewModel() {

    private val _markers = MutableStateFlow<List<RestaurantMarker>>(getRestaurantMarkers())
    val markers: StateFlow<List<RestaurantMarker>> = _markers.asStateFlow()

    private val _selectedMarker = MutableStateFlow<RestaurantMarker?>(null)
    val selectedMarker: StateFlow<RestaurantMarker?> = _selectedMarker.asStateFlow()

    // Hotel location (mock - center of Moscow)
    val hotelLocation = Location(55.751244, 37.618423)

    fun selectMarker(marker: RestaurantMarker) {
        _selectedMarker.value = marker
    }

    fun clearSelection() {
        _selectedMarker.value = null
    }

    private fun getRestaurantMarkers(): List<RestaurantMarker> = listOf(
        RestaurantMarker(
            id = "1",
            name = "Кафе Пушкинъ",
            cuisine = "Русская",
            rating = 4.8,
            distance = 0.5,
            latitude = 55.760244,
            longitude = 37.605423,
            address = "Тверской бульвар, 26А"
        ),
        RestaurantMarker(
            id = "2",
            name = "White Rabbit",
            cuisine = "Европейская",
            rating = 4.9,
            distance = 1.2,
            latitude = 55.747244,
            longitude = 37.590423,
            address = "Смоленская площадь, 3"
        ),
        RestaurantMarker(
            id = "3",
            name = "Турандот",
            cuisine = "Паназиатская",
            rating = 4.7,
            distance = 0.8,
            latitude = 55.755244,
            longitude = 37.610423,
            address = "Тверской бульвар, 26"
        ),
        RestaurantMarker(
            id = "4",
            name = "Сыроварня",
            cuisine = "Итальянская",
            rating = 4.5,
            distance = 0.3,
            latitude = 55.749244,
            longitude = 37.615423,
            address = "ул. Петровка, 15"
        ),
        RestaurantMarker(
            id = "5",
            name = "Чайхона №1",
            cuisine = "Узбекская",
            rating = 4.4,
            distance = 1.5,
            latitude = 55.743244,
            longitude = 37.625423,
            address = "ул. Большая Дмитровка, 35"
        )
    )
}

data class RestaurantMarker(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val distance: Double, // km from hotel
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
