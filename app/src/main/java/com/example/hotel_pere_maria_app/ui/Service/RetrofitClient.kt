package com.example.hotel_pere_maria_app.ui.Service

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://51.255.198.93:3000/"

    private val authInterceptor = Interceptor{ chain ->
        val request = chain.request().newBuilder()

        SessionManager.userToken?.let {
            request.addHeader("Authorization", "Bearer ${it}")
        }
        chain.proceed(request.build())
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor).addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    private val retrofit : Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Creación de servicios
    val reservationService: ReservationService by lazy {
        retrofit.create(ReservationService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService:: class.java)
    }
}