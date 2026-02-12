package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.Reservation
import retrofit2.Response
import retrofit2.http.GET

interface ReservationService{
    @GET("reservation/all")
    suspend fun getAll(): Response<List<Reservation>>
}