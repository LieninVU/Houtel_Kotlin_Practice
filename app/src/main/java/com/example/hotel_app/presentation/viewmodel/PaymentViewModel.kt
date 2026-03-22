package com.example.hotel_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.models.HotelService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class PaymentViewModel : ViewModel() {
    
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    private var currentService: HotelService? = null
    
    fun startPayment(service: HotelService) {
        currentService = service
        _paymentState.value = PaymentState.Processing(service)
        
        viewModelScope.launch {
            delay(5000) // 5 секунд эмуляции платежа
            
            // Эмуляция успешного платежа
            if (service.price > 0) {
                _paymentState.value = PaymentState.Success(service)
            } else {
                _paymentState.value = PaymentState.Error("Ошибка оплаты: некорректная сумма")
            }
        }
    }
    
    fun resetPayment() {
        _paymentState.value = PaymentState.Idle
        currentService = null
    }
    
    sealed class PaymentState {
        object Idle : PaymentState()
        data class Processing(val service: HotelService) : PaymentState()
        data class Success(val service: HotelService) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }
}