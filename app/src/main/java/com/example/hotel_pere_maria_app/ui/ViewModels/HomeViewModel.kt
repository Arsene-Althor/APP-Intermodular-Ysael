package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel: ViewModel() {
    val listMisReservas = ReservationRepository.reservations

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val proximaReserva: StateFlow<Reservation?> = listMisReservas
        .map { lista ->
            val hoy = Date()

            lista.filter {reserva ->
                reserva.check_in >= hoy && reserva.cancelation_date == null
            }.minByOrNull { it.check_in }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        cargarDatos()
    }

    private fun cargarDatos(){
        viewModelScope.launch {
            ReservationRepository.fetchReservations()
        }
    }
}