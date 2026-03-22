package com.example.hotel_app.domain.model

/**
 * Модель местоположения (широта и долгота).
 * Используется для отображения объектов на карте.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Маркер ресторана на карте.
 * Содержит всю необходимую информацию для отображения и взаимодействия.
 *
 * @property id Уникальный идентификатор ресторана
 * @property name Название ресторана
 * @property cuisine Тип кухни
 * @property rating Рейтинг (0.0 - 5.0)
 * @property distance Расстояние от отеля в километрах
 * @property coordinates Координаты ресторана
 * @property address Полный адрес
 * @property phone Номер телефона (опционально)
 */
data class RestaurantMarker(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val distance: Double, // km from hotel
    val coordinates: Location,
    val address: String,
    val phone: String? = null
) {
    /**
     * Форматированная информация для сниппета маркера на карте.
     */
    fun getSnippet(): String = "${cuisine} • ${rating}★ • ${distance} км"

    /**
     * Проверка, доступен ли ресторан (рейтинг > 0).
     */
    fun isAvailable(): Boolean = rating > 0
}
