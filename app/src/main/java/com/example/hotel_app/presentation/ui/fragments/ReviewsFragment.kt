<<<<<<< HEAD
package com.example.hotel_app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.adapters.ReviewsAdapter
import com.example.hotel_app.databinding.FragmentReviewsBinding
import com.example.hotel_app.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReviewsFragment : Fragment() {
    
    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReviewViewModel by viewModel()
    private lateinit var adapter: ReviewsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupButtons()
    }
    
    private fun setupRecyclerView() {
        adapter = ReviewsAdapter()
        binding.recyclerViewReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReviewsFragment.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
            if (reviews.isEmpty()) {
                binding.textNoReviews.visibility = View.VISIBLE
                binding.recyclerViewReviews.visibility = View.GONE
            } else {
                binding.textNoReviews.visibility = View.GONE
                binding.recyclerViewReviews.visibility = View.VISIBLE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isReviewAdded.collect { added ->
                if (added) {
                    Toast.makeText(requireContext(), "Отзыв добавлен!", Toast.LENGTH_SHORT).show()
                    clearReviewForm()
                }
            }
        }
    }
    
    private fun setupButtons() {
        binding.buttonSubmitReview.setOnClickListener {
            val userName = binding.editTextUserName.text.toString().trim()
            val rating = binding.ratingBar.rating
            val comment = binding.editTextComment.text.toString().trim()
            
            when {
                userName.isEmpty() -> {
                    binding.editTextUserName.error = "Введите ваше имя"
                }
                comment.isEmpty() -> {
                    binding.editTextComment.error = "Введите текст отзыва"
                }
                rating == 0f -> {
                    Toast.makeText(requireContext(), "Поставьте оценку", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel.addReview(userName, rating, comment)
                }
            }
        }
    }
    
    private fun clearReviewForm() {
        binding.editTextUserName.text?.clear()
        binding.editTextComment.text?.clear()
        binding.ratingBar.rating = 0f
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
=======
package com.example.hotel_app.presentation.ui.fragments

import androidx.fragment.app.Fragment
import com.example.hotel_app.R

class ReviewsFragment : Fragment(R.layout.fragment_reviews)
>>>>>>> 571799fc9205593a2257cbab7c2d50265b15af69
