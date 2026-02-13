package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.Reservation
import retrofit2.Response
import retrofit2.http.GET

interface ReservationService{
    @GET("reservation/mine")
    suspend fun getMine(): Response<List<Reservation>>
}