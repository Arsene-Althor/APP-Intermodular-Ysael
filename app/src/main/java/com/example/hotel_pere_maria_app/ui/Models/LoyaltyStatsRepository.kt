package com.example.hotel_pere_maria_app.ui.Models

import android.util.Log
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object LoyaltyStatsRepository {
    private const val TAG = "LoyaltyStatsRepository"

    private val _stats = MutableStateFlow<ClientLoyaltyStatsDto?>(null)
    val stats: StateFlow<ClientLoyaltyStatsDto?> = _stats

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun fetchMyStats() {
        _loading.value = true
        _error.value = null
        try {
            val response = RetrofitClient.loyaltyStatsService.getMyStats()
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(response.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                _error.value = err.ifBlank { "Error ${response.code()}" }
                return
            }
            _stats.update { response.body() }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            _error.value = e.message ?: "Error de conexión"
        } finally {
            _loading.value = false
        }
    }
}
