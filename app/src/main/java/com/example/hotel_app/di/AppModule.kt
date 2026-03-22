package com.example.hotel_app.di

import com.example.hotel_app.data.repository.MockHotelRepository
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repository: Switch between Mock and Real here
    // single<HotelRepository> { MockHotelRepository() }
    
    // RemoteDataSource (Stub for future)
    // single { RemoteDataSource(get()) }

    // Мне уже всё-равно
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "hotel_database"
        ).build()
    }
    
    // Review Dao
    single { get<AppDatabase>().reviewDao() }
    
    // Review Repository
    single { ReviewRepository(get()) }

    // ViewModels
    viewModel { MainViewModel(get()) }
}
