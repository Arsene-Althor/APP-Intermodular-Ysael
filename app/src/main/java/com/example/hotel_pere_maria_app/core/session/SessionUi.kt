package com.example.hotel_pere_maria_app.core.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Señal Compose para refrescar avatar tras actualizar [SessionManager.userInfo]. */
object SessionUi {
    var userInfoTick by mutableIntStateOf(0)
        private set

    fun bumpUserInfo() {
        userInfoTick++
    }
}

