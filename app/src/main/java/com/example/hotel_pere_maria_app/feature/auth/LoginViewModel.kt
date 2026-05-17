package com.example.hotel_pere_maria_app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.BuildConfig
import com.example.hotel_pere_maria_app.data.model.LoginRequest
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val loginStatus: LoginState = LoginState.Idle
)

class LoginViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }
    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue) }
    }

    fun login() {
        val currentEmail = _uiState.value.email
        val currentPassword = _uiState.value.password

        // Lanzamos una corrutina para hacer la llamada a la web (asíncrono)
        viewModelScope.launch {
            _uiState.update { it.copy(loginStatus = LoginState.Loading) }

            try {
                val response =
                        RetrofitClient.authService.login(
                                LoginRequest(currentEmail, currentPassword)
                        )
                if (response.isSuccessful) {
                    val loginBody = response.body()
                    if (loginBody == null || loginBody.token.isBlank()) {
                        _uiState.update {
                            it.copy(loginStatus = LoginState.Error("Respuesta vacía o sin token. Revisa la API."))
                        }
                        return@launch
                    }

                    SessionManager.userToken = loginBody.token
                    SessionManager.userInfo = loginBody.user
                    SessionManager.saveSession()
                    com.example.hotel_pere_maria_app.HotelApplication.appContext?.let {
                        com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityPollWorker.schedule(it)
                    }

                    _uiState.update {
                        it.copy(loginStatus = LoginState.Success(loginBody.token))
                    }
                } else {
                    val errText = response.errorBody()?.string()?.trim().orEmpty()
                    val hint = if (errText.isNotBlank()) errText else "Código HTTP ${response.code()}"
                    _uiState.update { it.copy(loginStatus = LoginState.Error("Login incorrecto: $hint")) }
                }
            } catch (e: JsonSyntaxException) {
                _uiState.update {
                    it.copy(
                        loginStatus = LoginState.Error(
                            "JSON inválido (¿misma API?). ${e.message?.take(120)}"
                        )
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        loginStatus = LoginState.Error(
                            "Sin conexión a ${BuildConfig.API_BASE_URL}\n" +
                                "${e.javaClass.simpleName}: ${e.message}\n" +
                                "Móvil físico: en local.properties define hotel.api.base.url=http://IP_DE_TU_PC:3011/"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loginStatus = LoginState.Error(
                            "${e.javaClass.simpleName}: ${e.message ?: "error"}"
                        )
                    )
                }
            }
        }
    }
}

