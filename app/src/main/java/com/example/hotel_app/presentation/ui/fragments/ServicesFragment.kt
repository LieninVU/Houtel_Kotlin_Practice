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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentServicesBinding
import com.example.hotel_app.domain.model.HotelService
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

        setupToolbar() // ✅ Добавлена настройка toolbar
        setupRecyclerView()
        setupCategoryFilter()
        setupSearch()
        observeState()

        // Загружаем услуги
        viewModel.loadServices()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // ✅ Навигация назад
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
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

    /**
     * Маппинг кнопок категорий на соответствующие ServiceCategory.
     * Используется для упрощения обработки кликов.
     */
    private val categoryButtons: Map<View, ServiceCategory?> by lazy {
        mapOf(
            binding.chipAll to null,
            binding.chipSpa to ServiceCategory.SPA,
            binding.chipTransfer to ServiceCategory.TRANSFER,
            binding.chipFood to ServiceCategory.FOOD,
            binding.chipOther to ServiceCategory.OTHER
        )
    }

    private fun setupCategoryFilter() {
        categoryButtons.forEach { (button, category) ->
            button.setOnClickListener {
                disableCategoryButtons()
                button.isEnabled = false
                viewModel.selectCategory(category)
            }
        }
    }

    private fun disableCategoryButtons() {
        categoryButtons.keys.forEach { it.isEnabled = true }
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
