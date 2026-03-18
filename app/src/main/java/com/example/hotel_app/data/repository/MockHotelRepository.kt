package com.example.hotel_app.data.repository

import com.example.hotel_app.domain.model.*
import com.example.hotel_app.domain.repository.BookingResult
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.domain.repository.KeyAction
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class MockHotelRepository : HotelRepository {
    private val faker = Faker()
    private val random = Random()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    private val _rooms = MutableStateFlow<List<Room>>(generateInitialRooms())
    private val _nfcKeys = MutableStateFlow<List<NfcKey>>(emptyList())
    private val _activeBooking = MutableStateFlow<Booking?>(null)
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    private val baseUser = User(
        id = "user_123",
        name = faker.name.name(),
        email = faker.internet.email(),
        bookingHistory = emptyList()
    )

    private fun generateInitialRooms(): List<Room> {
        val roomTypes = listOf(
            "Standard" to 120.0,
            "Deluxe" to 200.0,
            "Suite" to 350.0,
            "Presidential" to 800.0
        )
        
        val descriptions = mapOf(
            "Standard" to "Comfortable room with essential amenities. Queen bed, TV, Wi-Fi, bathroom.",
            "Deluxe" to "Spacious room with premium amenities. King bed, work desk, minibar, city view.",
            "Suite" to "Luxurious suite with separate living area. King bed, jacuzzi, panoramic view.",
            "Presidential" to "Ultimate luxury experience. Two bedrooms, private terrace, butler service."
        )
        
        return listOf(
            Room("room_101", "Standard", descriptions["Standard"]!!, 120.0, "https://picsum.photos/seed/101/400/300", true),
            Room("room_102", "Standard", descriptions["Standard"]!!, 120.0, "https://picsum.photos/seed/102/400/300", true),
            Room("room_201", "Deluxe", descriptions["Deluxe"]!!, 200.0, "https://picsum.photos/seed/201/400/300", true),
            Room("room_202", "Deluxe", descriptions["Deluxe"]!!, 220.0, "https://picsum.photos/seed/202/400/300", true),
            Room("room_301", "Suite", descriptions["Suite"]!!, 350.0, "https://picsum.photos/seed/301/400/300", true),
            Room("room_302", "Suite", descriptions["Suite"]!!, 380.0, "https://picsum.photos/seed/302/400/300", false),
            Room("room_401", "Presidential", descriptions["Presidential"]!!, 800.0, "https://picsum.photos/seed/401/400/300", true),
            Room("room_402", "Presidential", descriptions["Presidential"]!!, 950.0, "https://picsum.photos/seed/402/400/300", false)
        )
    }

    override fun getRooms(): Flow<List<Room>> = _rooms

    override fun getAvailableRooms(): Flow<List<Room>> = _rooms.map { rooms ->
        rooms.filter { it.isAvailable }
    }

    override fun getServices(): Flow<List<HotelService>> = MutableStateFlow(
        listOf(
            HotelService("srv_1", "SPA Treatment", ServiceCategory.SPA, 80.0, "https://picsum.photos/seed/spa/200/200"),
            HotelService("srv_2", "Airport Transfer", ServiceCategory.TRANSFER, 45.0, "https://picsum.photos/seed/transfer/200/200"),
            HotelService("srv_3", "Breakfast Buffet", ServiceCategory.FOOD, 25.0, "https://picsum.photos/seed/breakfast/200/200"),
            HotelService("srv_4", "Gym Access", ServiceCategory.OTHER, 15.0, "https://picsum.photos/seed/gym/200/200"),
            HotelService("srv_5", "Room Service", ServiceCategory.FOOD, 35.0, "https://picsum.photos/seed/room/200/200"),
            HotelService("srv_6", "Laundry", ServiceCategory.OTHER, 20.0, "https://picsum.photos/seed/laundry/200/200")
        )
    )

    override fun getReviews(): Flow<List<Review>> = MutableStateFlow(
        List(5) {
            Review(
                id = UUID.randomUUID().toString(),
                userName = faker.name.name(),
                rating = 3 + random.nextInt(3),
                comment = listOf(
                    "Had a wonderful stay. The staff was very helpful and the room was clean.",
                    "Great location and excellent service. Will definitely come back!",
                    "The room was spacious and comfortable. Breakfast was amazing.",
                    "Perfect hotel for business trips. Fast Wi-Fi and quiet rooms.",
                    "Beautiful view from the room. The pool area is fantastic."
                ).random(),
                date = "2026-03-${10 + it}"
            )
        }
    )

    override fun getCurrentUser(): Flow<User> = _bookings.map { bookings ->
        baseUser.copy(bookingHistory = bookings)
    }

    override fun getActiveBooking(): Flow<Booking?> = _activeBooking

    override fun getBookings(): Flow<List<Booking>> = _bookings

    override suspend fun bookRoom(
        roomId: String,
        guestName: String,
        checkIn: String,
        checkOut: String
    ): BookingResult {
        delay(1000)
        
        val room = _rooms.value.find { it.id == roomId }
            ?: return BookingResult.Error("Room not found")
        
        if (!room.isAvailable) {
            return BookingResult.Error("Room is not available")
        }
        
        val roomNumber = roomId.removePrefix("room_")
        val bookingId = UUID.randomUUID().toString()
        val nfcKeyId = UUID.randomUUID().toString()
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val validUntil = dateFormat.format(calendar.time)
        
        val nfcKey = NfcKey(
            id = nfcKeyId,
            roomNumber = roomNumber,
            roomType = room.type,
            isActive = true,
            validUntil = validUntil
        )
        
        val booking = Booking(
            id = bookingId,
            roomId = roomId,
            roomNumber = roomNumber,
            roomType = room.type,
            guestName = guestName,
            checkIn = checkIn,
            checkOut = checkOut,
            status = BookingStatus.CONFIRMED,
            nfcKeyId = nfcKeyId
        )
        
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(isAvailable = false) else it
        }
        
        val currentKeys = _nfcKeys.value.toMutableList()
        currentKeys.add(nfcKey)
        _nfcKeys.value = currentKeys
        
        val currentBookings = _bookings.value.toMutableList()
        currentBookings.add(booking)
        _bookings.value = currentBookings
        _activeBooking.value = booking
        
        return BookingResult.Success(booking, nfcKey)
    }

    override fun getNfcKeys(): Flow<List<NfcKey>> = _nfcKeys

    override suspend fun activateNfcKey(bookingId: String): Boolean {
        delay(500)
        return true
    }

    override suspend fun useKeyAction(keyId: String, action: KeyAction): Boolean {
        delay(800)
        _nfcKeys.value = _nfcKeys.value.map { key ->
            if (key.id == keyId) {
                key.copy(lastUsed = timeFormat.format(Date()))
            } else key
        }
        return true
    }
}
