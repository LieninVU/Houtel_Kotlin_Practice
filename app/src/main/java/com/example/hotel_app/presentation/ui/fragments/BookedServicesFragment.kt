package com.hotel.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hotel.app.adapters.BookedServicesAdapter
import com.hotel.app.databinding.FragmentBookedServicesBinding
import com.hotel.app.viewmodel.BookedServicesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookedServicesFragment : Fragment() {
    
    private var _binding: FragmentBookedServicesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BookedServicesViewModel by viewModels()
    private lateinit var adapter: BookedServicesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookedServicesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        // Получаем забронированную услугу из аргументов
        val bookedService = arguments?.getParcelable<HotelService>("bookedService")
        
        if (bookedService != null) {
            viewModel.addBookedService(bookedService)
            Toast.makeText(
                requireContext(),
                "Услуга \"${bookedService.name}\" добавлена в список",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = BookedServicesAdapter { service ->
            // Обработка удаления услуги
            viewModel.removeBookedService(service.id)
            Toast.makeText(
                requireContext(),
                "Услуга \"${service.name}\" удалена",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.recyclerViewBookedServices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookedServicesFragment.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.bookedServices.observe(viewLifecycleOwner) { services ->
            adapter.submitList(services)
            
            if (services.isEmpty()) {
                binding.textNoServices.visibility = View.VISIBLE
                binding.recyclerViewBookedServices.visibility = View.GONE
            } else {
                binding.textNoServices.visibility = View.GONE
                binding.recyclerViewBookedServices.visibility = View.VISIBLE
                updateTotalPrice(services)
            }
        }
    }
    
    private fun updateTotalPrice(services: List<HotelService>) {
        val totalPrice = services.sumOf { it.price }
        binding.textTotalPrice.text = "Итого: %.2f ₽".format(totalPrice)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}