package com.example.hotel_pere_maria_app.data.model

import java.util.Locale

/**
 * Mapeo action (API / Mongo) → mensaje para usuario final.
 * Sin detalles técnicos ni JSON.
 */
object BookingHistoryFriendlyMapper {

    fun toUserMessage(action: String?): String {
        return when (action?.trim()?.uppercase(Locale.ROOT)) {
            "CREATED", "CREATE" -> "Reserva creada"
            "PAYMENT_RECEIVED", "PAYMENT", "PAID", "PAGO" -> "Pago recibido"
            "CHECK_IN", "CHECKIN", "CHECK_IN_DONE" -> "Check-in realizado"
            "EXTRA_SERVICE", "SERVICE_ADDED", "SERVICE_EXTRA" -> "Servicio extra añadido"
            "UPDATED", "UPDATE" -> "Cambios en tu reserva"
            "CANCELED", "CANCELLED", "CANCEL" -> "Reserva cancelada"
            else -> "Actividad en tu reserva"
        }
    }
}

