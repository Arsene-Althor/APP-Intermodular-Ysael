package com.example.hotel_pere_maria_app.data.repository

import android.util.Log
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.data.model.UserStayHistoryItem
import com.example.hotel_pere_maria_app.data.model.UserStayStatsDto
import com.example.hotel_pere_maria_app.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object UserStayRepository {
    private const val TAG = "UserStayRepository"

    private val _history = MutableStateFlow<List<UserStayHistoryItem>>(emptyList())
    val history: StateFlow<List<UserStayHistoryItem>> = _history

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading

    private val _stats = MutableStateFlow<UserStayStatsDto?>(null)
    val stats: StateFlow<UserStayStatsDto?> = _stats

    suspend fun fetchHistory(
        userId: String,
        page: Int = 1,
        status: String = "completed",
    ): Result<Int> {
        _historyLoading.value = true
        return try {
            val res = RetrofitClient.userStayService.getHistory(userId, page = page, limit = 30, status = status)
            if (!res.isSuccessful) {
                val err = res.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(res.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                return Result.failure(Exception(err.ifBlank { "Error ${res.code()}" }))
            }
            val body = res.body()
            _history.update { body?.items.orEmpty() }
            Result.success(body?.total_pages ?: 1)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            Result.failure(e)
        } finally {
            _historyLoading.value = false
        }
    }

    suspend fun fetchStats(userId: String) {
        try {
            val res = RetrofitClient.userStayService.getStats(userId)
            if (!res.isSuccessful) return
            _stats.update { res.body() }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }
}

