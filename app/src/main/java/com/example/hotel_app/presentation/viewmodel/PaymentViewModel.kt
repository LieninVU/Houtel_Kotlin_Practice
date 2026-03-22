package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _timeRemaining = MutableStateFlow(5)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _paymentResult = MutableStateFlow<PaymentResult?>(null)
    val paymentResult: StateFlow<PaymentResult?> = _paymentResult.asStateFlow()

    private val _amount = MutableStateFlow(0.0)
    val amount: StateFlow<Double> = _amount.asStateFlow()

    fun setAmount(value: Double) {
        _amount.value = value
    }

    fun startPayment() {
        viewModelScope.launch {
            _isLoading.value = true
            _paymentResult.value = null

            // Countdown timer
            for (i in 5 downTo 0) {
                _timeRemaining.value = i
                delay(1000)
            }

            // Simulate payment processing
            delay(2000)

            _isLoading.value = false
            _paymentResult.value = PaymentResult.Success(ResourceProvider.getString(R.string.payment_success_message))
        }
    }

    fun cancelPayment() {
        _paymentResult.value = PaymentResult.Cancelled(ResourceProvider.getString(R.string.payment_cancelled_message))
    }

    fun clearResult() {
        _paymentResult.value = null
    }
}

sealed class PaymentResult {
    data class Success(val message: String) : PaymentResult()
    data class Cancelled(val message: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}
