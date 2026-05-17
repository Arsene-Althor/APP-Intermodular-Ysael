package com.example.hotel_pere_maria_app.data.model

import java.util.Date

/**
 * Solo campos necesarios para el historial del cliente.
 * previous_state / new_state / resumen_cambios no se modelan: no deben mostrarse en UI.
 */
data class BookingAuditEntry(
    val booking_id: String? = null,
    val action: String? = null,
    val timestamp: Date? = null,
)

