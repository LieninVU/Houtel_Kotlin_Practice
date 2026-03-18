package com.example.hotel_app.di

import com.example.hotel_app.data.repository.MockHotelRepository
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import com.example.hotel_app.presentation.viewmodel.NfcViewModel
import com.example.hotel_app.presentation.viewmodel.BookingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repository: Switch between Mock and Real here
    single<HotelRepository> { MockHotelRepository() }
    
    // ViewModels
    viewModel { MainViewModel(get()) }
    viewModel { NfcViewModel(get()) }
    viewModel { BookingViewModel(get()) }
}
