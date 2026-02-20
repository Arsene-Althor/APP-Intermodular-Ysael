package com.example.hotel_pere_maria_app.ui.Service

// Almacena la información del usuario logueado en memoria
data class UserInfo(
        val user_id: String,
        val email: String,
        val name: String,
        val surname: String = "",
        val role: String,
        val dni: String = "",
        val birthDate: String? = null,
        val city: String? = null,
        val gender: String = "Other",
        val profileImage: String? = null,
        val isVIP: Boolean = false,
        val discount: Double = 0.0,
        val isActive: Boolean = true
)

// Objeto global (Singleton) para acceder a la sesión desde cualquier parte
object SessionManager {
    var userToken: String? = null // Token JWT para autenticación
    var userInfo: UserInfo? = null // Datos del usuario

    // Limpia la sesión al cerrar sesión
    fun clear() {
        userToken = null
        userInfo = null
    }
}
