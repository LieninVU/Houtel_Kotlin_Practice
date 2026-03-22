package com.hotel.app.data

import androidx.room.*
import com.hotel.app.models.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY date DESC")
    fun getAllReviews(): Flow<List<Review>>
    
    @Insert
    suspend fun insertReview(review: Review)
    
    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}