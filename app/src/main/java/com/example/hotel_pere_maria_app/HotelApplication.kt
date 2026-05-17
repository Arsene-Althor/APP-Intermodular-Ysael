package com.example.hotel_pere_maria_app

import android.app.Application
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityNotificationHelper
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityPollWorker
import com.example.hotel_pere_maria_app.core.session.SessionManager

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

