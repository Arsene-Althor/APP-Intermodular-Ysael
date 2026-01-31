package com.example.hotel_pere_maria_app.ui.Navegation

sealed class Routes(val route: String){

    object  Login: Routes("Login")
    object  Scaffold: Routes("Scaffold")
    object  Home: Routes("pantalla1")
    object  Favorites: Routes("pantalla2")
    object User: Routes("pantalla3")

}