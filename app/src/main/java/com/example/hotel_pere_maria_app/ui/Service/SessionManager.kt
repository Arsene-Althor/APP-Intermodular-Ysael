package com.example.hotel_pere_maria_app.ui.Service

data class UserInfo(
    val user_id: String,
    val email: String,
    val name : String,
    val role: String
)
object SessionManager{
    var userToken: String? = null
    var userInfo : UserInfo? = null
}