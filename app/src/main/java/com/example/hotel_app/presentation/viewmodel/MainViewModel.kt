package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.Booking
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.domain.model.User
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _activeBooking = MutableStateFlow<Booking?>(null)
    val activeBooking: StateFlow<Booking?> = _activeBooking.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRooms()
        observeActiveBooking()
        observeBookings()
        observeUser()
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

    private fun observeActiveBooking() {
        viewModelScope.launch {
            repository.getActiveBooking().collect { booking ->
                _activeBooking.value = booking
            }
        }
    }

    private fun observeBookings() {
        viewModelScope.launch {
            repository.getBookings().collect { list ->
                _bookings.value = list
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            repository.getCurrentUser().collect { currentUser ->
                _user.value = currentUser
            }
        }
    }
}
