<<<<<<< HEAD
package com.hotel.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hotel.app.databinding.FragmentServicesBinding
import com.hotel.app.models.HotelService

class ServicesFragment : Fragment() {
    
    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupServices()
    }
    
    private fun setupServices() {
        // Пример списка услуг отеля
        val services = listOf(
            HotelService(
                id = "1",
                name = "SPA процедуры",
                price = 3500.0,
                duration = "1 час",
                iconRes = 0
            ),
            HotelService(
                id = "2",
                name = "Массаж",
                price = 2500.0,
                duration = "45 мин",
                iconRes = 0
            ),
            HotelService(
                id = "3",
                name = "Бассейн",
                price = 1500.0,
                duration = "2 часа",
                iconRes = 0
            ),
            HotelService(
                id = "4",
                name = "Ресторан",
                price = 5000.0,
                duration = "Ужин",
                iconRes = 0
            ),
            HotelService(
                id = "5",
                name = "Фитнес-центр",
                price = 800.0,
                duration = "1 час",
                iconRes = 0
            )
        )
        
        // Здесь можно настроить адаптер для списка услуг
        // Для простоты показываем кнопки
        binding.buttonSpa.setOnClickListener {
            navigateToPayment(services[0])
        }
        
        binding.buttonMassage.setOnClickListener {
            navigateToPayment(services[1])
        }
        
        binding.buttonPool.setOnClickListener {
            navigateToPayment(services[2])
        }
        
        binding.buttonRestaurant.setOnClickListener {
            navigateToPayment(services[3])
        }
        
        binding.buttonFitness.setOnClickListener {
            navigateToPayment(services[4])
        }
    }
    
    private fun navigateToPayment(service: HotelService) {
        val bundle = Bundle().apply {
            putParcelable("service", service)
        }
        findNavController().navigate(
            R.id.action_servicesFragment_to_paymentFragment,
            bundle
        )
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

class ServicesFragment : Fragment(R.layout.fragment_services)
>>>>>>> 571799fc9205593a2257cbab7c2d50265b15af69
