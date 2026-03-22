package com.example.hotel_app.data.repository

import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import com.example.hotel_app.data.local.ReviewDao
import com.example.hotel_app.data.local.ReviewEntity
import com.example.hotel_app.domain.model.*
import com.example.hotel_app.domain.repository.BookingResult
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.domain.repository.KeyAction
import com.example.hotel_app.domain.repository.PaymentResult
import com.example.hotel_app.domain.model.PaymentStatus
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

/**
 * Результат валидации бронирования.
 */
private sealed class BookingValidationResult {
    data object Valid : BookingValidationResult()
    data class Error(val message: String) : BookingValidationResult()
}

/**
 * Успешный результат валидации с данными комнаты.
 * Наследует BookingValidationResult для использования в when.
 */
private data class ValidatedRoom(val room: Room) : BookingValidationResult()

class MockHotelRepository(
    private val reviewDao: ReviewDao
) : HotelRepository {
    private val faker = Faker()
    private val random = Random()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    private val _rooms = MutableStateFlow<List<Room>>(generateInitialRooms())
    private val _nfcKeys = MutableStateFlow<List<NfcKey>>(emptyList())
    private val _activeBooking = MutableStateFlow<Booking?>(null)
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    private val _paidServices = MutableStateFlow<List<PaidService>>(emptyList())
    private val baseUser = User(
        id = "user_123",
        name = faker.name.name(),
        email = faker.internet.email(),
        bookingHistory = emptyList()
    )

    private fun generateInitialRooms(): List<Room> {
        val roomTypes = listOf(
            ResourceProvider.getString(R.string.mock_room_type_standard) to 120.0,
            ResourceProvider.getString(R.string.mock_room_type_deluxe) to 200.0,
            ResourceProvider.getString(R.string.mock_room_type_suite) to 350.0,
            ResourceProvider.getString(R.string.mock_room_type_presidential) to 800.0
        )
        
        return listOf(
            Room("room_101", ResourceProvider.getString(R.string.mock_room_type_standard), ResourceProvider.getString(R.string.mock_room_description_standard), 120.0, "https://picsum.photos/seed/101/400/300", true),
            Room("room_102", ResourceProvider.getString(R.string.mock_room_type_standard), ResourceProvider.getString(R.string.mock_room_description_standard), 120.0, "https://picsum.photos/seed/102/400/300", true),
            Room("room_201", ResourceProvider.getString(R.string.mock_room_type_deluxe), ResourceProvider.getString(R.string.mock_room_description_deluxe), 200.0, "https://picsum.photos/seed/201/400/300", true),
            Room("room_202", ResourceProvider.getString(R.string.mock_room_type_deluxe), ResourceProvider.getString(R.string.mock_room_description_deluxe), 220.0, "https://picsum.photos/seed/202/400/300", true),
            Room("room_301", ResourceProvider.getString(R.string.mock_room_type_suite), ResourceProvider.getString(R.string.mock_room_description_suite), 350.0, "https://picsum.photos/seed/301/400/300", true),
            Room("room_302", ResourceProvider.getString(R.string.mock_room_type_suite), ResourceProvider.getString(R.string.mock_room_description_suite), 380.0, "https://picsum.photos/seed/302/400/300", false),
            Room("room_401", ResourceProvider.getString(R.string.mock_room_type_presidential), ResourceProvider.getString(R.string.mock_room_description_presidential), 800.0, "https://picsum.photos/seed/401/400/300", true),
            Room("room_402", ResourceProvider.getString(R.string.mock_room_type_presidential), ResourceProvider.getString(R.string.mock_room_description_presidential), 950.0, "https://picsum.photos/seed/402/400/300", false)
        )
    }

    override fun getRooms(): Flow<List<Room>> = _rooms

    override fun getAvailableRooms(): Flow<List<Room>> = _rooms.map { rooms ->
        rooms.filter { it.isAvailable }
    }

    override fun getServices(): Flow<List<HotelService>> = MutableStateFlow(
        listOf(
            HotelService("srv_1", ResourceProvider.getString(R.string.mock_service_spa), ServiceCategory.SPA, 80.0, "https://picsum.photos/seed/spa/200/200", ResourceProvider.getString(R.string.mock_service_description_spa)),
            HotelService("srv_2", ResourceProvider.getString(R.string.mock_service_transfer), ServiceCategory.TRANSFER, 45.0, "https://picsum.photos/seed/transfer/200/200", ResourceProvider.getString(R.string.mock_service_description_transfer)),
            HotelService("srv_3", ResourceProvider.getString(R.string.mock_service_breakfast), ServiceCategory.FOOD, 25.0, "https://picsum.photos/seed/breakfast/200/200", ResourceProvider.getString(R.string.mock_service_description_breakfast)),
            HotelService("srv_4", ResourceProvider.getString(R.string.mock_service_gym), ServiceCategory.OTHER, 15.0, "https://picsum.photos/seed/gym/200/200", ResourceProvider.getString(R.string.mock_service_description_gym)),
            HotelService("srv_5", ResourceProvider.getString(R.string.mock_service_room_service), ServiceCategory.FOOD, 35.0, "https://picsum.photos/seed/room/200/200", ResourceProvider.getString(R.string.mock_service_description_room_service)),
            HotelService("srv_6", ResourceProvider.getString(R.string.mock_service_laundry), ServiceCategory.OTHER, 20.0, "https://picsum.photos/seed/laundry/200/200", ResourceProvider.getString(R.string.mock_service_description_laundry))
        )
    )

    override fun getReviews(): Flow<List<Review>> = reviewDao.getAllReviews().map { entities ->
        entities.map { it.toReview() }
    }

    suspend fun saveReview(review: Review) {
        reviewDao.insertReview(review.toEntity())
    }

    suspend fun deleteReview(reviewId: String) {
        reviewDao.deleteReview(reviewId)
    }

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

        // ✅ Валидация через отдельный метод
        val room = when (val validation = validateBooking(roomId)) {
            is BookingValidationResult.Error -> return BookingResult.Error(validation.message)
            is ValidatedRoom -> validation.room
            BookingValidationResult.Valid -> return BookingResult.Error("Unexpected validation state")
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

        updateRoomAvailability(roomId, isAvailable = false)
        addNfcKey(nfcKey)
        addBooking(booking)

        return BookingResult.Success(booking, nfcKey)
    }

    /**
     * Валидация данных для бронирования.
     * Вынесена в отдельный метод для упрощения основной логики.
     */
    private fun validateBooking(roomId: String): BookingValidationResult {
        val room = _rooms.value.find { it.id == roomId }
            ?: return BookingValidationResult.Error(
                ResourceProvider.getString(R.string.mock_error_room_not_found)
            )

        if (!room.isAvailable) {
            return BookingValidationResult.Error(
                ResourceProvider.getString(R.string.mock_error_room_not_available)
            )
        }

        // Возвращаем комнату для дальнейшего использования
        return ValidatedRoom(room)
    }

    /**
     * Обновление доступности номера.
     * Вынесено в отдельный метод для читаемости.
     */
    private fun updateRoomAvailability(roomId: String, isAvailable: Boolean) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(isAvailable = isAvailable) else it
        }
    }

    /**
     * Добавление NFC ключа.
     * Вынесено в отдельный метод для читаемости.
     */
    private fun addNfcKey(key: NfcKey) {
        val currentKeys = _nfcKeys.value.toMutableList()
        currentKeys.add(key)
        _nfcKeys.value = currentKeys
    }

    /**
     * Добавление бронирования.
     * Вынесено в отдельный метод для читаемости.
     */
    private fun addBooking(booking: Booking) {
        val currentBookings = _bookings.value.toMutableList()
        currentBookings.add(booking)
        _bookings.value = currentBookings
        _activeBooking.value = booking
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

    override fun getPaidServices(): Flow<List<PaidService>> = _paidServices

    override suspend fun payForService(service: HotelService): PaymentResult {
        delay(1000) // Имитация обработки платежа

        val paidService = PaidService(
            id = "paid_${UUID.randomUUID()}",
            serviceId = service.id,
            title = service.title,
            price = service.price,
            category = service.category,
            paidAt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date()),
            status = PaymentStatus.PAID
        )

        val currentList = _paidServices.value.toMutableList()
        currentList.add(paidService)
        _paidServices.value = currentList

        return PaymentResult.Success(paidService)
    }

    // ✅ Map & Restaurants - данные в data слое
    override fun getHotelLocation(): Location {
        // Центр Москвы (моковая локация отеля)
        return Location(55.751244, 37.618423)
    }

    override fun getRestaurantMarkers(): Flow<List<RestaurantMarker>> = MutableStateFlow(
        listOf(
            RestaurantMarker(
                id = "1",
                name = "Кафе Пушкинъ",
                cuisine = "Русская",
                rating = 4.8,
                distance = 0.5,
                coordinates = Location(55.760244, 37.605423),
                address = "Тверской бульвар, 26А",
                phone = "+7 (495) 123-45-67"
            ),
            RestaurantMarker(
                id = "2",
                name = "White Rabbit",
                cuisine = "Европейская",
                rating = 4.9,
                distance = 1.2,
                coordinates = Location(55.747244, 37.590423),
                address = "Смоленская площадь, 3",
                phone = "+7 (495) 234-56-78"
            ),
            RestaurantMarker(
                id = "3",
                name = "Турандот",
                cuisine = "Паназиатская",
                rating = 4.7,
                distance = 0.8,
                coordinates = Location(55.755244, 37.610423),
                address = "Тверской бульвар, 26",
                phone = "+7 (495) 345-67-89"
            ),
            RestaurantMarker(
                id = "4",
                name = "Сыроварня",
                cuisine = "Итальянская",
                rating = 4.5,
                distance = 0.3,
                coordinates = Location(55.749244, 37.615423),
                address = "ул. Петровка, 15",
                phone = "+7 (495) 456-78-90"
            ),
            RestaurantMarker(
                id = "5",
                name = "Чайхона №1",
                cuisine = "Узбекская",
                rating = 4.4,
                distance = 1.5,
                coordinates = Location(55.743244, 37.625423),
                address = "ул. Большая Дмитровка, 35",
                phone = "+7 (495) 567-89-01"
            )
        )
    )

    override suspend fun getRouteToRestaurant(markerId: String): String? {
        delay(500) // Имитация загрузки маршрута
        return "Маршрут построен до ресторана"
    }

    override suspend fun callRestaurant(markerId: String): String? {
        delay(300) // Имитация звонка
        return "Звонок инициирован"
    }
}

// Extension functions for Review conversion
fun Review.toEntity(): ReviewEntity = ReviewEntity(
    id = id,
    userName = userName,
    rating = rating,
    comment = comment,
    date = date
)

fun ReviewEntity.toReview(): Review = Review(
    id = id,
    userName = userName,
    rating = rating,
    comment = comment,
    date = date
)
