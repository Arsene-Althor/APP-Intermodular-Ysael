package com.example.hotel_pere_maria_app.ui.Models

import java.util.Date

data class Reservation(
    val reservation_id:String,
    val room_id: String,
    val user_id:String,
    val check_in: Date,
    val check_out: Date,
    val price: Number,
    val celation_date: Date,
    val createdBy:String
)
