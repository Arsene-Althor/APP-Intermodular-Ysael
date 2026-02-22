package com.example.hotel_pere_maria_app.ui.Models

import android.util.Log
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de datos para Habitación basado en el esquema de la API
 */
data class Room(
    val room_id: String,
    val type: String,
    val description: String,
    val image: String,
    val price_per_night: Double,
    val rate: Double = 0.0,
    val max_occupancy: Int,
    val isAvailable: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Repositorio para gestionar las habitaciones.
 * _rooms     → todas las habitaciones (para RoomList).
 * _availableRooms → habitaciones filtradas por fechas (para el diálogo de reserva).
 * _availableError → error exclusivo del diálogo, no afecta a RoomList.
 */
object RoomRepository {

    // ── Lista completa (RoomList) ──────────────────────────────────────────────
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ── Lista de disponibles por fechas (diálogo de reserva) ──────────────────
    private val _availableRooms = MutableStateFlow<List<Room>>(emptyList())
    val availableRooms: StateFlow<List<Room>> = _availableRooms

    private val _availableLoading = MutableStateFlow(false)
    val availableLoading: StateFlow<Boolean> = _availableLoading

    // null = sin error; String = mensaje de error
    private val _availableError = MutableStateFlow<String?>(null)
    val availableError: StateFlow<String?> = _availableError

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Convierte dd/MM/yyyy → yyyy-MM-dd (ISO 8601) */
    private fun toISO(date: String): String {
        return try {
            val inp = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed: Date? = inp.parse(date)
            if (parsed != null) out.format(parsed) else date
        } catch (e: Exception) { date }
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    /** Obtiene TODAS las habitaciones del sistema (para RoomList). */
    suspend fun fetchRooms() {
        _isLoading.value = true
        _error.value = null
        try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                _rooms.update { response.body()?.toMutableList() ?: emptyList() }
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

    /** Obtiene una habitación por ID. Busca primero en la caché local. */
    suspend fun getRoomById(roomId: String): Room? {
        // 1. Buscar en la lista ya cargada (evita llamada de red innecesaria)
        val cached = _rooms.value.find { it.room_id == roomId }
        if (cached != null) {
            Log.d("ROOM_REPO", "getRoomById: encontrada en caché → $roomId")
            return cached
        }
        // 2. Si no está en caché, intentar cargar todas y buscar de nuevo
        Log.d("ROOM_REPO", "getRoomById: no está en caché, recargando lista…")
        return try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                _rooms.update { list }
                list.find { it.room_id == roomId }
            } else {
                Log.e("ROOM_REPO", "getRoomById fallback error ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ROOM_REPO", "getRoomById fallback exception: ${e.message}")
            null
        }
    }

    /**
     * Obtiene habitaciones disponibles para un rango de fechas.
     *
     * Estrategia:
     *  1. Intenta el endpoint `room/available` con las fechas en ISO.
     *  2. Si devuelve error (400, etc.) hace fallback a `room/all` y filtra
     *     por isAvailable = true en el cliente.
     *
     * El resultado se guarda en _availableRooms para no contaminar _rooms.
     * Los errores van a _availableError, no a _error.
     */
    suspend fun fetchAvailableRoomsByDates(checkIn: String, checkOut: String) {
        _availableLoading.value = true
        _availableError.value = null
        _availableRooms.update { emptyList() }   // limpiar resultado anterior

        val checkInISO  = toISO(checkIn)
        val checkOutISO = toISO(checkOut)
        Log.d("ROOM_REPO", "fetchAvailable: $checkInISO → $checkOutISO")

        try {
            val response = RetrofitClient.roomService
                .getAvailableRoomsByDates(checkInISO, checkOutISO)

            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                _availableRooms.update { list }
                Log.d("ROOM_REPO", "fetchAvailable OK: ${list.size} habitaciones")
            } else {
                // ── FALLBACK: room/all filtrado por isAvailable ────────────
                Log.w("ROOM_REPO", "room/available devolvió ${response.code()}, usando fallback")
                fetchAvailableFallback()
            }
        } catch (e: Exception) {
            Log.e("ROOM_REPO", "fetchAvailable exception: ${e.message}")
            fetchAvailableFallback()
        } finally {
            _availableLoading.value = false
        }
    }

    /** Fallback: carga todas y filtra por isAvailable=true. */
    private suspend fun fetchAvailableFallback() {
        try {
            val response = RetrofitClient.roomService.getAllRooms()
            if (response.isSuccessful) {
                val list = (response.body() ?: emptyList()).filter { it.isAvailable }
                _availableRooms.update { list }
                Log.d("ROOM_REPO", "fallback OK: ${list.size} disponibles")
            } else {
                _availableError.value = "No se pudieron cargar las habitaciones (${response.code()})"
            }
        } catch (e: Exception) {
            _availableError.value = "Error de conexión: ${e.message}"
        }
    }

    fun clearError() { _error.value = null }
    fun clearAvailableError() { _availableError.value = null }
}
