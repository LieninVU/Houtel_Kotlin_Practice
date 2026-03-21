package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentHotelInfoBinding
import com.example.hotel_app.presentation.viewmodel.HotelInfoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HotelInfoFragment : Fragment(R.layout.fragment_hotel_info) {

    private val viewModel: HotelInfoViewModel by viewModel()
    private var _binding: FragmentHotelInfoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHotelInfoBinding.bind(view)

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.hotelInfo.collect { info ->
                        binding.tvHotelName.text = info.name
                        binding.tvAddress.text = info.address
                        binding.tvPhone.text = info.phone
                        binding.tvEmail.text = info.email
                        binding.tvCheckIn.text = "Заезд: ${info.checkInTime}"
                        binding.tvCheckOut.text = "Выезд: ${info.checkOutTime}"
                        binding.tvDescription.text = info.description
                        binding.tvFacilities.text = info.facilities.joinToString("\n") { "• $it" }
                        
                        binding.tvBreakfast.text = "Завтрак: ${info.schedule.breakfast}"
                        binding.tvLunch.text = "Обед: ${info.schedule.lunch}"
                        binding.tvDinner.text = "Ужин: ${info.schedule.dinner}"
                        binding.tvSpa.text = "SPA: ${info.schedule.spa}"
                        binding.tvGym.text = "Спортзал: ${info.schedule.gym}"
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
