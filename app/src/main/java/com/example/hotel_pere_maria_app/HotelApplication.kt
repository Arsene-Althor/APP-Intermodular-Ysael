package com.example.hotel_pere_maria_app

import android.app.Application
import com.example.hotel_pere_maria_app.ui.Service.FlexibilityNotificationHelper
import com.example.hotel_pere_maria_app.ui.Service.FlexibilityPollWorker
import com.example.hotel_pere_maria_app.ui.Service.SessionManager

class HotelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        SessionManager.init(applicationContext)
        SessionManager.restoreSession()
        FlexibilityNotificationHelper.ensureChannel(applicationContext)
        if (SessionManager.userToken != null) {
            FlexibilityPollWorker.schedule(applicationContext)
        }
    }

    companion object {
        var appContext: android.content.Context? = null
            private set
    }
}
