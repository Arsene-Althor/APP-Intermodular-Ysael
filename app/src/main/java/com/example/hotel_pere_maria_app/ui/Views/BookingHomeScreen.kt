package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.BookingHomeViewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel

/** Home estilo Booking: próxima reserva + motor de búsqueda (fechas, personas, precio). */
@Composable
fun BookingHomeScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    bookingVm: BookingHomeViewModel = viewModel(),
    homeVm: HomeViewModel = viewModel(),
) {
    val checkIn by bookingVm.checkIn.collectAsState()
    val checkOut by bookingVm.checkOut.collectAsState()
    val guests by bookingVm.guests.collectAsState()
    val priceRange by bookingVm.priceRange.collectAsState()
    val msg by bookingVm.message.collectAsState()

    val reservaReciente by homeVm.proximaReserva.collectAsState(initial = null)
    val reservas by homeVm.listMisReservas.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        homeVm.navigationEvent.collect { ruta -> navController.navigate(ruta) }
    }

    LaunchedEffect(msg) {
        msg?.let {
            snackbarHostState.showSnackbar(it)
            bookingVm.clearMessage()
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
            text = "Busca tu estancia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Próxima reserva",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (reservaReciente != null) {
            proximaEstancia(reservaReciente!!)
        } else {
            SinproxEstancia()
        }
        if (reservas.isNotEmpty()) {
            FilledTonalButton(
                onClick = { navController.navigate(Routes.Reservations.route) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ver mis reservas")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Cuándo?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FechaInputSimple(
                    label = "Entrada",
                    fecha = checkIn,
                    onFechaSelected = { bookingVm.onCheckInMillis(it) },
                )
                FechaInputSimple(
                    label = "Salida",
                    fecha = checkOut,
                    onFechaSelected = { bookingVm.onCheckOutMillis(it) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Personas: $guests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Slider(
                    value = guests.toFloat(),
                    onValueChange = { bookingVm.setGuests(it.toInt()) },
                    valueRange = 1f..8f,
                    steps = 6,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text =
                        "Precio por noche: ${priceRange.start.toInt()} € – ${priceRange.endInclusive.toInt()} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                RangeSlider(
                    value = priceRange,
                    onValueChange = { bookingVm.setPriceRange(it) },
                    valueRange = 20f..400f,
                    steps = 37,
                )
            }
        }

        Button(
            onClick = {
                if (bookingVm.buildCriteriaOrError() != null) {
                    navController.navigate(Routes.SearchResults.route)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Buscar habitaciones")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.Add.route) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reservar con el asistente clásico")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.RoomList.route) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Catálogo de habitaciones (filtros clásicos)")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
