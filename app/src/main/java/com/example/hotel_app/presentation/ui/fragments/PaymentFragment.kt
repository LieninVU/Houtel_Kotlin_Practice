<<<<<<< HEAD
package com.example.hotel_app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hotel_app.databinding.FragmentPaymentBinding
import com.example.hotel_app.models.HotelService
import com.example.hotel_app.viewmodel.PaymentViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentFragment : Fragment() {
    
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PaymentViewModel by viewModel()
    private var currentService: HotelService? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем Parcelable объект из аргументов
        currentService = arguments?.getParcelable("service")
        
        if (currentService == null) {
            Toast.makeText(requireContext(), "Ошибка: услуга не найдена", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        
        setupUI()
        setupObservers()
        
        // Автоматически запускаем оплату
        currentService?.let { viewModel.startPayment(it) }
    }
    
    private fun setupUI() {
        currentService?.let { service ->
            binding.textServiceName.text = service.name
            binding.textServicePrice.text = "%.2f ₽".format(service.price)
            binding.textServiceDuration.text = "Длительность: ${service.duration}"
        }
        
        binding.buttonCancelPayment.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    private fun setupObservers() {
        viewModel.paymentState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PaymentViewModel.PaymentState.Processing -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonCancelPayment.isEnabled = false
                    binding.textPaymentStatus.text = "Обработка платежа..."
                    binding.textPaymentStatus.visibility = View.VISIBLE
                }
                
                is PaymentViewModel.PaymentState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textPaymentStatus.text = "Платеж успешно выполнен!"
                    
                    Toast.makeText(
                        requireContext(),
                        "Оплата ${state.service.name} прошла успешно!",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Переход к списку забронированных услуг
                    findNavController().navigate(
                        R.id.action_paymentFragment_to_bookedServicesFragment,
                        Bundle().apply {
                            putParcelable("bookedService", state.service)
                        }
                    )
                }
                
                is PaymentViewModel.PaymentState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCancelPayment.isEnabled = true
                    binding.textPaymentStatus.text = state.message
                    binding.textPaymentStatus.visibility = View.VISIBLE
                    
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                is PaymentViewModel.PaymentState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
=======
package com.example.hotel_app.presentation.ui.fragments

import androidx.fragment.app.Fragment
import com.example.hotel_app.R

class PaymentFragment : Fragment(R.layout.fragment_payment)
>>>>>>> 571799fc9205593a2257cbab7c2d50265b15af69
