package com.example.hotel_pere_maria_app.data.repository

import android.util.Log
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.data.model.ExtraService
import com.example.hotel_pere_maria_app.data.model.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio para gestionar las habitaciones.
 * _rooms     → todas las habitaciones (catálogo / detalle).
 * _availableRooms → habitaciones filtradas por fechas (para el diálogo de reserva).
 */
object RoomRepository {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _availableRooms = MutableStateFlow<List<Room>>(emptyList())
    val availableRooms: StateFlow<List<Room>> = _availableRooms

    private val _availableLoading = MutableStateFlow(false)
    val availableLoading: StateFlow<Boolean> = _availableLoading

    private val _availableError = MutableStateFlow<String?>(null)
    val availableError: StateFlow<String?> = _availableError

    private fun toISO(date: String): String {
        val t = date.trim()
        if (t.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return t
        return try {
            val inp = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed: Date? = inp.parse(t)
            if (parsed != null) out.format(parsed) else t
        } catch (e: Exception) {
            t
        }
    }

    suspend fun fetchRooms() {
        _isLoading.value = true
        _error.value = null
        try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                val raw = response.body() ?: emptyList()
                _rooms.update { raw.filter { it.isOperational } }
                Log.d("ROOM_REPO", "fetchRooms: ${_rooms.value.size} habitaciones")
            } else {
                _error.value = "Error al cargar habitaciones: ${response.code()}"
                Log.e("ROOM_REPO", "fetchRooms error ${response.code()}")
            }
        } catch (e: Exception) {
            _error.value = "Error de conexión: ${e.message}"
            Log.e("ROOM_REPO", "fetchRooms exception: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getRoomById(roomId: String): Room? {
        val cached = _rooms.value.find { it.room_id == roomId }
        if (cached != null) {
            Log.d("ROOM_REPO", "getRoomById: encontrada en caché → $roomId")
            return cached
        }
        Log.d("ROOM_REPO", "getRoomById: no está en caché, recargando lista…")
        return try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                _rooms.update { list.filter { it.isOperational } }
                list.find { it.room_id == roomId && it.isOperational }
            } else {
                Log.e("ROOM_REPO", "getRoomById fallback error ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ROOM_REPO", "getRoomById fallback exception: ${e.message}")
            null
        }
    }

    suspend fun fetchAvailableRoomsByDates(checkIn: String, checkOut: String, guests: Int = 2) {
        _availableLoading.value = true
        _availableError.value = null
        _availableRooms.update { emptyList() }

        val checkInISO = toISO(checkIn)
        val checkOutISO = toISO(checkOut)
        Log.d("ROOM_REPO", "fetchAvailable: $checkInISO → $checkOutISO guests=$guests")

        try {
            val response = RetrofitClient.roomService
                .getAvailableRoomsByDates(checkInISO, checkOutISO, guests)

            if (response.isSuccessful) {
                _availableError.value = null
                val list = (response.body() ?: emptyList()).filter { it.isOperational }
                _availableRooms.update { list }
                Log.d("ROOM_REPO", "fetchAvailable OK: ${list.size} habitaciones")
            } else {
                val errBody = response.errorBody()?.string().orEmpty()
                Log.w("ROOM_REPO", "room/available ${response.code()}: $errBody")
                _availableError.value = "Disponibilidad: ${response.code()} ${errBody.take(200)}"
                fetchAvailableFallback()
            }
        } catch (e: Exception) {
            Log.e("ROOM_REPO", "fetchAvailable exception: ${e.message}")
            _availableError.value = e.message
            fetchAvailableFallback()
        } finally {
            _availableLoading.value = false
        }
    }

    /** Fallback: room/all filtrado por en servicio y libre ahora (no sustituye solape por fechas). */
    private suspend fun fetchAvailableFallback() {
        try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                val list = (response.body() ?: emptyList()).filter { it.isOperational && it.isFreeNow() }
                _availableRooms.update { list }
                Log.d("ROOM_REPO", "fallback OK: ${list.size} candidatas")
            } else {
                _availableError.value = "No se pudieron cargar las habitaciones (${response.code()})"
            }
        } catch (e: Exception) {
            _availableError.value = "Error de conexión: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearAvailableError() {
        _availableError.value = null
    }

    suspend fun fetchExtraServices(): List<ExtraService> {
        return try {
            val r = RetrofitClient.roomService.listExtraServices()
            if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

