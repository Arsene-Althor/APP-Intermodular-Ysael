package com.example.hotel_pere_maria_app.ui.Models

import android.content.Context
import android.widget.Toast

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
