package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentPaymentBinding
import com.example.hotel_app.presentation.viewmodel.PaymentViewModel
import com.example.hotel_app.presentation.viewmodel.PaymentResult
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private val viewModel: PaymentViewModel by viewModel()
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaymentBinding.bind(view)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnPay.setOnClickListener {
            viewModel.setAmount(1000.0)
            viewModel.startPayment()
        }

        binding.btnCancel.setOnClickListener {
            viewModel.cancelPayment()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                        binding.btnPay.isEnabled = !isLoading
                        binding.btnCancel.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.timeRemaining.collect { time ->
                        binding.tvTimer.text = "Оплата через: ${time}с"
                        if (time > 0) {
                            binding.timerProgress.progress = time * 20 // 5 seconds = 100%
                        }
                    }
                }

                launch {
                    viewModel.paymentResult.collect { result ->
                        result?.let {
                            when (it) {
                                is PaymentResult.Success -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                    binding.tvPaymentStatus.text = it.message
                                    binding.tvPaymentStatus.isVisible = true
                                }
                                is PaymentResult.Cancelled -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                                is PaymentResult.Error -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                            viewModel.clearResult()
                        }
                    }
                }

                launch {
                    viewModel.amount.collect { amount ->
                        if (amount > 0) {
                            binding.tvAmount.text = "Сумма: ${amount}₽"
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
