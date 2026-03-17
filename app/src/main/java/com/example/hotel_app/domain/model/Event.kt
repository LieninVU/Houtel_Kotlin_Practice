package com.example.hotel_app.domain.model

/**
 * Модель события/мероприятия отеля.
 * Структура соответствует колонкам XLS-файла:
 * | title | description | date | time | location | category |
 */
data class Event(
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String
)
