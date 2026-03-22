package com.example.hotel_app.di

import com.example.hotel_app.data.repository.MockHotelRepository
import com.example.hotel_app.domain.repository.HotelRepository
import com.example.hotel_app.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repository: Switch between Mock and Real here
<<<<<<< HEAD
    // single<HotelRepository> { MockHotelRepository() }
=======
    single<HotelRepository> { MockHotelRepository() }
>>>>>>> 571799fc9205593a2257cbab7c2d50265b15af69
    
    // RemoteDataSource (Stub for future)
    // single { RemoteDataSource(get()) }

<<<<<<< HEAD
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

=======
>>>>>>> 571799fc9205593a2257cbab7c2d50265b15af69
    // ViewModels
    viewModel { MainViewModel(get()) }
}
