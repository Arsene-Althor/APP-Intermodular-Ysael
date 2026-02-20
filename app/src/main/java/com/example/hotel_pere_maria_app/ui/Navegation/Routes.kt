package com.example.hotel_pere_maria_app.ui.Navegation

sealed class Routes(val route: String){

    object  Login: Routes("Login")
    object  Scaffold: Routes("Scaffold")
    object  Home: Routes("Home")
    object  Add: Routes("Add")
    object User: Routes("User")
    object RoomList: Routes("RoomList")
    object RoomDetail: Routes("RoomDetail/{roomId}") {
        fun createRoute(roomId: String) = "RoomDetail/$roomId"
    }

}