package com.example.hotel_app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import com.example.hotel_app.data.parser.XlsEventParser
import com.example.hotel_app.data.preferences.UserPreferences
import com.example.hotel_app.domain.model.Event
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class HotelInfoViewModel(context: Context) : ViewModel() {

    private val parser = XlsEventParser(context)
    private val userPrefs = UserPreferences.getInstance(context)

    private val _eventsState = MutableLiveData<UiState<List<Event>>>()
    val eventsState: LiveData<UiState<List<Event>>> = _eventsState

    private val _recommendations = MutableLiveData<List<Event>>()
    val recommendations: LiveData<List<Event>> = _recommendations

    // Удобный доступ к списку событий для Dashboard
    val events: LiveData<List<Event>> get() = MutableLiveData<List<Event>>().also { ld ->
        eventsState.observeForever { state ->
            if (state is UiState.Success) ld.value = state.data
        }
    }

    init {
        loadEvents()
    }

    /**
     * Загрузка событий из assets.
     * Использует viewModelScope.launch с Dispatchers.IO внутри parseFromAssets.
     */
    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = UiState.Loading

            val allEvents = try {
                // ✅ parseFromAssets теперь suspend и использует Dispatchers.IO
                parser.parseFromAssets()
            } catch (e: Exception) {
                _eventsState.value = UiState.Error("Ошибка загрузки: ${e.localizedMessage}")
                parser.getMockEvents()
            }

            _eventsState.value = when {
                allEvents.isEmpty() -> UiState.Error(ResourceProvider.getString(R.string.hotel_info_events_error))
                else -> UiState.Success(allEvents)
            }

            _recommendations.value = getRecommendations(allEvents)
        }
    }

    /**
     * Алгоритм рекомендаций:
     * 1. Если есть история — показываем события той же категории
     * 2. Иначе — случайные 3 события
     */
    private fun getRecommendations(events: List<Event>): List<Event> {
        val lastCategory = userPrefs.lastViewedCategory
        
        return when {
            lastCategory.isBlank() -> events.shuffled().take(3)
            else -> {
                val byCat = events.filter { it.category == lastCategory }
                when {
                    byCat.isNotEmpty() -> byCat.take(5)
                    else -> events.shuffled().take(3)
                }
            }
        }
    }

    fun onEventViewed(event: Event) {
        userPrefs.lastViewedCategory = event.category
        userPrefs.markEventViewed(event.title)
        // Обновляем рекомендации после просмотра
        val current = (eventsState.value as? UiState.Success)?.data
            ?: parser.getMockEvents()
        _recommendations.value = getRecommendations(current)
    }
}
