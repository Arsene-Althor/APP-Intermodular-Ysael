package com.example.hotel_pere_maria_app.data.repository

import android.util.Log
import com.example.hotel_pere_maria_app.HotelApplication
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.data.model.Reservation
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ReservationRepository {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    suspend fun fetchReservations() {
        try {
            val response = RetrofitClient.reservationService.getMine()
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(response.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                Log.e("API_ERROR", "Código de error: ${response.code()} $err")
                return
            }
            val listReservas = response.body()
            if (listReservas != null) {
                _reservations.update { listReservas.toMutableList() }
                val ctx = HotelApplication.appContext
                if (ctx != null) {
                    FlexibilityNotificationHelper.checkStatusChanges(ctx, listReservas)
                }
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al cargar reservas: ${e.message}")
        }
    }

    fun getReservationById(id: String): Reservation? =
        _reservations.value.find { it.reservation_id == id }
}

