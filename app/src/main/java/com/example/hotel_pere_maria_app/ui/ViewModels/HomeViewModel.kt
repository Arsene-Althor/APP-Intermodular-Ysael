package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel: ViewModel() {

    private  val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    val listMisReservas = ReservationRepository.reservations

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun onEditarReservaClick() {
        viewModelScope.launch {
            // Enviamos la ruta exacta que configuraste sin Scaffold
            _navigationEvent.send(Routes.ModReserva.route)
        }
    }

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