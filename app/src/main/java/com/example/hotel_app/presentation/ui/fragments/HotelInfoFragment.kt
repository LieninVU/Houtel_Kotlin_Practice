package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentHotelInfoBinding
import com.example.hotel_app.presentation.ui.adapter.EventAdapter
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModel
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModelFactory
import com.example.hotel_app.presentation.viewmodel.UiState
import com.google.android.material.snackbar.Snackbar

class HotelInfoFragment : Fragment(R.layout.fragment_hotel_info) {

    private var _binding: FragmentHotelInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HotelInfoViewModel by viewModels {
        HotelInfoViewModelFactory(requireContext())
    }

    private lateinit var eventsAdapter: EventAdapter
    private lateinit var recommendationsAdapter: EventAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHotelInfoBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupAdapters()
        observeViewModel()
        setupTabs()
        setupRetry()
    }

    private fun setupAdapters() {
        eventsAdapter = EventAdapter { event -> viewModel.onEventViewed(event) }
        recommendationsAdapter = EventAdapter { event -> viewModel.onEventViewed(event) }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
        }
        binding.rvRecommendations.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = recommendationsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.eventsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutErrorInfo.visibility = View.GONE
                    binding.rvEvents.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutErrorInfo.visibility = View.GONE
                    binding.rvEvents.visibility = View.VISIBLE
                    eventsAdapter.submitList(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutErrorInfo.visibility = View.VISIBLE
                    binding.rvEvents.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { recs ->
            recommendationsAdapter.submitList(recs)
        }
    }

    private fun setupRetry() {
        binding.btnRetryInfo.setOnClickListener {
            viewModel.loadEvents()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showEventsTab()
                    1 -> showMapTab()
                    2 -> showContactsTab()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun showEventsTab() {
        binding.rvEvents.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.ivHotelMap.visibility = View.GONE
        binding.layoutContacts.visibility = View.GONE
    }

    private fun showMapTab() {
        binding.rvEvents.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.ivHotelMap.visibility = View.VISIBLE
        binding.layoutContacts.visibility = View.GONE
    }

    private fun showContactsTab() {
        binding.rvEvents.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.ivHotelMap.visibility = View.GONE
        binding.layoutContacts.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
