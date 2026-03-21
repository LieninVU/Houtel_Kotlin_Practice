package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.hotel_app.databinding.FragmentServicesBinding
import com.example.hotel_app.presentation.ui.adapter.ServiceCategoryPagerAdapter
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.hotel_app.R

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModel()
    private lateinit var pagerAdapter: ServiceCategoryPagerAdapter
    private var tabMediator: TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServicesBinding.bind(view)

        setupPager()
        observeServicesCatalog()
    }

    private fun setupPager() {
        pagerAdapter = ServiceCategoryPagerAdapter()
        binding.vpServices.adapter = pagerAdapter
    }

    private fun observeServicesCatalog() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceCategories.collect { categories ->
                tabMediator?.detach()
                tabMediator = TabLayoutMediator(binding.tabLayoutServices, binding.vpServices) { tab, position ->
                    tab.text = categories[position].displayName()
                }.apply { attach() }

                pagerAdapter.submitPages(categories, viewModel.services.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.services.collect { services ->
                pagerAdapter.submitPages(viewModel.serviceCategories.value, services)
                binding.tvServicesEmpty.visibility = if (services.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isServicesLoading.collect { isLoading ->
                binding.progressServices.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.vpServices.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        binding.vpServices.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
