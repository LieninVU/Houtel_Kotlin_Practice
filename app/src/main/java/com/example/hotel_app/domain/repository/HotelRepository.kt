package com.example.hotel_app.domain.repository

import com.example.hotel_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    fun getRooms(): Flow<List<Room>>
    fun getServices(): Flow<List<HotelService>>
    fun getReviews(): Flow<List<Review>>
    fun getCurrentUser(): Flow<User>
    suspend fun bookRoom(roomId: String, checkIn: String, checkOut: String): Boolean
}
