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
    @SerializedName(value = "is_operational", alternate = ["isOperational"])
    val isOperational: Boolean = true,
    @SerializedName(value = "is_occupied_now", alternate = ["isOccupiedNow"])
    val isOccupiedNow: Boolean = false,
    /** Legacy API field; no usar para lógica de negocio. */
    val isAvailable: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("extra_services") val extraServices: List<String> = emptyList(),
    @SerializedName("offer_active") val offerActive: Boolean = false,
    @SerializedName("offer_percent") val offerPercent: Double = 0.0,
    @SerializedName("effective_price_per_night") val effectivePricePerNight: Double? = null,
    @SerializedName("base_price_per_night") val basePricePerNight: Double? = null,
) {
    fun isFreeNow(): Boolean = isOperational && !isOccupiedNow

    /** Precio a mostrar (oferta aplicada si la API envía effective_*). */
    fun displayPricePerNight(): Double = effectivePricePerNight ?: price_per_night

    /** URLs de galería: `images` o campo legacy `image` separado por comas. */
    fun galleryImageUrls(): List<String> {
        val fromList = images.map { it.trim() }.filter { it.isNotEmpty() }
        if (fromList.isNotEmpty()) return fromList
        return image.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    }
}
