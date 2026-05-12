package com.example.hotel_pere_maria_app.ui.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.String
import kotlin.collections.Map

class ModReservaViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val reservaId: String = checkNotNull(savedStateHandle["reservaId"])

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _uiState = MutableStateFlow(ModuiState())
    val uiState : StateFlow<ModuiState> = _uiState

    private var check_in:Date? = null
    private  var check_out:Date? = null

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun onDateSelected(millis: Long?,isCheckin: Boolean){
        try {
            millis?.let {
                val limiteDate = Date(Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis)

                val fechaSelct = Date(it)
                val fechaString = formatter.format(fechaSelct)

                if(fechaSelct < limiteDate){
                    _uiState.update { it.copy(mensajeRespuesta = "¡La fecha seleccionada no esta disponible!", errorRespusta = false) }
                }else{
                    if(isCheckin){
                        check_in = fechaSelct
                        _uiState.update { it.copy(check_in = fechaString) }

                    }else{
                        check_out = fechaSelct
                        _uiState.update { it.copy(check_out  =fechaString) }
                    }
                    if(_uiState.value.room != "" && _uiState.value.room.isNotEmpty()){
                        actualizarPrecio()
                    }
                }

            }
        }catch (e: Exception){
            println(e.message)
        }

    }

    fun onRoomChanged(newValue: String){
        if(check_out != null && check_in != null){
            _uiState.update { it.copy(room = newValue) }
            actualizarPrecio()

        }
    }

    fun actualizarPrecio(){
        val datos = _uiState.value
        val userid = SessionManager.userInfo?.user_id ?: ""
        val requestPrecio = mapOf(
            "room_id" to datos.room,
            "user_id" to userid,
            "check_in" to check_in,
            "check_out" to check_out
        )
        viewModelScope.launch {
            try {
                val response = RetrofitClient.reservationService.getPrice(requestPrecio as Map<String, String>)
                if(response.isSuccessful && response.body() != null){
                    val precioReci = response.body()!!.get("precio") ?: 0.0
                    _uiState.update { it.copy(priceNuevo = precioReci)}
                }
            }catch (e: Exception){
                println(e.message)
            }
        }
    }

    fun onShowResumenMod(show: Boolean){
        actualizarPrecio()
        _uiState.update { it.copy(showResumenMod = show) }
    }

    fun onShowResumenCancel(show: Boolean){
        val datos = _uiState.value
        val ahora = System.currentTimeMillis()

        val requestPrecioCancel = mapOf(
            "reservation_id" to datos.reservation_id,
            "cancelation_date" to ahora
        )

        viewModelScope.launch {
            try {
                var response = RetrofitClient.reservationService.cancelationPrice(requestPrecioCancel as Map<String,String>)
                if(response.isSuccessful && response.body()!= null){
                    val precioCanc: Double = response.body()!!.get("precio")?.toDouble() ?: 0.0
                    _uiState.update { it.copy(precioCancel = precioCanc,showResumenCancel = show) }
                }else{
                    val respuError = response.errorBody()?.string()
                    _uiState.update { it.copy(mensajeRespuesta = "${respuError}", errorRespusta = true) }
                }
            }catch (e: Exception){
                _uiState.update { it.copy(mensajeRespuesta = "Fallo de conexión. Revisa tu internet.", errorRespusta = true) }
            }
        }


    }

    fun cancelarReserva(){
        val datos = _uiState.value
        viewModelScope.launch {
            try {
                var response = RetrofitClient.reservationService.cancelReservation(
                    datos.reservation_id,
                    datos.precioCancel
                )
                if(response.isSuccessful){
                    ReservationRepository.fetchReservations()
                    _uiState.update { it.copy(mensajeRespuesta = "¡Reserva cancelada con exito!", errorRespusta = false) }
                    _navigationEvent.send(Unit)
                }else{
                    val respuError = response.errorBody()?.string()
                    cargarDatos()
                    _uiState.update { it.copy(mensajeRespuesta = "${respuError}", errorRespusta = true) }
                }
            }catch (e: Exception){
                cargarDatos()
                _uiState.update { it.copy(mensajeRespuesta = "Fallo de conexión. Revisa tu internet.", errorRespusta = true) }
            }
        }
    }
    fun modificarReserva(){
        val datos = _uiState.value
        val userid = SessionManager.userInfo?.user_id ?: ""
        val requestReserva = mapOf(
            "reservation_id" to reservaId,
            "room_id" to datos.room,
            "user_id" to userid,
            "check_in" to check_in,
            "check_out" to check_out,
            "price" to datos.priceNuevo
        )

        viewModelScope.launch {
            try {
                var response = RetrofitClient.reservationService.updateReservation(requestReserva as Map<String, String>)
                if(response.isSuccessful){
                    ReservationRepository.fetchReservations()
                    cargarDatos()
                    _uiState.update { it.copy(mensajeRespuesta = "¡Reserva modificada con exito!", errorRespusta = false) }
                }else{
                    val respuError = response.errorBody()?.string()
                    println("Error en petición: ${respuError}")
                    _uiState.update { it.copy(mensajeRespuesta = "${respuError}", errorRespusta = true) }
                }
            }catch (e: Exception){
                _uiState.update { it.copy(mensajeRespuesta = "Fallo de conexión. Revisa tu internet.", errorRespusta = true) }
            }
        }
    }

    fun cargarDatos(){
       val reserva = ReservationRepository.getReservationById(reservaId)

        reserva?.let{item ->
            _uiState.update { currentState ->
                currentState.copy(
                    reservation_id = item.reservation_id,
                    priceActual = item.price.toDouble(),
                    check_in = formatter.format(item.check_in),
                    check_out = formatter.format(item.check_out),
                    room = item.room_id,
                    priceNuevo = 0.00,
                    showResumenMod = false,
                    showResumenCancel = false,
                    mensajeRespuesta = null,
                    errorRespusta = false
                )
            }
            check_in = item.check_in
            check_out = item.check_out
        }
    }
    fun limpiarMensaje(){
        _uiState.update { it.copy(mensajeRespuesta = null, errorRespusta = false) }
    }
}

data class ModuiState(
    val reservation_id:String = "",
    val priceActual: Double = 0.00,
    val priceNuevo: Double = 0.00,
    val check_in: String = "",
    val check_out: String = "",
    val room: String = "",
    val showResumenMod: Boolean = false,
    val showResumenCancel: Boolean = false,
    val mensajeRespuesta:String? = null,
    val errorRespusta: Boolean = false,
    val precioCancel: Double = 0.00
)