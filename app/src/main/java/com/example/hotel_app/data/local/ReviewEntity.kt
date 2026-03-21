package com.example.hotel_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)
