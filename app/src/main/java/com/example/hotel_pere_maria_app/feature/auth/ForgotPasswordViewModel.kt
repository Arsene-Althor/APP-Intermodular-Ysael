package com.example.hotel_pere_maria_app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estados posibles de la pantalla de recuperar contraseña
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState() // Sin actividad
    object Loading : ForgotPasswordState() // Enviando solicitud
    data class Success(val message: String) : ForgotPasswordState() // Correo enviado
    data class Error(val message: String) : ForgotPasswordState() // Error
}

// Datos de la UI
data class ForgotPasswordUiState(
        val email: String = "",
        val status: ForgotPasswordState = ForgotPasswordState.Idle
)

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    // Llama a la API para enviar la contraseña temporal al correo
    fun recoverPassword() {
        val currentEmail = _uiState.value.email

        if (currentEmail.isBlank()) {
            _uiState.update { it.copy(status = ForgotPasswordState.Error("Introduce tu email")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(status = ForgotPasswordState.Loading) }

            try {
                val response =
                        RetrofitClient.authService.recoverPassword(mapOf("email" to currentEmail))

                if (response.isSuccessful) {
                    val message = response.body()?.get("message") ?: "Revisa tu correo"
                    _uiState.update { it.copy(status = ForgotPasswordState.Success(message)) }
                } else {
                    _uiState.update {
                        it.copy(status = ForgotPasswordState.Error("Error al enviar solicitud"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(status = ForgotPasswordState.Error("Error de conexión: ${e.message}"))
                }
            }
        }
    }
}

