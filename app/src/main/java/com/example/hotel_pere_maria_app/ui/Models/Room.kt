package com.example.hotel_pere_maria_app.ui.Models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Habitación basado en el esquema de la API.
 * - isOperational: empleado marca si la habitación puede ofrecerse; si false no sale en búsqueda cliente.
 * - isOccupiedNow: calculado en API (reserva vigente sin cancelar).
 */
data class Room(
    val room_id: String,
    val type: String,
    val description: String,
    val image: String,
    val price_per_night: Double,
    val rate: Double = 0.0,
    val max_occupancy: Int,
    @SerializedName("is_operational") val isOperational: Boolean = true,
    @SerializedName("is_occupied_now") val isOccupiedNow: Boolean = false,
    /** Legacy API field; no usar para lógica de negocio. */
    val isAvailable: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /** Libre para reservar ahora (en servicio y sin huésped en curso). */
    fun isFreeNow(): Boolean = isOperational && !isOccupiedNow
}
