package com.example.hotel_pere_maria_app.core.network

import com.example.hotel_pere_maria_app.data.model.ExtendStayResponse
import com.example.hotel_pere_maria_app.data.model.FlexibilityStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface FlexibilityService {
    @GET("bookings/{id}/flexibility")
    suspend fun getStatus(
        @Path("id") reservationId: String,
    ): Response<FlexibilityStatusResponse>

    @PATCH("bookings/{id}/request-early-checkin")
    suspend fun requestEarlyCheckin(
        @Path("id") reservationId: String,
        @Body body: Map<String, String>,
    ): Response<Map<String, Any>>

    @PATCH("bookings/{id}/request-late-checkout")
    suspend fun requestLateCheckout(
        @Path("id") reservationId: String,
        @Body body: Map<String, String>,
    ): Response<Map<String, Any>>

    /** Ampliar fecha de salida; cambia habitación si hay conflicto. */
    @PATCH("bookings/{id}/extend-stay")
    suspend fun extendStay(
        @Path("id") reservationId: String,
        @Body body: Map<String, String>,
    ): Response<ExtendStayResponse>
}

