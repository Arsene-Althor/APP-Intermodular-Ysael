package com.example.hotel_pere_maria_app.data.model

import java.util.Date

data class UserStayHistoryResponse(
    val user_id: String? = null,
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val total_pages: Int = 1,
    val items: List<UserStayHistoryItem> = emptyList(),
)

data class UserStayHistoryItem(
    val reservation_id: String,
    val status: String? = null,
    val check_in: Date? = null,
    val check_out: Date? = null,
    val total_paid: Number = 0,
    val nights: Int = 0,
    val room: StayRoomDto? = null,
    val rating: StayRatingDto? = null,
)

data class StayRoomDto(
    val room_id: String,
    val name: String? = null,
    val type: String? = null,
    val description: String? = null,
    val image: String? = null,
)

data class StayRatingDto(
    val review_id: String? = null,
    val rating: Int = 0,
    val comment: String? = null,
)

data class UserStayStatsDto(
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
)

data class MostBookedRoomDto(
    val room_id: String,
    val type: String? = null,
    val bookings_count: Int = 0,
)

