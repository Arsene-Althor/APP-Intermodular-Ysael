package com.example.hotel_pere_maria_app.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.ExtraService
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Models.RoomRepository
import com.example.hotel_pere_maria_app.ui.Navegation.Routes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingResultsScreen(navController: NavHostController) {
    val raw by RoomRepository.availableRooms.collectAsState()
    val loading by RoomRepository.availableLoading.collectAsState()
    val err by RoomRepository.availableError.collectAsState()

    var sortHighFirst by remember { mutableStateOf(true) }
    val serviceSelection = remember { mutableStateMapOf<String, Boolean>() }
    var catalog by remember { mutableStateOf<List<ExtraService>>(emptyList()) }

    val sessionGuests = BookingSearchSession.guests
    val sessionPriceMin = BookingSearchSession.priceMin
    val sessionPriceMax = BookingSearchSession.priceMax

    LaunchedEffect(Unit) {
        catalog = RoomRepository.fetchExtraServices()
    }

    LaunchedEffect(
        BookingSearchSession.checkInMillis,
        BookingSearchSession.checkOutMillis,
        sessionGuests,
    ) {
        val ci = BookingSearchSession.checkInDisplay()
        val co = BookingSearchSession.checkOutDisplay()
        if (ci.isNotBlank() && co.isNotBlank()) {
            RoomRepository.fetchAvailableRoomsByDates(ci, co, sessionGuests)
        }
    }

    val selectedIds = serviceSelection.filterValues { it }.keys
    val filtered =
        raw
            .filter { r ->
                val p = r.displayPricePerNight()
                p >= sessionPriceMin &&
                    p <= sessionPriceMax &&
                    selectedIds.all { id -> r.extraServices.contains(id) }
            }
            .sortedBy { if (sortHighFirst) -it.rate else it.rate }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Resultados",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Text("Ordenar por", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            FilterChip(
                selected = sortHighFirst,
                onClick = { sortHighFirst = true },
                label = { Text("Valoración: mayor primero") },
            )
            FilterChip(
                selected = !sortHighFirst,
                onClick = { sortHighFirst = false },
                label = { Text("Valoración: menor primero") },
            )
        }

        Text(
            "Servicios extra",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (catalog.isEmpty()) {
                "No hay servicios en catálogo (empleado: créalos en WPF). Luego podrás filtrar aquí."
            } else {
                "La habitación debe incluir todos los servicios seleccionados."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            catalog.forEach { svc ->
                val on = serviceSelection[svc.serviceId] == true
                FilterChip(
                    selected = on,
                    onClick = { serviceSelection[svc.serviceId] = !on },
                    label = {
                        Text(svc.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                )
            }
        }

        when {
            loading ->
                CircularProgressIndicator(
                    Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                )

            err != null ->
                Text(err ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))

            filtered.isEmpty() ->
                Text(
                    "No hay habitaciones que coincidan. Prueba precio, servicios o fechas.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )

            else ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.room_id }) { room ->
                        ResultRoomCard(room = room) {
                            navController.navigate(Routes.RoomDetail.createRoute(room.room_id))
                        }
                    }
                }
        }
    }
}

@Composable
private fun ResultRoomCard(room: Room, onClick: () -> Unit) {
    val thumb = room.galleryImageUrls().firstOrNull() ?: room.image
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = thumb,
                contentDescription = null,
                modifier = Modifier.height(100.dp).weight(0.38f),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(0.62f)) {
                Text(room.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(room.room_id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.height(16.dp))
                    Text("${room.rate}", style = MaterialTheme.typography.bodySmall)
                    Text("· Máx ${room.max_occupancy} pers.", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${room.displayPricePerNight()} € / noche",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (room.offerActive && room.offerPercent > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "-${room.offerPercent.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    room.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
