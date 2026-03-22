package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import com.example.hotel_app.domain.model.NfcKey
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.domain.repository.KeyAction
import com.example.hotel_app.presentation.ui.NfcNotificationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NfcViewModel(
    private val repository: HotelRepository,
    private val notificationManager: NfcNotificationManager
) : ViewModel() {

    private val _nfcKeys = MutableStateFlow<List<NfcKey>>(emptyList())
    val nfcKeys: StateFlow<List<NfcKey>> = _nfcKeys.asStateFlow()

    private val _nfcEvent = MutableSharedFlow<String>()
    val nfcEvent: SharedFlow<String> = _nfcEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNfcKeys()
    }

    fun loadNfcKeys() {
        viewModelScope.launch {
            repository.getNfcKeys().collect {
                _nfcKeys.value = it
            }
        }
    }

    fun performAction(keyId: String, action: KeyAction) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.useKeyAction(keyId, action)
            if (success) {
                val actionName = action.name.replace("_", " ").lowercase()
                val message = ResourceProvider.getString(R.string.nfc_scan_success_format, actionName, keyId)
                _nfcEvent.emit(message)

                if (action == KeyAction.OPEN_DOOR) {
                    val key = _nfcKeys.value.find { it.id == keyId }
                    key?.let {
                        notificationManager.showDoorOpenedNotification(it.roomNumber)
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun requestNewKey(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.activateNfcKey(bookingId)
            _isLoading.value = false
        }
    }
}
