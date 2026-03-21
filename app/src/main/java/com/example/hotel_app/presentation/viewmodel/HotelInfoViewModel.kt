package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HotelInfoViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _hotelInfo = MutableStateFlow(HotelInfoData())
    val hotelInfo: StateFlow<HotelInfoData> = _hotelInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHotelInfo()
    }

    fun loadHotelInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            // Mock data - in real app would parse XLS or fetch from API
            _hotelInfo.value = HotelInfoData(
                name = "Grand Hotel",
                address = "ул. Примерная, 123, Москва",
                phone = "+7 (495) 123-45-67",
                email = "info@grandhotel.ru",
                checkInTime = "14:00",
                checkOutTime = "12:00",
                description = "Роскошный отель в центре города с прекрасным видом на парк.",
                facilities = listOf(
                    "Бесплатный Wi-Fi",
                    "Фитнес-центр",
                    "SPA-салон",
                    "Ресторан",
                    "Бар",
                    "Конференц-залы",
                    "Парковка",
                    "Трансфер до аэропорта"
                ),
                schedule = ScheduleData(
                    breakfast = "07:00 - 10:00",
                    lunch = "12:00 - 15:00",
                    dinner = "18:00 - 22:00",
                    spa = "09:00 - 21:00",
                    gym = "06:00 - 23:00"
                )
            )
            _isLoading.value = false
        }
    }
}

data class HotelInfoData(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val description: String = "",
    val facilities: List<String> = emptyList(),
    val schedule: ScheduleData = ScheduleData()
)

data class ScheduleData(
    val breakfast: String = "",
    val lunch: String = "",
    val dinner: String = "",
    val spa: String = "",
    val gym: String = ""
)
