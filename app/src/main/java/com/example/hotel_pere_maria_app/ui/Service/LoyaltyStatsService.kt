package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.ClientLoyaltyStatsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface LoyaltyStatsService {
    /** Recalcula desde todas las reservas del usuario y devuelve documento ClientLoyaltyStats. */
    @GET("loyalty/me")
    suspend fun getMyStats(): Response<ClientLoyaltyStatsDto>

    @POST("loyalty/me/sync")
    suspend fun syncMyStats(): Response<Map<String, Any>>
}
