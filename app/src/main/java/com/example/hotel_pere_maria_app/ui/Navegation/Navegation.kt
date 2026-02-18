package com.example.hotel_pere_maria_app.ui.Navegation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hotel_pere_maria_app.ui.Scaffold.ScaffoldMain
import com.example.hotel_pere_maria_app.ui.Views.Add
import com.example.hotel_pere_maria_app.ui.Views.Home
import com.example.hotel_pere_maria_app.ui.Views.Login
import com.example.hotel_pere_maria_app.ui.Views.Profile
import com.example.hotel_pere_maria_app.ui.Views.Register

@Composable
fun NavigationLogin(navigationController: NavHostController){
    NavHost(
        navController = navigationController,
        startDestination = Routes.Login.route
    ){
        composable(Routes.Login.route){
            Login(
                    onLoginSuccess = {
                        navigationController.navigate(Routes.Scaffold.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navigationController.navigate(Routes.Register.route) }
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
    ) {
        composable(Routes.Home.route) { Home() }
        composable(Routes.User.route) { Profile(onLogout = onLogout) }
        composable(Routes.Add.route) { Add(snackbarHostState) }
    }
}
