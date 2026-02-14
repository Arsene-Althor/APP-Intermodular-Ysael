package com.example.hotel_pere_maria_app.ui.Navegation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.hotel_pere_maria_app.ui.Scaffold.ScaffoldMain
import com.example.hotel_pere_maria_app.ui.Views.Add
import com.example.hotel_pere_maria_app.ui.Views.Home
import com.example.hotel_pere_maria_app.ui.Views.Login
import com.example.hotel_pere_maria_app.ui.Views.ModReserva

@Composable
fun NavigationLogin(navigationController: NavHostController){
    NavHost(
        navController = navigationController,
        startDestination = Routes.Login.route
    ){
        composable(Routes.Login.route){
            Login(
                onLoginSuccess = {
                    navigationController.navigate(Routes.Scaffold.route){
                        popUpTo(Routes.Login.route){ inclusive = true}
                    }
                }
            )
        }

        composable(Routes.Scaffold.route){
            ScaffoldMain()
        }

    }
}

@Composable
fun NavigationScaffold(navigationController: NavHostController , modifier: Modifier, snackbarHostState: SnackbarHostState){
    NavHost(
        navController = navigationController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ){
        composable(Routes.Home.route){
            Home(onNavigate = {ruta ->
                navigationController.navigate(ruta)
            })
        }
        composable(Routes.User.route){
            Home({})
        }
        composable(Routes.Add.route){
            Add(snackbarHostState)
        }
        composable(Routes.ModReserva.route){
            ModReserva(snackbarHostState)
        }
    }
}