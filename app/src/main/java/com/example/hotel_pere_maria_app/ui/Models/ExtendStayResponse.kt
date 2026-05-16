package com.example.hotel_pere_maria_app.ui.Models

import com.google.gson.annotations.SerializedName

/** Respuesta PATCH /bookings/:id/extend-stay (solo campos usados en cliente). */
data class ExtendStayResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("reservation_id") val reservation_id: String? = null,
    @SerializedName("supplement") val supplement: Double? = null,
    @SerializedName("room_changed") val room_changed: Boolean? = null,
)
