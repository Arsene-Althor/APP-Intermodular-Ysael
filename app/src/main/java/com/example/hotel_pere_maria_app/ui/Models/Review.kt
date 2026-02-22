package com.example.hotel_pere_maria_app.ui.Models

import android.util.Log
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Modelo de datos para Reseña basado en el esquema de la API */
data class Review(
        val review_id: String = "",
        val room_id: String = "",
        val user_id: String = "",
        val user_name: String = "",
        val rating: Int = 0,
        val comment: String = "",
        val createdAt: String? = null,
        val updatedAt: String? = null
)

/** Respuesta al crear una reseña */
data class ReviewCreateResponse(val message: String, val review: Review? = null)

/** Repositorio para gestionar las reseñas. Comunica con la API y mantiene el estado reactivo. */
object ReviewRepository {

    // ── Reseñas de una habitación ─────────────────────────────────────────────
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ── Operaciones ───────────────────────────────────────────────────────────

    /** Obtiene todas las reseñas de una habitación */
    suspend fun fetchReviewsByRoom(roomId: String) {
        _isLoading.value = true
        _error.value = null
        try {
            val response = RetrofitClient.reviewService.getReviewsByRoom(roomId)
            if (response.isSuccessful) {
                _reviews.update { response.body() ?: emptyList() }
                Log.d("REVIEW_REPO", "Cargadas ${_reviews.value.size} reseñas para $roomId")
            } else {
                _error.value = "Error al cargar reseñas: ${response.code()}"
                Log.e("REVIEW_REPO", "Error ${response.code()}")
            }
        } catch (e: Exception) {
            _error.value = "Error de conexión: ${e.message}"
            Log.e("REVIEW_REPO", "Exception: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /** Crea una nueva reseña */
    suspend fun createReview(roomId: String, rating: Int, comment: String): Result<String> {
        return try {
            val body =
                    mapOf("room_id" to roomId, "rating" to rating.toString(), "comment" to comment)
            val response = RetrofitClient.reviewService.createReview(body)
            if (response.isSuccessful) {
                // Recargar las reseñas tras crear
                fetchReviewsByRoom(roomId)
                Result.success(response.body()?.get("message") ?: "Reseña creada")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Elimina una reseña propia */
    suspend fun deleteReview(reviewId: String, roomId: String): Result<String> {
        return try {
            val body = mapOf("review_id" to reviewId)
            val response = RetrofitClient.reviewService.deleteReview(body)
            if (response.isSuccessful) {
                fetchReviewsByRoom(roomId)
                Result.success("Reseña eliminada")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearError() {
        _error.value = null
    }
    fun clearReviews() {
        _reviews.update { emptyList() }
    }
}
