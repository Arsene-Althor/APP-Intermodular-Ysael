package com.example.hotel_pere_maria_app.data.model

import android.content.Context
import android.widget.Toast
import com.example.hotel_pere_maria_app.data.repository.LoyaltyStatsRepository
import com.example.hotel_pere_maria_app.data.model.loyaltyTierLabel

/** Toast breve tras acciones que suman stats (gamificación ligera). */
object LoyaltyFeedback {
    suspend fun toastAfterAction(context: Context) {
        LoyaltyStatsRepository.fetchMyStats()
        val s = LoyaltyStatsRepository.stats.value ?: return
        val tier = loyaltyTierLabel(s.loyalty_tier)
        val nights = s.total_nights
        val msg = "★ Rango $tier · $nights noches acumuladas"
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}

