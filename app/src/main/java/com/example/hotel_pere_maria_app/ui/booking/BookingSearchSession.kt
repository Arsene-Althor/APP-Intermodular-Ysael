package com.example.hotel_pere_maria_app.ui.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Criterios de la última búsqueda (fechas, huéspedes, precio). No es persistido. */
object BookingSearchSession {
    var checkInMillis: Long? by mutableStateOf(null)
    var checkOutMillis: Long? by mutableStateOf(null)
    var guests: Int by mutableIntStateOf(2)
    var priceMin: Double by mutableDoubleStateOf(20.0)
    var priceMax: Double by mutableDoubleStateOf(250.0)

    private val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun checkInDisplay(): String = checkInMillis?.let { displayFmt.format(Date(it)) } ?: ""

    fun checkOutDisplay(): String = checkOutMillis?.let { displayFmt.format(Date(it)) } ?: ""

    fun checkInIso(): String? = checkInMillis?.let { isoFmt.format(Date(it)) }

    fun checkOutIso(): String? = checkOutMillis?.let { isoFmt.format(Date(it)) }

    fun isComplete(): Boolean {
        val ci = checkInMillis ?: return false
        val co = checkOutMillis ?: return false
        return co > ci
    }

    fun clear() {
        checkInMillis = null
        checkOutMillis = null
        guests = 2
        priceMin = 20.0
        priceMax = 250.0
    }
}
