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
    val category: ServiceCategory,
    val price: Double,
    val imageUrl: String,
    val description: String = ""
)

enum class ServiceCategory {
    SPA, TRANSFER, FOOD, OTHER
}

fun ServiceCategory.getIcon(): String = when (this) {
    ServiceCategory.SPA -> "🧖"
    ServiceCategory.TRANSFER -> "🚗"
    ServiceCategory.FOOD -> "🍽️"
    ServiceCategory.OTHER -> "⭐"
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
    val roomNumber: String,
    val roomType: String,
    val guestName: String,
    val checkIn: String,
    val checkOut: String,
    val status: BookingStatus,
    val nfcKeyId: String? = null
)

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

data class NfcKey(
    val id: String,
    val roomNumber: String,
    val roomType: String,
    val isActive: Boolean,
    val validUntil: String,
    val lastUsed: String? = null
)

data class PaidService(
    val id: String,
    val serviceId: String,
    val title: String,
    val price: Double,
    val category: ServiceCategory,
    val paidAt: String,
    val status: PaymentStatus
)

enum class PaymentStatus {
    PENDING, PAID, CANCELLED
}
