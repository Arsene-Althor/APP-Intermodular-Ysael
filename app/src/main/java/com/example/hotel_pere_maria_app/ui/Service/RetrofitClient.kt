package com.example.hotel_pere_maria_app.ui.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.108:3000/"
    private val retrofit : Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Creación de servicios
    val reservationService: ReservationService by lazy {
        retrofit.create(ReservationService::class.java)
    }
}