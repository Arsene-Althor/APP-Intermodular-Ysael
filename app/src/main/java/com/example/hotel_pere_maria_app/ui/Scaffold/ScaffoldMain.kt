package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hotel_pere_maria_app.ui.Navegation.NavigationScaffold
import com.example.hotel_pere_maria_app.ui.Navegation.Routes

@Composable
fun ScaffoldMain(onLogout: () -> Unit = {}) {
    val ScaffoldnavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by ScaffoldnavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mostrarScafold =
        when {
            currentRoute == null -> true
            currentRoute.startsWith(Routes.ModReserva.route) -> false
            currentRoute.startsWith("ReservationAudit") -> false
            currentRoute == Routes.ReservationHistory.route -> false
            currentRoute == Routes.InvoiceHistory.route -> false
            else -> true
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (mostrarScafold) {
                TopAppBarState(ScaffoldnavController)
            }
        },
        bottomBar = {
            if (mostrarScafold) {
                BottomBookingBar(ScaffoldnavController, currentRoute)
            }
        },
    ) { innerpadding ->
        val pading = if(mostrarScafold) innerpadding else PaddingValues(0.dp)
        NavigationScaffold(ScaffoldnavController, modifier = Modifier.padding(pading), snackbarHostState,onLogout = onLogout )
    }
}
