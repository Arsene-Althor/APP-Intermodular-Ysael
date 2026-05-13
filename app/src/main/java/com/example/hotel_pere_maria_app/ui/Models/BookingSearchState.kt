package com.example.hotel_pere_maria_app.ui.Models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Criterios de búsqueda tipo Booking (sin destino: un solo hotel). */
data class BookingSearchCriteria(
    val checkIn: String,
    val checkOut: String,
    val guests: Int,
    val priceMin: Double,
    val priceMax: Double,
)

/**
 * Estado compartido entre la Home de búsqueda y la pantalla de resultados.
 * (Evita serializar criterios largos en la ruta de navegación.)
 */
object BookingSearchState {
    private val _criteria = MutableStateFlow<BookingSearchCriteria?>(null)
    val criteria: StateFlow<BookingSearchCriteria?> = _criteria.asStateFlow()

    fun setCriteria(c: BookingSearchCriteria) {
        _criteria.value = c
    }

    fun clear() {
        _criteria.value = null
    }
}
