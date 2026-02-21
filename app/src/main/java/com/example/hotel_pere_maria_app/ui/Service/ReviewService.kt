package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path

/**
 * Servicio de Retrofit para gestionar las peticiones HTTP relacionadas con reseñas
 */
interface ReviewService {

    /**
     * Obtiene todas las reseñas de una habitación
     * @param roomId ID de la habitación (ej: HAB-101)
     * @return Response con lista de reseñas
     */
    @GET("review/room/{roomId}")
    suspend fun getReviewsByRoom(@Path("roomId") roomId: String): Response<List<Review>>

    /**
     * Crea una nueva reseña
     * @param body Map con room_id, rating, comment
     * @return Response con mensaje de confirmación
     */
    @retrofit2.http.POST("review/create")
    suspend fun createReview(@Body body: Map<String, String>): Response<Map<String, String>>

    /**
     * Elimina una reseña propia
     * @param body Map con review_id
     * @return Response con mensaje de confirmación
     */
    @HTTP(method = "DELETE", path = "review/delete", hasBody = true)
    suspend fun deleteReview(@Body body: Map<String, String>): Response<Map<String, String>>
}
