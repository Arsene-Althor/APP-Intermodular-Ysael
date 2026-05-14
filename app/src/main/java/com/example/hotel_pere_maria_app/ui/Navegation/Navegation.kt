package com.example.hotel_pere_maria_app.ui.Navegation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hotel_pere_maria_app.ui.Scaffold.ScaffoldMain
import com.example.hotel_pere_maria_app.ui.Views.ForgotPassword
import com.example.hotel_pere_maria_app.ui.Views.Login
import com.example.hotel_pere_maria_app.ui.Views.ModReserva
import com.example.hotel_pere_maria_app.ui.Views.Profile
import com.example.hotel_pere_maria_app.ui.Views.Register
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import com.example.hotel_pere_maria_app.ui.Views.InvoiceHistoryScreen
import com.example.hotel_pere_maria_app.ui.Views.MyBookingsScreen
import com.example.hotel_pere_maria_app.ui.Views.ReservationAuditScreen
import com.example.hotel_pere_maria_app.ui.Views.ReservationHistoryScreen
import com.example.hotel_pere_maria_app.ui.Views.ReviewsScreen
import com.example.hotel_pere_maria_app.ui.Views.RoomDetail
import com.example.hotel_pere_maria_app.ui.booking.BookingConfirmScreen
import com.example.hotel_pere_maria_app.ui.booking.BookingHomeScreen
import com.example.hotel_pere_maria_app.ui.booking.BookingResultsScreen

@Composable
fun NavigationLogin(navigationController: NavHostController) {
    val startRoute =
        remember {
            if (!SessionManager.userToken.isNullOrBlank() && SessionManager.userInfo != null) {
                Routes.Scaffold.route
            } else {
                Routes.Login.route
            }
        }
    NavHost(navController = navigationController, startDestination = startRoute) {
        composable(Routes.Login.route) {
            Login(
                onLoginSuccess = {
                    navigationController.navigate(Routes.Scaffold.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navigationController.navigate(Routes.Register.route) },
                onNavigateToForgotPassword = { navigationController.navigate(Routes.ForgotPassword.route) },
            )
        }

        composable(Routes.Register.route) {
            Register(
                onRegisterSuccess = {
                    navigationController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navigationController.popBackStack() },
            )
        }

        composable(Routes.Scaffold.route) {
            ScaffoldMain(
                onLogout = {
                    navigationController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

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
    onLogout: () -> Unit,
) {
    NavHost(
        navController = navigationController,
        startDestination = Routes.BookingHome.route,
        modifier = modifier,
    ) {
        composable(Routes.BookingHome.route) {
            BookingHomeScreen(
                navController = navigationController,
                snackbarHostState = snackbarHostState,
            )
        }
        composable(Routes.BookingResults.route) {
            BookingResultsScreen(navController = navigationController)
        }
        composable(Routes.BookingConfirm.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            BookingConfirmScreen(navController = navigationController, roomId = roomId)
        }
        composable(Routes.Reservations.route) {
            MyBookingsScreen(navController = navigationController)
        }
        composable(Routes.ReservationHistory.route) {
            ReservationHistoryScreen(navController = navigationController)
        }
        composable(Routes.InvoiceHistory.route) {
            InvoiceHistoryScreen(navController = navigationController)
        }
        composable(Routes.ReservationAudit.route) { ReservationAuditScreen(navController = navigationController) }
        composable(Routes.Reviews.route) {
            ReviewsScreen(onBack = { navigationController.popBackStack() })
        }
        composable(Routes.RoomDetail.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            RoomDetail(navController = navigationController, roomId = roomId)
        }
        composable("${Routes.ModReserva.route}/{reservaId}") { backStackEntry ->
            val reservaId = backStackEntry.arguments?.getString("reservaId") ?: ""
            ModReserva(
                snackbarHostState,
                reservaId,
                onBack = { navigationController.popBackStack() },
            )
        }
        composable(Routes.User.route) {
            Profile(
                onLogout = onLogout,
                onOpenMyReviews = { navigationController.navigate(Routes.Reviews.route) },
            )
        }
    }
}
