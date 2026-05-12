package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Models.Review
import com.example.hotel_pere_maria_app.ui.Models.ReviewRepository
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * ViewModel para gestionar las reseñas de una habitación. Controla: carga de reseñas, comprobación
 * de si el usuario puede reseñar, creación y borrado de reseñas.
 */
class ReviewViewModel : ViewModel() {

    // Estado de las reseñas
    val reviews: StateFlow<List<Review>> = ReviewRepository.reviews
    val isLoading: StateFlow<Boolean> = ReviewRepository.isLoading
    val error: StateFlow<String?> = ReviewRepository.error

    // Si el usuario puede escribir una reseña (tiene reserva en la habitación)
    private val _canReview = MutableStateFlow(false)
    val canReview: StateFlow<Boolean> = _canReview

    // Si el usuario ya tiene una reseña propia en esta habitación
    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview

    // Estado del formulario
    private val _selectedRating = MutableStateFlow(0)
    val selectedRating: StateFlow<Int> = _selectedRating

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText

    // Estado de envío
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitMessage = MutableStateFlow<String?>(null)
    val submitMessage: StateFlow<String?> = _submitMessage

    /** Carga las reseñas de una habitación y comprueba si el usuario puede reseñar */
    fun loadReviews(roomId: String) {
        viewModelScope.launch {
            supervisorScope {
                val reservas = async { ReservationRepository.fetchReservations() }
                val reseñas = async { ReviewRepository.fetchReviewsByRoom(roomId) }
                reservas.await()
                reseñas.await()
                checkCanReview(roomId)
            }
        }
    }

    /** Comprueba si el usuario tiene una reserva en la habitación y si ya ha dejado una reseña */
    private fun checkCanReview(roomId: String) {
        val userId = SessionManager.userInfo?.user_id
        if (userId.isNullOrBlank()) {
            _canReview.value = false
            _userReview.value = null
            return
        }

        // Comprobar si tiene reserva en esta habitación (cualquier rol con reserva en BD puede reseñar vía API)
        val reservations = ReservationRepository.reservations.value
        val hasReservation = reservations.any { it.room_id == roomId }
        _canReview.value = hasReservation

        // Buscar si ya tiene una reseña en esta habitación
        val existing = ReviewRepository.reviews.value.find { it.user_id == userId }
        _userReview.value = existing
    }

    /** Actualiza la puntuación seleccionada */
    fun setRating(rating: Int) {
        _selectedRating.value = rating
    }

    /** Actualiza el texto del comentario */
    fun setComment(text: String) {
        _commentText.value = text
    }

    /** Envía una nueva reseña */
    fun submitReview(roomId: String) {
        val rating = _selectedRating.value
        val comment = _commentText.value.trim()

        if (rating < 1 || rating > 5) {
            _submitMessage.value = "Selecciona una puntuación de 1 a 5"
            return
        }
        if (comment.isEmpty()) {
            _submitMessage.value = "Escribe un comentario"
            return
        }

        _isSubmitting.value = true
        viewModelScope.launch {
            val result = ReviewRepository.createReview(roomId, rating, comment)
            result.fold(
                    onSuccess = {
                        _submitMessage.value = "¡Reseña publicada!"
                        _selectedRating.value = 0
                        _commentText.value = ""
                        checkCanReview(roomId)
                    },
                    onFailure = { _submitMessage.value = "Error: ${it.message}" }
            )
            _isSubmitting.value = false
        }
    }

    /** Elimina la reseña propia del usuario */
    fun deleteMyReview(roomId: String) {
        val reviewId = _userReview.value?.review_id ?: return
        _isSubmitting.value = true
        viewModelScope.launch {
            val result = ReviewRepository.deleteReview(reviewId, roomId)
            result.fold(
                    onSuccess = {
                        _submitMessage.value = "Reseña eliminada"
                        _userReview.value = null
                        checkCanReview(roomId)
                    },
                    onFailure = { _submitMessage.value = "Error: ${it.message}" }
            )
            _isSubmitting.value = false
        }
    }

    fun clearMessage() {
        _submitMessage.value = null
    }
}
