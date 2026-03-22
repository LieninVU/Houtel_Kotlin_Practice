package com.example.hotel_app.data.repository

import com.example.hotel_app.domain.model.*
import com.example.hotel_app.domain.repository.HotelRepository
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class MockHotelRepository : HotelRepository {
    private val faker = Faker()
    private val random = Random()

    override fun getRooms(): Flow<List<Room>> = flow {
        delay(500)
        val rooms = List(10) {
            Room(
                id = UUID.randomUUID().toString(),
                type = listOf("Standard", "Deluxe", "Suite", "Presidential").random(),
                description = "Spacious room with modern amenities and a beautiful view.",
                price = 100.0 + random.nextInt(900),
                imageUrl = "https://picsum.photos/seed/${random.nextInt(1000)}/400/300",
                isAvailable = random.nextBoolean()
            )
        }
        emit(rooms)
    }

    override fun getServices(): Flow<List<HotelService>> = flow {
        delay(500)
        val services = List(8) {
            HotelService(
                id = UUID.randomUUID().toString(),
                title = listOf("SPA Treatment", "Airport Transfer", "Breakfast Buffet", "Gym Access").random(),
                category = ServiceCategory.values().random(),
                price = 20.0 + random.nextInt(180),
                imageUrl = "https://picsum.photos/seed/${random.nextInt(1000)}/200/200"
            )
        }
        emit(services)
    }

    override fun getReviews(): Flow<List<Review>> = flow {
        delay(500)
        val reviews = List(5) {
            Review(
                id = UUID.randomUUID().toString(),
                userName = faker.name.name(),
                rating = 1 + random.nextInt(5),
                comment = "Had a wonderful stay. The staff was very helpful and the room was clean.",
                date = "2023-10-15"
            )
        }
        emit(reviews)
    }

    override fun getCurrentUser(): Flow<User> = flow {
        delay(500)
        val user = User(
            id = "user_123",
            name = faker.name.name(),
            email = faker.internet.email(),
            bookingHistory = emptyList()
        )
        emit(user)
    }

    override suspend fun bookRoom(roomId: String, checkIn: String, checkOut: String): Boolean {
        delay(500)
        return true
    }
}
