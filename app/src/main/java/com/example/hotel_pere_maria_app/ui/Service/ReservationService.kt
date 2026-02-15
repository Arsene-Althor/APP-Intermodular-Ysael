package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.Reservation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ReservationService{
    @GET("reservation/mine")
    suspend fun getMine(): Response<List<Reservation>>

    @POST("reservation/getPrice")
    suspend fun getPrice(@Body datos : Map<String, String>): Response<Map<String,Double>>

    @POST("reservation/add")
    suspend fun addReservation(@Body datos: Map<String, String>): Response<Map<String, String>>

    @POST("reservation/cancel")
    suspend fun cancelReservation(@Body datos: Map<String, String>):Response<Map<String, Any>>

    @POST("reservation/getCancelationPrice")
    suspend fun cancelationPrice(@Body datos: Map<String, String>): Response<Map<String, String>>

    @PUT("reservation/update")
    suspend fun updateReservation(@Body datos : Map<String, String>): Response<Map<String, Any>>
}