package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentServicesBinding
import com.example.hotel_app.domain.model.ServiceCategory
import com.example.hotel_app.presentation.ui.adapter.ServicesAdapter
import com.example.hotel_app.presentation.viewmodel.PaymentUiState
import com.example.hotel_app.presentation.viewmodel.ServicesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private val viewModel: ServicesViewModel by viewModel()
    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var servicesAdapter: ServicesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServicesBinding.bind(view)

        setupRecyclerView()
        setupCategoryFilter()
        setupSearch()
        observeState()
        
        // Загружаем услуги
        viewModel.loadServices()
    }

    private fun setupRecyclerView() {
        servicesAdapter = ServicesAdapter { service ->
            showPaymentDialog(service)
        }
        binding.rvServices.apply {
            adapter = servicesAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun showPaymentDialog(service: HotelService) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Оплата услуги")
            .setMessage("${service.title}\n\nЦена: $${service.price.toInt()}\n\nОплатить эту услугу?")
            .setPositiveButton("Оплатить") { _, _ ->
                viewModel.payForService(service)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setupCategoryFilter() {
        binding.chipAll.setOnClickListener {
            clearCategoryButtons()
            binding.chipAll.isEnabled = false
            viewModel.selectCategory(null)
        }
        binding.chipSpa.setOnClickListener {
            clearCategoryButtons()
            binding.chipSpa.isEnabled = false
            viewModel.selectCategory(ServiceCategory.SPA)
        }
        binding.chipTransfer.setOnClickListener {
            clearCategoryButtons()
            binding.chipTransfer.isEnabled = false
            viewModel.selectCategory(ServiceCategory.TRANSFER)
        }
        binding.chipFood.setOnClickListener {
            clearCategoryButtons()
            binding.chipFood.isEnabled = false
            viewModel.selectCategory(ServiceCategory.FOOD)
        }
        binding.chipOther.setOnClickListener {
            clearCategoryButtons()
            binding.chipOther.isEnabled = false
            viewModel.selectCategory(ServiceCategory.OTHER)
        }
    }

    private fun clearCategoryButtons() {
        binding.chipAll.isEnabled = true
        binding.chipSpa.isEnabled = true
        binding.chipTransfer.isEnabled = true
        binding.chipFood.isEnabled = true
        binding.chipOther.isEnabled = true
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredServices.collect { services ->
                        servicesAdapter.submitList(services)
                        binding.rvServices.isVisible = services.isNotEmpty()
                        binding.tvServicesPlaceholder.isVisible = services.isEmpty()
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.paymentResult.collect { state ->
                        when (state) {
                            is PaymentUiState.Success -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            is PaymentUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
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
