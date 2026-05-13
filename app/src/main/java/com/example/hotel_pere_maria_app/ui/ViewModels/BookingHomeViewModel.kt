package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.BookingSearchCriteria
import com.example.hotel_pere_maria_app.ui.Models.BookingSearchState
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class BookingHomeViewModel : ViewModel() {
    private val _checkIn = MutableStateFlow("")
    private val _checkOut = MutableStateFlow("")
    val checkIn: StateFlow<String> = _checkIn.asStateFlow()
    val checkOut: StateFlow<String> = _checkOut.asStateFlow()

    private var checkInDate: Date? = null
    private var checkOutDate: Date? = null

    private val _guests = MutableStateFlow(2)
    val guests: StateFlow<Int> = _guests.asStateFlow()

    private val _priceRange = MutableStateFlow(20f..250f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch { ReservationRepository.fetchReservations() }
    }

    fun onCheckInMillis(millis: Long?) {
        millis ?: return
        val d = Date(millis)
        checkInDate = d
        _checkIn.value = android.text.format.DateFormat.format("dd/MM/yyyy", d).toString()
        _message.value = null
    }

    fun onCheckOutMillis(millis: Long?) {
        millis ?: return
        val d = Date(millis)
        checkOutDate = d
        _checkOut.value = android.text.format.DateFormat.format("dd/MM/yyyy", d).toString()
        _message.value = null
    }

    fun setGuests(g: Int) {
        _guests.value = g.coerceIn(1, 8)
    }

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun clearMessage() {
        _message.value = null
    }

    /**
     * Valida fechas y guarda criterios. Devuelve null si error (mensaje en [message]).
     */
    fun buildCriteriaOrError(): BookingSearchCriteria? {
        val ci = checkInDate
        val co = checkOutDate
        if (ci == null || co == null) {
            _message.value = "Selecciona fechas de entrada y salida."
            return null
        }
        if (!co.after(ci)) {
            _message.value = "La salida debe ser posterior a la entrada."
            return null
        }
        val limite = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        if (ci.before(limite)) {
            _message.value = "La fecha de entrada no es válida."
            return null
        }
        val pr = _priceRange.value
        val criteria =
            BookingSearchCriteria(
                checkIn = _checkIn.value,
                checkOut = _checkOut.value,
                guests = _guests.value,
                priceMin = pr.start.toDouble(),
                priceMax = pr.endInclusive.toDouble(),
            )
        BookingSearchState.setCriteria(criteria)
        return criteria
    }
}
