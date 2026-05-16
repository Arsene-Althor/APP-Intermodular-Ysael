package com.example.hotel_pere_maria_app.ui.Service

import org.json.JSONObject

fun parseApiError(raw: String?): String {
    if (raw.isNullOrBlank()) return "Error del servidor"
    return try {
        val jo = JSONObject(raw)
        val err = jo.optString("error", "").trim()
        val det = jo.optString("detalle", "").trim()
        when {
            err.isNotBlank() && det.isNotBlank() -> "$err. $det"
            err.isNotBlank() -> err
            det.isNotBlank() -> det
            else -> raw.take(240)
        }
    } catch (_: Exception) {
        raw.take(240).ifBlank { "Error del servidor" }
    }
}
