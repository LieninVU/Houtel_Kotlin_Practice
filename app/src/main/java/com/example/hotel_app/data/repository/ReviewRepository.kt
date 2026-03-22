package com.hotel.app.data

import com.hotel.app.models.Review
import com.hotel.app.utils.MockReviews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val database: AppDatabase
) {
    val allReviews: Flow<List<Review>> = database.reviewDao().getAllReviews()
    
    suspend fun addReview(review: Review) {
        database.reviewDao().insertReview(review)
    }
    
    suspend fun loadMockReviews() {
        MockReviews.getMockReviews().forEach { review ->
            database.reviewDao().insertReview(review)
        }
    }
}