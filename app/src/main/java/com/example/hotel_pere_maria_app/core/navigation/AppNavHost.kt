package com.example.hotel_pere_maria_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavigationLogin(navigationController = navController)
}

