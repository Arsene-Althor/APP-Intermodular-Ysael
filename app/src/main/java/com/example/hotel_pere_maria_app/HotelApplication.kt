package com.example.hotel_pere_maria_app

import android.app.Application
import com.example.hotel_pere_maria_app.ui.Service.SessionManager

class HotelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(applicationContext)
        SessionManager.restoreSession()
    }
}
