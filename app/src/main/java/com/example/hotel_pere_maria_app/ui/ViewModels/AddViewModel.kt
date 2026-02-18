package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.let

class AddViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AdduiState())
    val uiState : StateFlow<AdduiState> = _uiState

    private var check_in:Date? = null
    private  var check_out:Date? = null

private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun onDateSelected(millis: Long?,isCheckin: Boolean){
        try {
            millis?.let {

                val limiteDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val fechaSelct = Date(it)
                val fechaString = formatter.format(fechaSelct)

                if(it < limiteDate){
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
                    _uiState.update { it.copy(price = precioReci)}
                }
            }catch (e: Error){
                println(e.message)
            }
        }
    }

    fun onShowResumen(show: Boolean){
        _uiState.update { it.copy(showResumen = show) }
    }

    fun realizarReserva(){
        val datos = _uiState.value
        val userid = SessionManager.userInfo?.user_id ?: ""
        val requestReserva = mapOf(
            "room_id" to datos.room,
            "user_id" to userid,
            "check_in" to check_in,
            "check_out" to check_out,
            "price" to datos.price
        )

        viewModelScope.launch {
            try {
                var response = RetrofitClient.reservationService.addReservation(requestReserva as Map<String, String>)
                if(response.isSuccessful){
                    ReservationRepository.fetchReservations()
                    vaciarForm()
                    _uiState.update { it.copy(mensajeRespuesta = "¡Reserva realizada con exito!", errorRespusta = false) }
                }else{
                    val respuError = response.errorBody()?.string()
                    _uiState.update { it.copy(mensajeRespuesta = "${respuError}", errorRespusta = true) }
                }
            }catch (e: Error){
                _uiState.update { it.copy(mensajeRespuesta = "Fallo de conexión. Revisa tu internet.", errorRespusta = true) }
            }
        }
    }

    fun vaciarForm(){
        _uiState.update { it.copy(price = 0.00, check_in = "", check_out = "" ,room = "", showResumen = false, mensajeRespuesta = null ) }
        check_in = null
        check_out = null

    }
    fun limpiarMensaje(){
        _uiState.update { it.copy(mensajeRespuesta = null, errorRespusta = false) }
    }


}

data class AdduiState(
    val price: Double = 0.00,
    val check_in: String = "",
    val check_out: String = "",
    val room: String = "",
    val showResumen: Boolean = false,
    val mensajeRespuesta:String? = null,
    val errorRespusta: Boolean = false
)