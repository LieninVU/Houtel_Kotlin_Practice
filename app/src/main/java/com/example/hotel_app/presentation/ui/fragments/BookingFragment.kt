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
import com.example.hotel_app.presentation.ui.adapter.RoomAdapter
import com.example.hotel_app.presentation.viewmodel.BookingAction
import com.example.hotel_app.presentation.viewmodel.BookingEvent
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
        // ✅ MVI: отправляем действие в ViewModel
        viewModel.onAction(BookingAction.SelectRoom(room))
        updateSelectedRoomInAdapter(room.id)
        scrollToBookingForm()
    }

    /**
     * Обновляет выделенный элемент в адаптере.
     * Выносится в отдельный метод для избежания рекурсивной проблемы компилятора.
     */
    private fun updateSelectedRoomInAdapter(roomId: String) {
        roomAdapter.setSelectedRoom(roomId)
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
                // ✅ MVI: отправляем действие в ViewModel
                viewModel.onAction(BookingAction.SetCheckInDate(date))
            }
        }

        binding.etCheckOut.setOnClickListener {
            showDatePicker { date ->
                binding.etCheckOut.setText(date)
                // ✅ MVI: отправляем действие в ViewModel
                viewModel.onAction(BookingAction.SetCheckOutDate(date))
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
            // ✅ MVI: отправляем действие в ViewModel
            viewModel.onAction(BookingAction.CreateBooking)
        }
    }

    private fun setupGuestNameInput() {
        binding.etGuestName.doAfterTextChanged { text ->
            // ✅ MVI: отправляем действие в ViewModel
            viewModel.onAction(BookingAction.SetGuestName(text?.toString() ?: ""))
        }
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

    /**
     * Наблюдение за единым состоянием UI.
     * ✅ ХОРОШО: один collect для всего состояния вместо 6 отдельных
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ✅ Холодный Flow для комнат — данные от репозитория
                launch {
                    viewModel.roomsFlow.collect { rooms ->
                        roomAdapter.submitList(rooms)
                    }
                }

                // ✅ Единый StateFlow для всего UI-состояния
                launch {
                    viewModel.state.collect { state ->
                        // Обновляем прогресс загрузки комнат
                        binding.progressRooms.isVisible = state.isRoomsLoading

                        // Обновляем прогресс бронирования и кнопку
                        binding.progressBooking.isVisible = state.isBookingLoading
                        binding.btnBook.isEnabled = state.isFormValid && !state.isBookingLoading

                        // Обновляем карточку summary
                        updateSummary(state)
                    }
                }

                // ✅ SharedFlow для одноразовых событий (навигация, ошибки)
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is BookingEvent.BookingSuccess -> {
                                showSuccessDialog(
                                    roomNumber = event.booking.roomNumber,
                                    message = event.message
                                )
                            }
                            is BookingEvent.NavigateToPayment -> {
                                val action = BookingFragmentDirections.actionBookingFragmentToPaymentFragment(
                                    amount = event.amount.toFloat(),
                                    bookingId = event.booking.id,
                                    roomNumber = event.booking.roomNumber
                                )
                                findNavController().navigate(action)
                            }
                            is BookingEvent.BookingError -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }
                            is BookingEvent.ValidationError -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Рендеринг состояния в UI.
     * Принимает immutable состояние — предсказуемый рендеринг.
     */
    private fun updateSummary(state: com.example.hotel_app.presentation.viewmodel.BookingUiState) {
        val room = state.selectedRoom
        val checkIn = state.checkInDate
        val checkOut = state.checkOutDate

        if (room != null) {
            binding.cardSummary.isVisible = true
            val roomNumber = room.id.removePrefix("room_")
            binding.tvSummaryRoom.text = getString(R.string.booking_summary_room_format, room.type, roomNumber)

            if (!checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()) {
                binding.tvSummaryDates.text = getString(R.string.booking_summary_dates_format, checkIn, checkOut)
                // ✅ Используем вычисляемое свойство из состояния
                binding.tvSummaryPrice.text = getString(R.string.booking_summary_total_format, state.totalPrice.toInt())
            } else {
                binding.tvSummaryDates.text = getString(R.string.booking_summary_dates_select)
                binding.tvSummaryPrice.text = getString(R.string.booking_summary_price_per_night, room.price.toInt())
            }
        } else {
            binding.cardSummary.isVisible = false
        }
    }

    private fun showSuccessDialog(roomNumber: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.booking_dialog_confirmed_title)
            .setMessage(getString(R.string.booking_dialog_confirmed_message, message))
            .setPositiveButton(R.string.booking_dialog_view_key) { _, _ ->
                findNavController().navigate(R.id.keyFragment)
            }
            .setNegativeButton(R.string.booking_dialog_back_to_dashboard) { _, _ ->
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
