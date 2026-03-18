package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.domain.model.Booking
import com.example.hotel_app.domain.model.NfcKey
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.domain.repository.BookingResult
import com.example.hotel_app.domain.repository.HotelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom.asStateFlow()

    private val _checkInDate = MutableStateFlow<String?>(null)
    val checkInDate: StateFlow<String?> = _checkInDate.asStateFlow()

    private val _checkOutDate = MutableStateFlow<String?>(null)
    val checkOutDate: StateFlow<String?> = _checkOutDate.asStateFlow()

    private val _bookingEvent = MutableSharedFlow<BookingUiEvent>()
    val bookingEvent: SharedFlow<BookingUiEvent> = _bookingEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRoomsLoading = MutableStateFlow(false)
    val isRoomsLoading: StateFlow<Boolean> = _isRoomsLoading.asStateFlow()

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _isRoomsLoading.value = true
            repository.getAvailableRooms().collect { roomList ->
                _rooms.value = roomList
                _isRoomsLoading.value = false
            }
        }
    }

    fun selectRoom(room: Room) {
        _selectedRoom.value = room
    }

    fun setCheckInDate(date: String) {
        _checkInDate.value = date
    }

    fun setCheckOutDate(date: String) {
        _checkOutDate.value = date
    }

    fun isFormValid(guestName: String): Boolean {
        return _selectedRoom.value != null &&
                !_checkInDate.value.isNullOrBlank() &&
                !_checkOutDate.value.isNullOrBlank() &&
                guestName.isNotBlank()
    }

    fun calculateTotalPrice(): Double {
        val room = _selectedRoom.value ?: return 0.0
        val checkIn = _checkInDate.value ?: return 0.0
        val checkOut = _checkOutDate.value ?: return 0.0
        
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val startDate = sdf.parse(checkIn)
            val endDate = sdf.parse(checkOut)
            if (startDate != null && endDate != null) {
                val days = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                if (days > 0) room.price * days else room.price
            } else {
                room.price
            }
        } catch (e: Exception) {
            room.price
        }
    }

    fun createBooking(guestName: String) {
        val room = _selectedRoom.value
        val checkIn = _checkInDate.value
        val checkOut = _checkOutDate.value

        if (room == null) {
            viewModelScope.launch { 
                _bookingEvent.emit(BookingUiEvent.ValidationError("Please select a room")) 
            }
            return
        }

        if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) {
            viewModelScope.launch { 
                _bookingEvent.emit(BookingUiEvent.ValidationError("Please select check-in and check-out dates")) 
            }
            return
        }

        if (guestName.isBlank()) {
            viewModelScope.launch { 
                _bookingEvent.emit(BookingUiEvent.ValidationError("Please enter guest name")) 
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = repository.bookRoom(room.id, guestName, checkIn, checkOut)) {
                is BookingResult.Success -> {
                    _bookingEvent.emit(
                        BookingUiEvent.BookingSuccess(
                            booking = result.booking,
                            nfcKey = result.nfcKey,
                            message = "Booking confirmed! Your NFC key for room ${result.booking.roomNumber} is ready."
                        )
                    )
                    _selectedRoom.value = null
                    _checkInDate.value = null
                    _checkOutDate.value = null
                    loadRooms()
                }
                is BookingResult.Error -> {
                    _bookingEvent.emit(BookingUiEvent.BookingError(result.message))
                }
            }
            
            _isLoading.value = false
        }
    }

    sealed class BookingUiEvent {
        data class BookingSuccess(
            val booking: Booking,
            val nfcKey: NfcKey,
            val message: String
        ) : BookingUiEvent()
        
        data class BookingError(val message: String) : BookingUiEvent()
        data class ValidationError(val message: String) : BookingUiEvent()
    }
}
