package com.example.hotel_pere_maria_app.data.model

// Respuesta que recibimos del servidor tras un registro exitoso
data class RegisterResponse(
        val message: String, // Mensaje de éxito o error
        val user: User // Datos del usuario creado
)

