package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import com.example.hotel_app.data.local.ReviewDao
import com.example.hotel_app.data.repository.MockHotelRepository
import com.example.hotel_app.data.repository.toEntity
import com.example.hotel_app.data.repository.toReview
import com.example.hotel_app.domain.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension-функции для валидации данных отзыва.
 * Упрощают основную логику и делают код более читаемым.
 */
private fun NewReviewData.validate(): ReviewValidationError? {
    return when {
        userName.isBlank() -> ReviewValidationError.EmptyName
        rating !in 1..5 -> ReviewValidationError.InvalidRating
        comment.isBlank() -> ReviewValidationError.EmptyComment
        else -> null
    }
}

/**
 * Типы ошибок валидации отзыва.
 * Содержат resource ID для локализации.
 */
private sealed class ReviewValidationError(val messageResId: Int) {
    object EmptyName : ReviewValidationError(R.string.reviews_error_empty_name)
    object InvalidRating : ReviewValidationError(R.string.reviews_error_empty_rating)
    object EmptyComment : ReviewValidationError(R.string.reviews_error_empty_text)
}

class ReviewsViewModel(
    private val reviewDao: ReviewDao,
    private val mockRepository: MockHotelRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _newReview = MutableStateFlow(NewReviewData())
    val newReview: StateFlow<NewReviewData> = _newReview.asStateFlow()

    private val _submitResult = MutableStateFlow<SubmitResult?>(null)
    val submitResult: StateFlow<SubmitResult?> = _submitResult.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            reviewDao.getAllReviews().collect { entities ->
                _reviews.value = entities.map { it.toReview() }
                _isLoading.value = false
            }
        }
    }

    fun setUserName(name: String) {
        _newReview.value = _newReview.value.copy(userName = name)
    }

    fun setRating(rating: Int) {
        _newReview.value = _newReview.value.copy(rating = rating)
    }

    fun setComment(comment: String) {
        _newReview.value = _newReview.value.copy(comment = comment)
    }

    fun submitReview() {
        val reviewData = _newReview.value

        // ✅ Валидация через extension-функцию
        reviewData.validate()?.let { error ->
            _submitResult.value = SubmitResult.Error(ResourceProvider.getString(error.messageResId))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val review = Review(
                id = UUID.randomUUID().toString(),
                userName = reviewData.userName,
                rating = reviewData.rating,
                comment = reviewData.comment,
                date = SimpleDateFormat(ResourceProvider.getString(R.string.reviews_date_format), Locale.getDefault()).format(Date())
            )

            mockRepository.saveReview(review)
            _isLoading.value = false
            _submitResult.value = SubmitResult.Success(ResourceProvider.getString(R.string.reviews_success_message))
            _newReview.value = NewReviewData()
        }
    }

    fun clearSubmitResult() {
        _submitResult.value = null
    }
}

data class NewReviewData(
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = ""
)

sealed class SubmitResult {
    data class Success(val message: String) : SubmitResult()
    data class Error(val message: String) : SubmitResult()
}
