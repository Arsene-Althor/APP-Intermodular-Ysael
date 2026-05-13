package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeUiEvent
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel
import android.content.Intent
import android.net.Uri

/**
 * Perfil + contacto: mapa, llamada y correo bajo «Soporte / más información».
 */
@Composable
fun MyAccountScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    homeViewModel: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeViewModel.uiEvent.collect { action ->
            when (action) {
                is HomeUiEvent.OpenMap -> {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(action.uri)).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.uri)))
                    }
                }
                is HomeUiEvent.MakeCall -> {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(action.uri)))
                }
                is HomeUiEvent.SendEmail -> {
                    val intent =
                        Intent(Intent.ACTION_SENDTO).apply {
                            data =
                                Uri.parse(
                                    "mailto:${action.address}?subject=${Uri.encode(action.subject)}",
                                )
                        }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("No tienes una aplicación de correo configurada")
                    }
                }
            }
        }
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Mi cuenta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Gestiona tu perfil y contacta con el hotel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Soporte y más información",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ServiceItem(Icons.Default.LocationOn, "Mapa") { homeViewModel.abrirMapa() }
                    ServiceItem(Icons.Default.Phone, "Llamar") { homeViewModel.llamarHotel() }
                    ServiceItem(Icons.Default.Email, "Correo") { homeViewModel.enviarCorreoHotel() }
                }
            }
        }

        FilledTonalButton(
            onClick = { navController.navigate(Routes.Reservations.route) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Mis reservas e historial")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.User.route) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Datos y ajustes del perfil")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.Add.route) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Nueva reserva (asistente clásico)")
        }
    }
}
