package com.example.hotel_app.presentation.ui.fragments

import android.app.DatePickerDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentBookingBinding
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.presentation.ui.adapter.RoomAdapter
import com.example.hotel_app.presentation.viewmodel.BookingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingFragment : Fragment(R.layout.fragment_booking) {

    private val viewModel: BookingViewModel by viewModel()
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val roomAdapter = RoomAdapter { room ->
        onRoomSelected(room)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        setupDatePickers()
        setupBookButton()
        setupGuestNameInput()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.rvRooms.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
    }

    private fun setupDatePickers() {
        binding.etCheckIn.setOnClickListener {
            showDatePicker { date ->
                binding.etCheckIn.setText(date)
                viewModel.setCheckInDate(date)
                updateSummary()
                updateBookButtonState()
            }
        }

        binding.etCheckOut.setOnClickListener {
            showDatePicker { date ->
                binding.etCheckOut.setText(date)
                viewModel.setCheckOutDate(date)
                updateSummary()
                updateBookButtonState()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormat.format(calendar.time)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun setupBookButton() {
        binding.btnBook.setOnClickListener {
            val guestName = binding.etGuestName.text?.toString() ?: ""
            viewModel.createBooking(guestName)
        }
    }

    private fun setupGuestNameInput() {
        binding.etGuestName.doAfterTextChanged {
            updateBookButtonState()
        }
    }

    private fun updateBookButtonState() {
        val guestName = binding.etGuestName.text?.toString() ?: ""
        binding.btnBook.isEnabled = viewModel.isFormValid(guestName)
    }

    private fun onRoomSelected(room: Room) {
        viewModel.selectRoom(room)
        roomAdapter.setSelectedRoom(room.id)
        updateSummary()
        updateBookButtonState()
        scrollToBookingForm()
    }

    private fun scrollToBookingForm() {
        // "Перекидываем" пользователя к заполнению данных после выбора номера
        binding.scrollBooking.post {
            val rect = Rect()
            binding.etCheckIn.getDrawingRect(rect)
            binding.scrollBooking.offsetDescendantRectToMyCoords(binding.etCheckIn, rect)
            val padding = (16 * resources.displayMetrics.density).toInt()
            binding.scrollBooking.smoothScrollTo(0, (rect.top - padding).coerceAtLeast(0))
            binding.etGuestName.requestFocus()
        }
    }

    private fun updateSummary() {
        val room = viewModel.selectedRoom.value
        val checkIn = viewModel.checkInDate.value
        val checkOut = viewModel.checkOutDate.value

        if (room != null) {
            binding.cardSummary.isVisible = true
            val roomNumber = room.id.removePrefix("room_")
            binding.tvSummaryRoom.text = "Room: ${room.type} #$roomNumber"
            
            if (!checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()) {
                binding.tvSummaryDates.text = "Dates: $checkIn - $checkOut"
                val total = viewModel.calculateTotalPrice()
                binding.tvSummaryPrice.text = "Total: $${total.toInt()}"
            } else {
                binding.tvSummaryDates.text = "Dates: Select dates"
                binding.tvSummaryPrice.text = "Price: $${room.price.toInt()} / night"
            }
        } else {
            binding.cardSummary.isVisible = false
        }
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
                    viewModel.isRoomsLoading.collect { isLoading ->
                        binding.progressRooms.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBooking.isVisible = isLoading
                        binding.btnBook.isEnabled = !isLoading && viewModel.isFormValid(
                            binding.etGuestName.text?.toString() ?: ""
                        )
                    }
                }

                launch {
                    viewModel.bookingEvent.collect { event ->
                        when (event) {
                            is BookingViewModel.BookingUiEvent.BookingSuccess -> {
                                showSuccessDialog(
                                    roomNumber = event.booking.roomNumber,
                                    message = event.message
                                )
                            }
                            is BookingViewModel.BookingUiEvent.NavigateToPayment -> {
                                // Переход на оплату с передачей суммы
                                val action = BookingFragmentDirections.actionBookingFragmentToPaymentFragment(
                                    amount = event.amount.toDouble(),
                                    bookingId = event.booking.id,
                                    roomNumber = event.booking.roomNumber
                                )
                                findNavController().navigate(action)
                            }
                            is BookingViewModel.BookingUiEvent.BookingError -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }
                            is BookingViewModel.BookingUiEvent.ValidationError -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(roomNumber: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Booking Confirmed!")
            .setMessage("$message\n\nYour digital NFC key has been automatically created and is ready to use.")
            .setPositiveButton("View My Key") { _, _ ->
                findNavController().navigate(R.id.keyFragment)
            }
            .setNegativeButton("Back to Dashboard") { _, _ ->
                // корректно возвращаемся на dashboard (вместе с bottom-nav)
                if (!findNavController().popBackStack(R.id.dashboardFragment, false)) {
                    findNavController().navigate(R.id.dashboardFragment)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
