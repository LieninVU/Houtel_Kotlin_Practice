package com.example.hotel_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.data.ReviewRepository
import com.example.hotel_app.models.Review
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class ReviewViewModel(
    private val repository: ReviewRepository
) : ViewModel() {
    
    val reviews = repository.allReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _isReviewAdded = MutableSharedFlow<Boolean>()
    val isReviewAdded: SharedFlow<Boolean> = _isReviewAdded.asSharedFlow()
    
    init {
        loadMockData()
    }
    
    private fun loadMockData() {
        viewModelScope.launch {
            repository.loadMockReviews()
        }
    }
    
    fun addReview(userName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val review = Review(
                userName = userName,
                rating = rating,
                comment = comment,
                date = Date()
            )
            repository.addReview(review)
            _isReviewAdded.emit(true)
        }
    }
}