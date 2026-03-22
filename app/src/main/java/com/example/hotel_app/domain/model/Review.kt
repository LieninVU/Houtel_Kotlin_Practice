package com.hotel.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: Date
)