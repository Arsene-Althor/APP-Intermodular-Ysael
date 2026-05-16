package com.example.hotel_pere_maria_app.ui.Models

import java.util.Date

data class ClientLoyaltyStatsDto(
    val user_id: String? = null,
    val loyalty_tier: String? = null,
    val total_nights: Int = 0,
    val total_spent: Double = 0.0,
    val completed_stays_count: Int = 0,
    val last_stay_checkout_at: Date? = null,
    val favorite_season: String? = null,
    val favorite_month: Int? = null,
    val most_booked_room: MostBookedRoomDto? = null,
    val max_stay_streak: Int = 0,
    val summary: LoyaltySummaryDto? = null,
    val tier_thresholds: TierThresholdsDto? = null,
)

data class LoyaltySummaryDto(
    val total_reservations: Int = 0,
    val cancelled_reservations: Int = 0,
    val active_reservations: Int = 0,
)

data class TierThresholdsDto(
    val silver_nights: Int = 5,
    val gold_nights: Int = 15,
    val silver_spent: Int = 400,
    val gold_spent: Int = 1200,
)
