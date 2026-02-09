package com.example.hotel_pere_maria_app.ui.Navegation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavegationMain(){
    val navcontrller = rememberNavController()

    NavigationLogin( navcontrller)
}