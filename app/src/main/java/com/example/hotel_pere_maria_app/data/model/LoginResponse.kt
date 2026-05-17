package com.example.hotel_pere_maria_app.data.model

import com.example.hotel_pere_maria_app.core.session.UserInfo

data class LoginResponse(
    val token: String,
    val user: UserInfo,
    val mensaje: String? = null
)

