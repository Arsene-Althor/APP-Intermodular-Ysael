package com.example.hotel_pere_maria_app.ui.booking

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel
import com.example.hotel_pere_maria_app.ui.Views.FechaInputSimple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun BookingHomeScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    val homeVm: HomeViewModel = viewModel()
    val proxima by homeVm.proximaReserva.collectAsState()
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    var entrada by remember { mutableStateOf(BookingSearchSession.checkInDisplay()) }
    var salida by remember { mutableStateOf(BookingSearchSession.checkOutDisplay()) }
    var guests by remember { mutableIntStateOf(BookingSearchSession.guests) }
    var priceMin by remember { mutableFloatStateOf(BookingSearchSession.priceMin.toFloat()) }
    var priceMax by remember { mutableFloatStateOf(BookingSearchSession.priceMax.toFloat()) }

    LaunchedEffect(Unit) {
        homeVm.limpiarMensaje()
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Encuentra tu habitación",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Elige fechas y ajusta huéspedes y presupuesto.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Próxima reserva",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        ProximaReservaCard(proxima = proxima, formatter = formatter)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Fechas", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FechaInputSimple(
                            label = "Entrada",
                            fecha = entrada,
                            onFechaSelected = { ms ->
                                BookingSearchSession.checkInMillis = ms
                                entrada = ms?.let { formatter.format(Date(it)) } ?: ""
                            },
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FechaInputSimple(
                            label = "Salida",
                            fecha = salida,
                            onFechaSelected = { ms ->
                                BookingSearchSession.checkOutMillis = ms
                                salida = ms?.let { formatter.format(Date(it)) } ?: ""
                            },
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Personas", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { guests = (guests - 1).coerceAtLeast(1) },
                            enabled = guests > 1,
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Una persona menos")
                        }
                        Text(
                            text = "$guests",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        FilledTonalIconButton(
                            onClick = { guests = (guests + 1).coerceAtMost(8) },
                            enabled = guests < 8,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Una persona más")
                        }
                    }
                }

                Text(
                    "Precio / noche: ${priceMin.toInt()} € – ${priceMax.toInt()} €",
                    fontWeight = FontWeight.SemiBold,
                )
                RangeSlider(
                    value = priceMin..priceMax,
                    onValueChange = { r ->
                        var a = r.start
                        var b = r.endInclusive
                        if (b - a < 10f) {
                            if (a + 10f <= 500f) b = a + 10f
                            else a = (b - 10f).coerceAtLeast(20f)
                        }
                        priceMin = a.coerceIn(20f, 490f)
                        priceMax = b.coerceIn(30f, 500f).coerceAtLeast(priceMin + 10f)
                    },
                    valueRange = 20f..500f,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                )
            }
        }

        Button(
            onClick = {
                if (!BookingSearchSession.isComplete()) {
                    scope.launch { snackbarHostState.showSnackbar("Selecciona entrada y salida válidas.") }
                    return@Button
                }
                BookingSearchSession.guests = guests
                BookingSearchSession.priceMin = priceMin.toDouble()
                BookingSearchSession.priceMax = priceMax.toDouble()
                navController.navigate(Routes.BookingResults.route) { launchSingleTop = true }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Buscar habitaciones", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProximaReservaCard(proxima: Reservation?, formatter: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                if (proxima == null) {
                    Text(
                        "No tienes reservas próximas. Busca fechas y reserva en un clic.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        "Check-in: ${formatter.format(proxima.check_in)} · Check-out: ${formatter.format(proxima.check_out)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Habitación: ${proxima.room_id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
