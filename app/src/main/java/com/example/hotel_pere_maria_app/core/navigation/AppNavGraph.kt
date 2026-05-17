package com.example.hotel_pere_maria_app.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hotel_pere_maria_app.ui.Scaffold.ScaffoldMain
import com.example.hotel_pere_maria_app.feature.auth.ForgotPassword
import com.example.hotel_pere_maria_app.feature.auth.Login
import com.example.hotel_pere_maria_app.feature.auth.Register
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.feature.invoice.InvoiceHistoryScreen
import com.example.hotel_pere_maria_app.feature.loyalty.ClientStatsScreen
import com.example.hotel_pere_maria_app.feature.loyalty.MyStaysScreen
import com.example.hotel_pere_maria_app.feature.loyalty.StayDetailScreen
import com.example.hotel_pere_maria_app.feature.profile.Profile
import com.example.hotel_pere_maria_app.feature.reservation.ModReserva
import com.example.hotel_pere_maria_app.feature.reservation.MyBookingsScreen
import com.example.hotel_pere_maria_app.feature.reservation.ReservationAuditScreen
import com.example.hotel_pere_maria_app.feature.reservation.ReservationHistoryScreen
import com.example.hotel_pere_maria_app.feature.review.ReviewsScreen
import com.example.hotel_pere_maria_app.feature.room.RoomDetail
import com.example.hotel_pere_maria_app.feature.booking.BookingConfirmScreen
import com.example.hotel_pere_maria_app.feature.booking.BookingHomeScreen
import com.example.hotel_pere_maria_app.feature.booking.BookingResultsScreen

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
            DisposableEffect(navigationController) {
                val cb: () -> Unit = {
                    navigationController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                SessionManager.onSessionExpired = cb
                onDispose {
                    if (SessionManager.onSessionExpired === cb) {
                        SessionManager.onSessionExpired = null
                    }
                }
            }
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
        composable(Routes.ClientStats.route) {
            ClientStatsScreen()
        }
        composable(Routes.MyStays.route) {
            MyStaysScreen(navController = navigationController)
        }
        composable(Routes.StayDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("reservationId") ?: ""
            StayDetailScreen(navController = navigationController, reservationId = id)
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

