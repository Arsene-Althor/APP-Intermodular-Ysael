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
    /**
     * API envía `is_operational` (lean) y/o `isOperational` (spread Mongo). Gson ignoraba el default
     * Kotlin si faltaba la clave → false y el filtro vaciaba la lista.
     */
    @SerializedName(value = "is_operational", alternate = ["isOperational"])
    val isOperational: Boolean? = null,
    @SerializedName(value = "is_occupied_now", alternate = ["isOccupiedNow"])
    val isOccupiedNow: Boolean? = null,
    /** Legacy API field; no usar para lógica de negocio. */
    val isAvailable: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    /** En servicio para el cliente: null o true (solo false excluye). */
    fun isInService(): Boolean = isOperational != false

    fun isOccupiedNowEffective(): Boolean = isOccupiedNow == true

    fun isFreeNow(): Boolean = isInService() && !isOccupiedNowEffective()

    /** URLs para galería: varias separadas por coma o una sola. */
    fun galleryImageUrls(): List<String> {
        val parts = image.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        return parts.ifEmpty { listOf(image) }
    }
}
