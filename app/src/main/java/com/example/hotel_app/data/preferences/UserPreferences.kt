package com.example.hotel_app.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Синглтон для хранения пользовательских предпочтений.
 * Используется для алгоритма рекомендаций (Этап 2).
 */
class UserPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastViewedCategory: String
        get() = prefs.getString(KEY_LAST_CATEGORY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_CATEGORY, value).apply()

    var viewedEventIds: Set<String>
        get() = prefs.getStringSet(KEY_VIEWED_EVENTS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_VIEWED_EVENTS, value).apply()

    fun markEventViewed(eventTitle: String) {
        viewedEventIds = viewedEventIds + eventTitle
    }

    companion object {
        private const val PREFS_NAME = "hotel_user_prefs"
        private const val KEY_LAST_CATEGORY = "last_category"
        private const val KEY_VIEWED_EVENTS = "viewed_events"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences =
            instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
    }
}
