package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentPaymentBinding
import com.example.hotel_app.presentation.viewmodel.PaymentResult
import com.example.hotel_app.presentation.viewmodel.PaymentViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private val viewModel: PaymentViewModel by viewModel()
    private val args: PaymentFragmentArgs by navArgs()
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaymentBinding.bind(view)

        // Устанавливаем сумму из аргументов
        viewModel.setAmount(args.amount)
        
        setupToolbar()
        setupListeners()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            title = "Оплата номера ${args.roomNumber}"
        }
    }

    private fun setupListeners() {
        binding.btnPay.setOnClickListener {
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
                    viewModel.amount.collect { amount ->
                        if (amount > 0) {
                            binding.tvAmount.text = "Сумма: $${amount.toInt()}"
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
                                    
                                    // Переход на ключи после успешной оплаты
                                    binding.btnContinue.isVisible = true
                                    binding.btnContinue.setOnClickListener {
                                        findNavController().navigate(R.id.keyFragment)
                                    }
                                }
                                is PaymentResult.Cancelled -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                    findNavController().navigateUp()
                                }
                                is PaymentResult.Error -> {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                            viewModel.clearResult()
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
