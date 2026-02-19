package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.LoginRequest
import com.example.hotel_pere_maria_app.ui.Models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Envia el email para recuperar la contraseña (POST /auth/recover)
    @POST("auth/recover")
    suspend fun recoverPassword(@Body request: Map<String, String>): Response<Map<String, String>>
}
