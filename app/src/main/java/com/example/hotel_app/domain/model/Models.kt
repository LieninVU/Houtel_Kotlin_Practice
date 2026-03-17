package com.example.hotel_app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val bookingHistory: List<Booking>
)

data class Room(
    val id: String,
    val type: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val isAvailable: Boolean
)

data class HotelService(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ServiceCategory,
    val price: Double,
    val imageUrl: String,
    val durationMinutes: Int,
    val isPopular: Boolean = false
)

enum class ServiceCategory {
    SPA,
    TRANSFER,
    FOOD,
    LEISURE,
    BUSINESS;

    fun displayName(): String = when (this) {
        SPA -> "SPA & Wellness"
        TRANSFER -> "Transfer"
        FOOD -> "Food & Drinks"
        LEISURE -> "Leisure"
        BUSINESS -> "Business"
    }
}

data class Review(
    val id: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class Booking(
    val id: String,
    val roomId: String,
    val checkIn: String,
    val checkOut: String,
    val status: BookingStatus
)

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
