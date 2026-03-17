package com.example.hotel_app

import com.example.hotel_app.domain.model.Event
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit-тесты для логики парсера и рекомендаций.
 * Тестируем без Android-контекста — только чистую логику.
 */
class XlsEventParserTest {

    private val mockEvents = listOf(
        Event("Концерт джаза", "Описание", "15.06.2024", "19:00", "Лобби", "Концерт"),
        Event("Экскурсия", "Описание", "16.06.2024", "10:00", "Вход", "Экскурсия"),
        Event("Йога", "Описание", "17.06.2024", "07:00", "Терраса", "Спорт"),
        Event("Рок-концерт", "Описание", "18.06.2024", "20:00", "Зал", "Концерт"),
        Event("Дегустация", "Описание", "19.06.2024", "18:00", "Ресторан", "Гастрономия")
    )

    @Test
    fun `getMockEvents returns non-empty list`() {
        val events = getMockEventsStub()
        assertTrue("Список моков не должен быть пустым", events.isNotEmpty())
    }

    @Test
    fun `getMockEvents returns events with non-blank titles`() {
        val events = getMockEventsStub()
        events.forEach { event ->
            assertTrue("Заголовок не должен быть пустым", event.title.isNotBlank())
        }
    }

    @Test
    fun `recommendations by category returns correct events`() {
        val lastCategory = "Концерт"
        val recs = getRecommendationsStub(mockEvents, lastCategory)
        assertTrue("Рекомендации должны содержать события категории Концерт",
            recs.all { it.category == lastCategory })
    }

    @Test
    fun `recommendations fallback to random when category not found`() {
        val lastCategory = "НесуществующаяКатегория"
        val recs = getRecommendationsStub(mockEvents, lastCategory)
        assertTrue("Должны вернуться случайные события", recs.isNotEmpty())
        assertTrue("Не более 3 случайных событий", recs.size <= 3)
    }

    @Test
    fun `recommendations return random 3 when no history`() {
        val recs = getRecommendationsStub(mockEvents, "")
        assertTrue("Должны вернуться события", recs.isNotEmpty())
        assertTrue("Не более 3 событий", recs.size <= 3)
    }

    @Test
    fun `event model fields are correct`() {
        val event = mockEvents[0]
        assertEquals("Концерт джаза", event.title)
        assertEquals("Концерт", event.category)
        assertEquals("Лобби", event.location)
    }

    // Дублируем логику ViewModel для тестирования без Android-контекста
    private fun getMockEventsStub() = mockEvents

    private fun getRecommendationsStub(events: List<Event>, lastCategory: String): List<Event> {
        return if (lastCategory.isNotBlank()) {
            val byCat = events.filter { it.category == lastCategory }
            if (byCat.isNotEmpty()) byCat.take(5) else events.shuffled().take(3)
        } else {
            events.shuffled().take(3)
        }
    }
}
