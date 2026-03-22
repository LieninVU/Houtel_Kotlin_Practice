package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Единое состояние UI для экрана бронирования.
 * Используется immutable data class для предсказуемости.
 */
data class BookingUiState(
    val rooms: List<Room> = emptyList(),
    val selectedRoom: Room? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guestName: String = "",
    val isRoomsLoading: Boolean = false,
    val isBookingLoading: Boolean = false,
    val error: String? = null
) {
    /**
     * Вычисляемое свойство: валидна ли форма для бронирования.
     * Инкапсулирует логику валидации внутри состояния.
     */
    val isFormValid: Boolean
        get() = selectedRoom != null &&
                !checkInDate.isNullOrBlank() &&
                !checkOutDate.isNullOrBlank() &&
                guestName.isNotBlank()

    /**
     * Вычисляемая стоимость бронирования.
     * Возвращает 0.0, если недостаточно данных для расчёта.
     */
    val totalPrice: Double
        get() {
            val room = selectedRoom ?: return 0.0
            val checkIn = checkInDate ?: return 0.0
            val checkOut = checkOutDate ?: return 0.0

            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
}

/**
 * События от UI (Actions) — что пользователь сделал.
 * MVI-подход: UI отправляет действия, ViewModel обрабатывает.
 */
sealed class BookingAction {
    data class SelectRoom(val room: Room) : BookingAction()
    data class SetCheckInDate(val date: String) : BookingAction()
    data class SetCheckOutDate(val date: String) : BookingAction()
    data class SetGuestName(val name: String) : BookingAction()
    data object CreateBooking : BookingAction()
    data object LoadRooms : BookingAction()
    data object ClearError : BookingAction()
}

/**
 * Одноразовые события от ViewModel к UI (навигация, тоасты, диалоги).
 * Используем SharedFlow для событий, которые должны быть обработаны один раз.
 */
sealed class BookingEvent {
    data class BookingSuccess(
        val booking: Booking,
        val nfcKey: NfcKey,
        val message: String
    ) : BookingEvent()

    data class NavigateToPayment(
        val booking: Booking,
        val nfcKey: NfcKey,
        val amount: Double
    ) : BookingEvent()

    data class BookingError(val message: String) : BookingEvent()
    data class ValidationError(val message: String) : BookingEvent()
}

class BookingViewModel(private val repository: HotelRepository) : ViewModel() {

    // ✅ ХОРОШО: Единый StateFlow для всего состояния UI
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    // ✅ ХОРОШО: SharedFlow только для одноразовых событий (навигация, ошибки)
    private val _event = MutableSharedFlow<BookingEvent>()
    val event: SharedFlow<BookingEvent> = _event.asSharedFlow()

    // ✅ ХОРОШО: Холодный Flow для загрузки комнат — создаётся по подписке
    val roomsFlow = repository.getAvailableRooms()
        .map { rooms -> rooms }

    init {
        loadRooms()
    }

    /**
     * Обработка действий от UI.
     * Централизованная логика — все изменения состояния в одном месте.
     */
    fun onAction(action: BookingAction) {
        when (action) {
            is BookingAction.SelectRoom -> {
                _state.value = _state.value.copy(selectedRoom = action.room)
            }
            is BookingAction.SetCheckInDate -> {
                _state.value = _state.value.copy(checkInDate = action.date)
            }
            is BookingAction.SetCheckOutDate -> {
                _state.value = _state.value.copy(checkOutDate = action.date)
            }
            is BookingAction.SetGuestName -> {
                _state.value = _state.value.copy(guestName = action.name)
            }
            is BookingAction.CreateBooking -> {
                createBooking()
            }
            is BookingAction.LoadRooms -> {
                loadRooms()
            }
            is BookingAction.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRoomsLoading = true, error = null)
            repository.getAvailableRooms().collect { roomList ->
                _state.value = _state.value.copy(
                    rooms = roomList,
                    isRoomsLoading = false
                )
            }
        }
    }

    private fun createBooking() {
        val currentState = _state.value

        // ✅ Валидация через вычисляемые свойства состояния
        if (currentState.selectedRoom == null) {
            viewModelScope.launch {
                _event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_room)))
            }
            return
        }

        if (currentState.checkInDate.isNullOrBlank() || currentState.checkOutDate.isNullOrBlank()) {
            viewModelScope.launch {
                _event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_dates)))
            }
            return
        }

        if (currentState.guestName.isBlank()) {
            viewModelScope.launch {
                _event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_guest_name)))
            }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isBookingLoading = true, error = null)

            when (val result = repository.bookRoom(
                roomId = currentState.selectedRoom!!.id,
                guestName = currentState.guestName,
                checkIn = currentState.checkInDate!!,
                checkOut = currentState.checkOutDate!!
            )) {
                is BookingResult.Success -> {
                    _event.emit(
                        BookingEvent.NavigateToPayment(
                            booking = result.booking,
                            nfcKey = result.nfcKey,
                            amount = currentState.totalPrice
                        )
                    )
                }
                is BookingResult.Error -> {
                    _event.emit(BookingEvent.BookingError(result.message))
                    _state.value = _state.value.copy(error = result.message)
                }
            }

            _state.value = _state.value.copy(isBookingLoading = false)
        }
    }
}
