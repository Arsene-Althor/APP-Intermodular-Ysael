package com.example.hotel_pere_maria_app.ui.Models

// Modelo de datos que representa a un usuario en la aplicación
// Coincide con los campos que devuelve la API
data class User(
        val user_id: String? = null, // Identificador único del usuario
        val name: String = "",
        val surname: String = "",
        val email: String = "",
        val password: String? =
                null, // Puede ser nulo porque la API no siempre devuelve la contraseña por
        // seguridad
        val dni: String = "",
        val birthDate: String? = null,
        val city: String? = null, // Opcional
        val gender: String = "Other",
        val profileImage: String? = null,
        val role: String = "client", // Por defecto es cliente
        val isVIP: Boolean = false,
        val discount: Double = 0.0,
        val isActive: Boolean = true,
        val createdAt: String? = null,
        val updatedAt: String? = null
)
