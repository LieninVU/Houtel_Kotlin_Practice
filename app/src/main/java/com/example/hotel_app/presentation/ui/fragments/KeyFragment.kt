package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentKeyBinding
import com.example.hotel_app.presentation.ui.adapter.NfcKeyAdapter
import com.example.hotel_app.presentation.viewmodel.NfcViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class KeyFragment : Fragment(R.layout.fragment_key) {

    private val viewModel: NfcViewModel by viewModel()
    private var _binding: FragmentKeyBinding? = null
    private val binding get() = _binding!!
    
    private val keyAdapter = NfcKeyAdapter { keyId, action ->
        viewModel.performAction(keyId, action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentKeyBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            // В связке с bottom-nav самый стабильный вариант: popUpTo dashboard + singleTop
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.dashboardFragment, false)
                .setLaunchSingleTop(true)
                .build()
            findNavController().navigate(R.id.dashboardFragment, null, options)
        }

        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.rvKeys.apply {
            adapter = keyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.nfcKeys.collect { keys ->
                        keyAdapter.submitList(keys)
                    }
                }

                launch {
                    viewModel.nfcEvent.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
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
