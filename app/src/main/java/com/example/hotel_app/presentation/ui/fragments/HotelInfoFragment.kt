package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.databinding.FragmentHotelInfoBinding
import com.example.hotel_app.presentation.ui.adapter.EventAdapter
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModel
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModelFactory

class HotelInfoFragment : Fragment() {

    private var _binding: FragmentHotelInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HotelInfoViewModel by viewModels {
        HotelInfoViewModelFactory(requireContext())
    }

    private lateinit var eventsAdapter: EventAdapter
    private lateinit var recommendationsAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotelInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeViewModel()
        setupTabs()
    }

    private fun setupAdapters() {
        eventsAdapter = EventAdapter { event ->
            viewModel.onEventViewed(event)
        }
        recommendationsAdapter = EventAdapter { event ->
            viewModel.onEventViewed(event)
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
        }

        // Горизонтальный список рекомендаций (Этап 2)
        binding.rvRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventsAdapter.submitList(events)
        }
        viewModel.recommendations.observe(viewLifecycleOwner) { recs ->
            recommendationsAdapter.submitList(recs)
        }
    }

    private fun setupTabs() {
        // Переключение между вкладками: Мероприятия / Карта / Контакты
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
        binding.ivHotelMap.visibility = View.GONE
        binding.layoutContacts.visibility = View.GONE
    }

    private fun showMapTab() {
        binding.rvEvents.visibility = View.GONE
        binding.ivHotelMap.visibility = View.VISIBLE
        binding.layoutContacts.visibility = View.GONE
    }

    private fun showContactsTab() {
        binding.rvEvents.visibility = View.GONE
        binding.ivHotelMap.visibility = View.GONE
        binding.layoutContacts.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
