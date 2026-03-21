package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentReviewsBinding
import com.example.hotel_app.presentation.ui.adapter.ReviewsAdapter
import com.example.hotel_app.presentation.viewmodel.ReviewsViewModel
import com.example.hotel_app.presentation.viewmodel.SubmitResult
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReviewsFragment : Fragment(R.layout.fragment_reviews) {

    private val viewModel: ReviewsViewModel by viewModel()
    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewsAdapter: ReviewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewsBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        reviewsAdapter = ReviewsAdapter()
        binding.rvReviews.apply {
            adapter = reviewsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        binding.etUserName.addTextChangedListener { text ->
            viewModel.setUserName(text?.toString() ?: "")
        }

        binding.etComment.addTextChangedListener { text ->
            viewModel.setComment(text?.toString() ?: "")
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            viewModel.setRating(rating.toInt())
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.submitReview()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.reviews.collect { reviews ->
                        reviewsAdapter.submitList(reviews)
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.submitResult.collect { result ->
                        result?.let {
                            when (it) {
                                is SubmitResult.Success -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                    binding.etUserName.text?.clear()
                                    binding.etComment.text?.clear()
                                    binding.ratingBar.rating = 0f
                                }
                                is SubmitResult.Error -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                            viewModel.clearSubmitResult()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
