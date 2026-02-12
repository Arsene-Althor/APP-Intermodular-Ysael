package com.example.hotel_pere_maria_app.ui.Models

import com.example.hotel_pere_maria_app.ui.Service.UserInfo

data class LoginResponse(
    val token: String,
    val user : UserInfo
)


