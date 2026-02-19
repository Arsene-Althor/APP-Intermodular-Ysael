package com.example.hotel_pere_maria_app.ui.Navegation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hotel_pere_maria_app.ui.Scaffold.ScaffoldMain
import com.example.hotel_pere_maria_app.ui.Views.Add
import com.example.hotel_pere_maria_app.ui.Views.ForgotPassword
import com.example.hotel_pere_maria_app.ui.Views.Home
import com.example.hotel_pere_maria_app.ui.Views.Login
import com.example.hotel_pere_maria_app.ui.Views.ModReserva
import com.example.hotel_pere_maria_app.ui.Views.Profile
import com.example.hotel_pere_maria_app.ui.Views.Register

@Composable
fun NavigationLogin(navigationController: NavHostController) {
    NavHost(navController = navigationController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            Login(
                    onLoginSuccess = {
                        navigationController.navigate(Routes.Scaffold.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navigationController.navigate(Routes.Register.route) },
                    onNavigateToForgotPassword = {
                        navigationController.navigate(Routes.ForgotPassword.route)
                    }
            )
        }

        composable(Routes.Register.route) {
            Register(
                    onRegisterSuccess = {
                        // Volver al login tras registro exitoso
                        navigationController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navigationController.popBackStack() }
            )
        }

        composable(Routes.Scaffold.route) {
            ScaffoldMain(
                    onLogout = {
                        navigationController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
            )
        }

        // Pantalla de recuperar contraseña
        composable(Routes.ForgotPassword.route) {
            ForgotPassword(onNavigateToLogin = { navigationController.popBackStack() })
        }
    }
}

@Composable
fun NavigationScaffold(
        navigationController: NavHostController,
        modifier: Modifier,
        snackbarHostState: SnackbarHostState,
        onLogout: () -> Unit
) {
    NavHost(
        navController = navigationController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ){
        composable(Routes.Home.route){
            Home(onNavigate = {ruta ->
                navigationController.navigate(ruta)
            }, snackbarHostState = snackbarHostState)
        }
        composable(Routes.Add.route){
            Add(snackbarHostState)
        }
        composable("${Routes.ModReserva.route}/{reservaId}"){
            backStackEntry ->
            val reservaId = backStackEntry.arguments?.getString("reservaId")?: ""
            ModReserva(snackbarHostState, reservaId, onBack = {navigationController.popBackStack()})
        }
        composable(Routes.User.route) { Profile(onLogout = onLogout) }
    }
}
