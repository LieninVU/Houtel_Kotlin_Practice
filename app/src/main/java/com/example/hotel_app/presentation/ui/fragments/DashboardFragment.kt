package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentDashboardBinding
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val viewModel: MainViewModel by viewModel()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnQuickKey.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_keyFragment)
        }

        binding.btnQuickServices.setOnClickListener {
            findNavController().navigate(R.id.servicesFragment)
        }

        // Other listeners...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
