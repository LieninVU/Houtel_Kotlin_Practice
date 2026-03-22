package com.example.hotel_app.domain.repository

import com.example.hotel_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    fun getRooms(): Flow<List<Room>>
    fun getAvailableRooms(): Flow<List<Room>>
    fun getServices(): Flow<List<HotelService>>
    fun getReviews(): Flow<List<Review>>
    fun getCurrentUser(): Flow<User>
    fun getActiveBooking(): Flow<Booking?>
    fun getBookings(): Flow<List<Booking>>

    suspend fun bookRoom(
        roomId: String,
        guestName: String,
        checkIn: String,
        checkOut: String
    ): BookingResult

    fun getNfcKeys(): Flow<List<NfcKey>>
    suspend fun activateNfcKey(bookingId: String): Boolean
    suspend fun useKeyAction(keyId: String, action: KeyAction): Boolean

    // Paid services
    fun getPaidServices(): Flow<List<PaidService>>
    suspend fun payForService(service: HotelService): PaymentResult

    // Map & Restaurants
    fun getHotelLocation(): Location
    fun getRestaurantMarkers(): Flow<List<RestaurantMarker>>
    suspend fun getRouteToRestaurant(markerId: String): String?
    suspend fun callRestaurant(markerId: String): String?
}

sealed class BookingResult {
    data class Success(val booking: Booking, val nfcKey: NfcKey) : BookingResult()
    data class Error(val message: String) : BookingResult()
}

sealed class PaymentResult {
    data class Success(val paidService: PaidService) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}

enum class KeyAction {
    OPEN_DOOR, CLOSE_DOOR, LIGHTS_ON, LIGHTS_OFF, POWER_ON, POWER_OFF
}
