package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentDashboardBinding
import com.example.hotel_app.presentation.ui.adapter.EventAdapter
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModel
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModelFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // ViewModel от Android-3 — рекомендации
    private val hotelInfoViewModel: HotelInfoViewModel by viewModels {
        HotelInfoViewModelFactory(requireContext())
    }

    private lateinit var recommendationsAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecommendations()
        observeRecommendations()
        setupNavigation()
    }

    private fun setupRecommendations() {
        recommendationsAdapter = EventAdapter { event ->
            hotelInfoViewModel.onEventViewed(event)
            findNavController().navigate(R.id.hotelInfoFragment)
        }
        binding.rvDashboardRecommendations.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = recommendationsAdapter
        }
    }

    private fun observeRecommendations() {
        hotelInfoViewModel.recommendations.observe(viewLifecycleOwner) { recs ->
            if (recs.isNullOrEmpty()) {
                showError()
            } else {
                showRecommendations()
                recommendationsAdapter.submitList(recs)
            }
        }

        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            hotelInfoViewModel.loadEvents()
        }
    }

    private fun showRecommendations() {
        binding.layoutRecommendations.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError() {
        binding.layoutRecommendations.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
    }

    private fun setupNavigation() {
        binding.btnGoToInfo.setOnClickListener {
            findNavController().navigate(R.id.hotelInfoFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
