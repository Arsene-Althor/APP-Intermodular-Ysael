package com.example.hotel_pere_maria_app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.data.model.RegisterRequest
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del registro
// Estado del registro
sealed class RegisterState {
    object Idle : RegisterState() // Estado inicial, sin actividad
    object Loading : RegisterState() // Cargando / enviando datos
    data class Success(val message: String) : RegisterState() // Registro exitoso
    data class Error(val message: String) : RegisterState() // Error en el registro
}

data class RegisterUiState(
        val name: String = "",
        val surname: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val dni: String = "",
        val birthDate: String = "",
        val city: String = "",
        val gender: String = "Other",
        val registerStatus: RegisterState = RegisterState.Idle,

        // Mensajes de error específicos para cada campo (si los hay)
        val nameError: String? = null,
        val surnameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val dniError: String? = null,
        val birthDateError: String? = null,
        val genderError: String? = null
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Funciones para actualizar campos
    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }
    fun onSurnameChange(value: String) {
        _uiState.update { it.copy(surname = value, surnameError = null) }
    }
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }
    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }
    fun onDniChange(value: String) {
        _uiState.update { it.copy(dni = value.uppercase(), dniError = null) }
    }
    fun onBirthDateChange(value: String) {
        _uiState.update { it.copy(birthDate = value, birthDateError = null) }
    }
    fun onCityChange(value: String) {
        _uiState.update { it.copy(city = value) }
    }
    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value, genderError = null) }
    }

    fun register() {
        val state = _uiState.value

        // Validaciones locales
        var hasError = false
        var newState =
                state.copy(
                        nameError = null,
                        surnameError = null,
                        emailError = null,
                        passwordError = null,
                        confirmPasswordError = null,
                        dniError = null,
                        birthDateError = null,
                        genderError = null
                )

        if (state.name.isBlank()) {
            newState = newState.copy(nameError = "El nombre es obligatorio")
            hasError = true
        }
        if (state.surname.isBlank()) {
            newState = newState.copy(surnameError = "Los apellidos son obligatorios")
            hasError = true
        }
        if (state.email.isBlank()) {
            newState = newState.copy(emailError = "El email es obligatorio")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            newState = newState.copy(emailError = "Formato de email no válido")
            hasError = true
        }
        if (state.dni.isBlank()) {
            newState = newState.copy(dniError = "El DNI es obligatorio")
            hasError = true
        } else if (!validarDNI(state.dni)) {
            newState = newState.copy(dniError = "DNI no válido. Verifica la letra")
            hasError = true
        }
        if (state.password.isBlank()) {
            newState = newState.copy(passwordError = "La contraseña es obligatoria")
            hasError = true
        } else if (!validarPassword(state.password)) {
            newState =
                    newState.copy(
                            passwordError = "Mín. 8 caracteres, 1 mayúscula, 1 minúscula, 1 número"
                    )
            hasError = true
        }
        if (state.confirmPassword.isBlank()) {
            newState = newState.copy(confirmPasswordError = "Confirma la contraseña")
            hasError = true
        } else if (state.password != state.confirmPassword) {
            newState = newState.copy(confirmPasswordError = "Las contraseñas no coinciden")
            hasError = true
        }
        if (state.birthDate.isBlank()) {
            newState = newState.copy(birthDateError = "La fecha de nacimiento es obligatoria")
            hasError = true
        }

        if (hasError) {
            _uiState.value = newState
            return
        }

        // Llamar a la API
        _uiState.update { it.copy(registerStatus = RegisterState.Loading) }

        viewModelScope.launch {
            try {
                val request =
                        RegisterRequest(
                                name = state.name.trim(),
                                surname = state.surname.trim(),
                                email = state.email.trim().lowercase(),
                                password = state.password,
                                confirmPassword = state.confirmPassword,
                                dni = state.dni.trim().uppercase(),
                                birthDate = state.birthDate,
                                gender = state.gender,
                                city = state.city.trim().ifBlank { null }
                        )

                val response = RetrofitClient.userService.register(request)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                                registerStatus =
                                        RegisterState.Success("¡Registro exitoso! Inicia sesión")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.update { it.copy(registerStatus = RegisterState.Error(errorBody)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(registerStatus = RegisterState.Error("Error de conexión: ${e.message}"))
                }
            }
        }
    }

    // Validar DNI español: 8 dígitos + letra correcta (módulo 23)
    private fun validarDNI(dni: String): Boolean {
        val patron = Regex("^\\d{8}[A-Z]$")
        if (!patron.matches(dni)) return false

        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numeros = dni.substring(0, 8).toIntOrNull() ?: return false
        val letraCorrecta = letras[numeros % 23]
        return dni[8] == letraCorrecta
    }

    // Validar contraseña robusta
    private fun validarPassword(password: String): Boolean {
        val patron = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
        return patron.matches(password)
    }
}

