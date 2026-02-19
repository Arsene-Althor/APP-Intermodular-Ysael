package com.example.hotel_pere_maria_app.ui.Models

// Datos necesarios para enviar una petición de registro a la API
data class RegisterRequest(
        val name: String,
        val surname: String,
        val email: String,
        val password: String,
        val confirmPassword: String, // Necesario para validación en backend
        val dni: String,
        val birthDate: String,
        val gender: String,
        val city: String? = null // Campo opcional
)
