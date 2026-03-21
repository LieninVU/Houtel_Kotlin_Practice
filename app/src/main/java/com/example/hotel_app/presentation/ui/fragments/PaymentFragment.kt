package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val args: PaymentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaymentBinding.bind(view)

        val service = args.selectedService
        binding.tvPaymentServiceTitle.text = service.title
        binding.tvPaymentServiceCategory.text = service.category.displayName()
        binding.tvPaymentServiceDescription.text = service.subtitle
        binding.tvPaymentServiceDuration.text = getString(R.string.services_duration_template, service.durationMinutes)
        binding.tvPaymentServicePrice.text = getString(R.string.services_price_template, service.price)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
