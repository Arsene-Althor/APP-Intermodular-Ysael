package com.example.hotel_pere_maria_app.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Singleton para gestionar el tema (claro/oscuro) en toda la aplicación
object ThemeManager {
    // Variable privada mutable que guarda el estado
    private val _isDarkTheme = MutableStateFlow(false)

    // Variable pública de solo lectura que la UI puede observar
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Invierte el tema actual
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Establece un tema específico
    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }
}

