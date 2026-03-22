package com.hotel.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HotelService(
    val id: String,
    val name: String,
    val price: Double,
    val duration: String,
    val iconRes: Int
) : Parcelable