package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.databinding.LayoutItemReviewBinding
import com.example.hotel_app.domain.model.Review

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(DiffCallback) {

    class ReviewViewHolder(val binding: LayoutItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = LayoutItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = getItem(position)
        with(holder.binding) {
            tvUserName.text = review.userName
            tvRating.text = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
            tvComment.text = review.comment
            tvDate.text = review.date
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}
