package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.Booking
import com.example.hotel_app.domain.model.BookingStatus
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookingViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bookingResult = MutableStateFlow<Result<String>?>(null)
    val bookingResult: StateFlow<Result<String>?> = _bookingResult.asStateFlow()

    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom.asStateFlow()

    private val _checkInDate = MutableStateFlow("")
    val checkInDate: StateFlow<String> = _checkInDate.asStateFlow()

    private val _checkOutDate = MutableStateFlow("")
    val checkOutDate: StateFlow<String> = _checkOutDate.asStateFlow()

    fun loadRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRooms().collect {
                _rooms.value = it
                _isLoading.value = false
            }
        }
    }

    fun selectRoom(room: Room) {
        _selectedRoom.value = room
    }

    fun setCheckInDate(date: Long) {
        _checkInDate.value = formatDate(date)
    }

    fun setCheckOutDate(date: Long) {
        _checkOutDate.value = formatDate(date)
    }

    fun bookRoom() {
        val room = _selectedRoom.value ?: run {
            _bookingResult.value = Result.failure(Exception("Выберите номер"))
            return
        }

        if (_checkInDate.value.isEmpty() || _checkOutDate.value.isEmpty()) {
            _bookingResult.value = Result.failure(Exception("Выберите даты заезда и выезда"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.bookRoom(room.id, _checkInDate.value, _checkOutDate.value)
            _isLoading.value = false
            _bookingResult.value = if (success) {
                Result.success("Бронирование успешно!")
            } else {
                Result.failure(Exception("Ошибка бронирования"))
            }
        }
    }

    fun clearBookingResult() {
        _bookingResult.value = null
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
