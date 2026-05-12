package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.ReviewRepository
import kotlinx.coroutines.launch

/** Carga las reseñas del usuario logueado para la pestaña global «Reseñas». */
class MyReviewsViewModel : ViewModel() {

    val myReviews = ReviewRepository.myReviews
    val myReviewsLoading = ReviewRepository.myReviewsLoading
    val myReviewsError = ReviewRepository.myReviewsError

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { ReviewRepository.fetchMyReviews() }
    }
}
