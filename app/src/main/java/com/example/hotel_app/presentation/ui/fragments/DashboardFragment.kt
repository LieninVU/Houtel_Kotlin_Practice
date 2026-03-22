package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentDashboardBinding
import com.example.hotel_app.presentation.ui.adapter.EventAdapter
import com.example.hotel_app.presentation.ui.adapter.PaidServicesAdapter
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModel
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModelFactory
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: MainViewModel by viewModel()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val hotelInfoViewModel: HotelInfoViewModel by viewModels {
        HotelInfoViewModelFactory(requireContext())
    }

    private lateinit var recommendationsAdapter: EventAdapter
    private lateinit var paidServicesAdapter: PaidServicesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Не прячем ошибки биндинга: иначе экран может "не обновляться" до нажатия Back/Esc
        _binding = FragmentDashboardBinding.bind(view)
        setupListeners()
        setupRecommendations()
        setupPaidServices()
        observeRecommendations()
        observePaidServices()
        observeState()
    }

    private fun setupListeners() {
        binding.btnQuickBooking.setOnClickListener {
            findNavController().navigate(R.id.bookingFragment)
        }

        // Services
        binding.btnQuickServices.setOnClickListener {
            findNavController().navigate(R.id.servicesFragment)
        }

        binding.btnQuickKey.setOnClickListener {
            findNavController().navigate(R.id.keyFragment)
        }

        binding.cardStatus.setOnClickListener {
            val hasBooking = viewModel.activeBooking.value != null
            findNavController().navigate(if (hasBooking) R.id.keyFragment else R.id.bookingFragment)
        }

        binding.btnGoToInfo.setOnClickListener {
            findNavController().navigate(R.id.hotelInfoFragment)
        }
    }

    private fun setupRecommendations() {
        recommendationsAdapter = EventAdapter { event ->
            hotelInfoViewModel.onEventViewed(event)
            findNavController().navigate(R.id.hotelInfoFragment)
        }
        binding.rvDashboardRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationsAdapter
        }
    }

    private fun setupPaidServices() {
        paidServicesAdapter = PaidServicesAdapter()
        binding.rvPaidServices.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = paidServicesAdapter
        }
    }

    private fun observePaidServices() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.paidServices.collect { services ->
                    paidServicesAdapter.submitList(services)
                    binding.rvPaidServices.isVisible = services.isNotEmpty()
                    binding.tvPaidServicesLabel.isVisible = services.isNotEmpty()
                    binding.tvNoPaidServices.isVisible = services.isEmpty()
                }
            }
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

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        binding.tvUserName.text = user?.name ?: getString(R.string.dashboard_quick_action_key)
                    }
                }

                launch {
                    viewModel.activeBooking.collect { booking ->
                        booking?.let {
                            binding.tvBookingRoom.text = "${it.roomType} #${it.roomNumber}"
                            binding.tvBookingDates.text = "${it.checkIn} - ${it.checkOut}"
                            val statusText = it.status.name.replace("_", " ")
                            binding.tvBookingStatus.text = statusText
                        } ?: run {
                            binding.tvBookingRoom.text = getString(R.string.dashboard_no_active_booking)
                            binding.tvBookingDates.text = getString(R.string.dashboard_tap_to_reserve)
                            binding.tvBookingStatus.text = "—"
                        }
                    }
                }

                launch {
                    viewModel.bookings.collect { list ->
                        binding.tvBookingCount.text = getString(R.string.dashboard_bookings_count_format, list.size)
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
