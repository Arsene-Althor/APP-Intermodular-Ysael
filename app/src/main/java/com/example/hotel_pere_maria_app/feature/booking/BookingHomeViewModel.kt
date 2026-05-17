package com.example.hotel_pere_maria_app.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.data.model.Reservation
import com.example.hotel_pere_maria_app.data.repository.ReservationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class BookingHomeViewModel : ViewModel() {

    private val reservations = ReservationRepository.reservations

    val proximaReserva: StateFlow<Reservation?> =
        reservations
            .map { lista ->
                val hoy = Date()
                lista
                    .filter { it.check_in >= hoy && it.cancelation_date == null }
                    .minByOrNull { it.check_in }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { ReservationRepository.fetchReservations() }
    }
}

