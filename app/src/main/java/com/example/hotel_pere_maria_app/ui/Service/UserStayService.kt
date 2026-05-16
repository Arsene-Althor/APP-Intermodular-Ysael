package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.UserStayHistoryResponse
import com.example.hotel_pere_maria_app.ui.Models.UserStayStatsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserStayService {
    @GET("users/{id}/history")
    suspend fun getHistory(
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = "completed",
        @Query("year") year: Int? = null,
        @Query("room_type") roomType: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): Response<UserStayHistoryResponse>

    @GET("users/{id}/stats")
    suspend fun getStats(
        @Path("id") userId: String,
        @Query("year") year: Int? = null,
        @Query("room_type") roomType: String? = null,
        @Query("status") status: String? = "all",
    ): Response<UserStayStatsDto>
}
