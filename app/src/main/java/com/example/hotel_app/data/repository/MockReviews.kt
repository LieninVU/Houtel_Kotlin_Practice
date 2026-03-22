package com.hotel.app.utils

import com.hotel.app.models.Review
import java.util.*

object MockReviews {
    fun getMockReviews(): List<Review> {
        return listOf(
            Review(
                id = 1,
                userName = "Анна Петрова",
                rating = 5f,
                comment = "Отличный отель! Очень чисто, персонал вежливый. Обязательно вернусь!",
                date = Date(System.currentTimeMillis() - 86400000 * 2)
            ),
            Review(
                id = 2,
                userName = "Иван Сидоров",
                rating = 4f,
                comment = "Хороший отель, но завтраки могли бы быть разнообразнее.",
                date = Date(System.currentTimeMillis() - 86400000 * 5)
            ),
            Review(
                id = 3,
                userName = "Мария Иванова",
                rating = 5f,
                comment = "Прекрасное место для отдыха! Отдельное спасибо спа-центру.",
                date = Date(System.currentTimeMillis() - 86400000 * 7)
            )
        )
    }
}