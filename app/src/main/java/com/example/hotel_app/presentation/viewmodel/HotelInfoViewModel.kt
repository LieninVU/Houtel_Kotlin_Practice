package com.example.hotel_app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotel_app.data.parser.XlsEventParser
import com.example.hotel_app.data.preferences.UserPreferences
import com.example.hotel_app.domain.model.Event

class HotelInfoViewModel(context: Context) : ViewModel() {

    private val parser = XlsEventParser(context)
    private val userPrefs = UserPreferences.getInstance(context)

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _recommendations = MutableLiveData<List<Event>>()
    val recommendations: LiveData<List<Event>> = _recommendations

    init {
        loadEvents()
    }

    fun loadEvents() {
        val allEvents = parser.parseFromAssets()
        _events.value = allEvents
        _recommendations.value = getRecommendations(allEvents)
    }

    /**
     * Алгоритм рекомендаций:
     * 1. Если есть история просмотров — показываем события той же категории
     * 2. Иначе — случайные 3 события
     */
    private fun getRecommendations(events: List<Event>): List<Event> {
        val lastCategory = userPrefs.lastViewedCategory
        return if (lastCategory.isNotBlank()) {
            val byCat = events.filter { it.category == lastCategory }
            if (byCat.isNotEmpty()) byCat.take(5) else events.shuffled().take(3)
        } else {
            events.shuffled().take(3)
        }
    }

    fun onEventViewed(event: Event) {
        userPrefs.lastViewedCategory = event.category
        userPrefs.markEventViewed(event.title)
    }
}
