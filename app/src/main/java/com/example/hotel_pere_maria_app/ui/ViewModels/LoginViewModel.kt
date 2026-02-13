package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.LoginRequest
import com.example.hotel_pere_maria_app.ui.Service.AuthService
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import com.example.hotel_pere_maria_app.ui.Service.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginState{
    object Idle : LoginState()
    object Loading: LoginState()
    data class Success(val token: String) : LoginState()
    data class Error (val message:String): LoginState()
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loginStatus : LoginState = LoginState.Idle
)

class LoginViewModel(): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(newValue: String){
        _uiState.update { it.copy(email = newValue) }
    }
    fun onPasswordChange(newValue: String){
        _uiState.update { it.copy(password = newValue) }
    }

    fun login() {
        val currentEmail = _uiState.value.email
        val currentPassword = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(loginStatus = LoginState.Loading) }

            try {
                val response = RetrofitClient.authService.login(LoginRequest(currentEmail, currentPassword))
                if (response.isSuccessful) {
                    val loginBody = response.body()
                    SessionManager.userToken = loginBody?.token
                    SessionManager.userInfo = loginBody?.user as UserInfo?
                    _uiState.update { it.copy(loginStatus = LoginState.Success(response.body()!!.token)) }

                } else {
                    _uiState.update { it.copy(loginStatus = LoginState.Error("Error de login")) }
                    println(response.message())
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loginStatus = LoginState.Error("Fallo de red")) }
                println(e.message)
            }
        }
    }
}