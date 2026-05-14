package com.example.hotel_pere_maria_app.ui.Models

import android.util.Log
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

data class Reservation(
    val reservation_id: String,
    val room_id: String,
    val user_id: String,
    val check_in: Date,
    val check_out: Date,
    val price: Number,
    val cancelation_date: Date? = null,
    val createdBy: String,
    /** Asignado tras checkout en recepción (API). Si no es null, existe PDF en GET …/invoice. */
    val invoice_number: String? = null,
    val checkout_completed_at: Date? = null,
)

/** Respuesta de `GET /invoices?userId=`. */
data class InvoicesByUserResponse(
    val user_id: String? = null,
    val count: Int = 0,
    val reservations: List<Reservation> = emptyList(),
)

object ReservationRepository {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations : StateFlow<List<Reservation>> = _reservations

    suspend fun fetchReservations(){
        try {
            val response = RetrofitClient.reservationService.getMine()
            if(response.isSuccessful){
                val listReservas = response.body()
                if(listReservas != null){
                    _reservations.update { listReservas.toMutableList() }
                }
            }else{
                Log.e("API_ERROR", "Código de error: ${response.code()}")
            }
        }catch (e: Exception){
            Log.e("API_ERROR", "Error al cargar productos: ${e.message}")
        }
    }

    fun getReservationById(id:String): Reservation?{
        return _reservations.value.find { it.reservation_id == id }
    }
}
