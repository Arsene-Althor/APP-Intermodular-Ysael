package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel: ViewModel() {

    private  val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    val listMisReservas = ReservationRepository.reservations
    private val _uiState = MutableStateFlow(HomeState())
    val uiState : StateFlow<HomeState> = _uiState

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun onEditarReservaClick(id:String, reserva: Reservation) {
        if(reserva.cancelation_date!= null){
            _uiState.update { it.copy(mensajeRespuesta = "No es posible editar una reserva cancelada", errorRespusta = true) }
        }else{
            viewModelScope.launch {
                _navigationEvent.send("${Routes.ModReserva.route}/$id")
            }
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
    fun limpiarMensaje(){
        _uiState.update { it.copy(mensajeRespuesta = null, errorRespusta = false) }
    }

    fun abrirMapa() {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.OpenMap("geo:0,0?q=IES+Pere+Maria+Orts+i+Bosch+Benidorm"))
        }
    }

    fun llamarHotel() {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.MakeCall("tel:965000000"))
        }
    }
    fun enviarCorreoHotel() {
        viewModelScope.launch {
            _uiEvent.send(
                HomeUiEvent.SendEmail(
                    address = "info@hotelperemaria.com",
                    subject = "Consulta desde la App Móvil"
                )
            )
        }
    }

}

data class HomeState(
    val mensajeRespuesta:String? = null,
    val errorRespusta: Boolean = false
)

sealed class HomeUiEvent {
    data class MakeCall(val uri: String) : HomeUiEvent()
    data class OpenMap(val uri: String) : HomeUiEvent()
    data class SendEmail(val address: String, val subject: String) : HomeUiEvent()
}