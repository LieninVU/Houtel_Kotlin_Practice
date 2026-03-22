package com.example.hotel_app

import android.app.Application
import com.example.hotel_app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HotelApplication : Application() {

    companion object {
        lateinit var instance: HotelApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger()
            androidContext(this@HotelApplication)
            modules(appModule)
        }
    }
}
