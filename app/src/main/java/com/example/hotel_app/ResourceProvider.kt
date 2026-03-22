package com.example.hotel_app

import android.content.Context
import androidx.annotation.StringRes
import com.example.hotel_app.HotelApplication

/**
 * Утилита для получения строк из ресурсов в ViewModel и других non-UI классах.
 * Использует Application Context для доступа к ресурсам.
 */
object ResourceProvider {

    private val context: Context
        get() = HotelApplication.instance

    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
