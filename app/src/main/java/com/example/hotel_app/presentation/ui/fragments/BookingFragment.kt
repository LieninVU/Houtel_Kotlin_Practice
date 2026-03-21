package com.example.hotel_app.presentation.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentBookingBinding
import com.example.hotel_app.presentation.ui.adapter.RoomAdapter
import com.example.hotel_app.presentation.viewmodel.BookingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class BookingFragment : Fragment(R.layout.fragment_booking) {

    private val viewModel: BookingViewModel by viewModel()
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private lateinit var roomAdapter: RoomAdapter
    private var checkInCalendar = Calendar.getInstance()
    private var checkOutCalendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingBinding.bind(view)

        setupRecyclerView()
        setupDatePicker()
        observeState()
        viewModel.loadRooms()
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter { room ->
            viewModel.selectRoom(room)
        }
        binding.rvRooms.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupDatePicker() {
        binding.btnCheckinDate.setOnClickListener {
            showDatePicker { date ->
                viewModel.setCheckInDate(date)
            }
        }

        binding.btnCheckoutDate.setOnClickListener {
            showDatePicker { date ->
                viewModel.setCheckOutDate(date)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(calendar.timeInMillis)
            },
            checkInCalendar.get(Calendar.YEAR),
            checkInCalendar.get(Calendar.MONTH),
            checkInCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.rooms.collect { rooms ->
                        roomAdapter.submitList(rooms)
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.selectedRoom.collect { room ->
                        binding.tvSelectedRoom.text = room?.let {
                            "Выбрано: ${it.type} - ${it.price}₽/ночь"
                        } ?: "Выберите номер"
                    }
                }

                launch {
                    viewModel.checkInDate.collect { date ->
                        binding.btnCheckinDate.text = date.ifEmpty { "Дата заезда" }
                    }
                }

                launch {
                    viewModel.checkOutDate.collect { date ->
                        binding.btnCheckoutDate.text = date.ifEmpty { "Дата выезда" }
                    }
                }

                launch {
                    viewModel.bookingResult.collect { result ->
                        result?.let {
                            val message = it.fold(
                                onSuccess = { msg -> msg },
                                onFailure = { error -> error.message ?: "Ошибка" }
                            )
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            viewModel.clearBookingResult()
                        }
                    }
                }
            }
        }

        binding.btnBookRoom.setOnClickListener {
            viewModel.bookRoom()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
