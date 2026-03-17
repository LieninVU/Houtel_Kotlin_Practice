package com.example.hotel_app.domain.repository

import com.example.hotel_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    fun getRooms(): Flow<List<Room>>
    fun getServices(): Flow<List<HotelService>>
    fun getReviews(): Flow<List<Review>>
    fun getCurrentUser(): Flow<User>
    suspend fun bookRoom(roomId: String, checkIn: String, checkOut: String): Boolean
    
    // NFC Key methods
    fun getNfcKeys(): Flow<List<NfcKey>>
    suspend fun activateNfcKey(bookingId: String): Boolean
    suspend fun useKeyAction(keyId: String, action: KeyAction): Boolean
}

enum class KeyAction {
    OPEN_DOOR, CLOSE_DOOR, LIGHTS_ON, LIGHTS_OFF, POWER_ON, POWER_OFF
}
