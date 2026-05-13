package com.example.hotel_pere_maria_app.ui.Views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeUiEvent
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun Home(onNavigate: (String) -> Unit, snackbarHostState : SnackbarHostState) {
    val homeviewModel : HomeViewModel = viewModel()
    val reservas by homeviewModel.listMisReservas.collectAsState(initial = emptyList())
    val reservaReciente by homeviewModel.proximaReserva.collectAsState(initial = null)
    val state by homeviewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeviewModel.navigationEvent.collect { ruta ->
            onNavigate(ruta)
        }
    }
    LaunchedEffect(state.mensajeRespuesta) {
        state.mensajeRespuesta?.let {
            snackbarHostState.showSnackbar(it)
            homeviewModel.limpiarMensaje()
        }
    }
    LaunchedEffect(Unit) {
        homeviewModel.uiEvent.collect { action ->
            when(action){
                is HomeUiEvent.OpenMap -> {
                    val intent = Intent(Intent.ACTION_VIEW,Uri.parse(action.uri)).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(intent)
                    }catch (e: Exception){
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.uri)))
                    }
                }
                is HomeUiEvent.MakeCall ->{
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(action.uri)))
                }
                is HomeUiEvent.SendEmail ->{
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${action.address}?subject=${Uri.encode(action.subject)}")
                    }
                    try {
                        context.startActivity(intent)
                    }catch (e: Exception){
                        snackbarHostState.showSnackbar("No tienes una aplicación de correo configurada")
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Hola, Bienvenido!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column() {
                Text(text = "Servicios del Hotel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceItem(Icons.Default.LocationOn, "Mapa",{homeviewModel.abrirMapa()})
                    ServiceItem(Icons.Default.Phone, "Llamar", {homeviewModel.llamarHotel()})
                    ServiceItem(Icons.Default.Email, "Correo",{homeviewModel.enviarCorreoHotel()})
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Próxima estancia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (reservaReciente != null) {
                    proximaEstancia(reservaReciente!!)
                } else {
                    SinproxEstancia()
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (reservas.isEmpty()) {
                    Text(
                        text = "Aún no tienes reservas. Explora habitaciones en la pestaña inferior.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { onNavigate(Routes.Reservations.route) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Ver mis reservas e historial de actividad")
                }
            }
        }

    }
}

@Composable
fun proximaEstancia(reserva: Reservation){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = reserva.reservation_id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reserva.room_id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_in)}" +
                            " - " +
                            "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_out)}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

        }
    }
}
@Composable
fun SinproxEstancia(){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "No tienes reservas próximas. ¿A qué esperas?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ServiceItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = {onClick()}
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

