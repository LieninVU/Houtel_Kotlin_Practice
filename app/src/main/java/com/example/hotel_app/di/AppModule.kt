package com.example.hotel_app.di

import com.example.hotel_app.data.local.AppDatabase
import com.example.hotel_app.data.local.ReviewDao
import com.example.hotel_app.data.repository.MockHotelRepository
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.presentation.ui.NfcNotificationManager
import com.example.hotel_app.presentation.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Room Database
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().reviewDao() }

    // DAO
    single<ReviewDao> { get<AppDatabase>().reviewDao() }

    // Repository: Switch between Mock and Real here
    single<HotelRepository> { MockHotelRepository(get()) }

    // Notification Manager
    single { NfcNotificationManager(androidContext()) }

    // ViewModels
    viewModel { MainViewModel(get()) }
    viewModel { NfcViewModel(get(), get()) }
    viewModel { BookingViewModel(get()) }
    viewModel { ServicesViewModel(get()) }
    viewModel { HotelInfoViewModel(get()) }
    viewModel { PaymentViewModel() }
    viewModel { ReviewsViewModel(get(), get()) }
    viewModel { RestaurantViewModel() }
    viewModel { MapsViewModel(get()) }
}
