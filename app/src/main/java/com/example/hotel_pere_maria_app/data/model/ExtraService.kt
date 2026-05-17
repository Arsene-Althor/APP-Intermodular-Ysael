package com.example.hotel_pere_maria_app.data.model

import com.google.gson.annotations.SerializedName

data class ExtraService(
    @SerializedName("service_id") val serviceId: String,
    val name: String,
    val active: Boolean = true,
)

