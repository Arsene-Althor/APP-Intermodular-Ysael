package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel

/**
 * Lista de habitaciones en servicio (fuera de servicio no se muestran al hu?sped).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomList(
    navController: NavController,
    viewModel: RoomViewModel = viewModel(),
) {
    val filteredRooms by viewModel.filteredRooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedAvailability by viewModel.selectedAvailability.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            FilterSection(
                selectedType = selectedType,
                selectedAvailability = selectedAvailability,
                onTypeSelected = { viewModel.setTypeFilter(it) },
                onAvailabilitySelected = { viewModel.setAvailabilityFilter(it) },
                roomTypes = viewModel.getRoomTypes(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    error != null -> {
                        ErrorMessage(
                            message = error ?: "Error desconocido",
                            onRetry = { viewModel.loadRooms() },
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    filteredRooms.isEmpty() -> {
                        Text(
                            text = "No hay habitaciones que coincidan con los filtros.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(filteredRooms, key = { it.room_id }) { room ->
                                RoomCard(
                                    room = room,
                                    onClick = {
                                        navController.navigate(Routes.RoomDetail.createRoute(room.room_id))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedType: String,
    selectedAvailability: Boolean?,
    onTypeSelected: (String) -> Unit,
    onAvailabilitySelected: (Boolean?) -> Unit,
    roomTypes: List<String>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp),
    ) {
        Text(
            text = "Tipo",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(roomTypes) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type) },
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Disponibilidad (solo en servicio)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedAvailability == null,
                onClick = { onAvailabilitySelected(null) },
                label = { Text("Todas") },
            )
            FilterChip(
                selected = selectedAvailability == true,
                onClick = { onAvailabilitySelected(true) },
                label = { Text("Libres ahora") },
            )
            FilterChip(
                selected = selectedAvailability == false,
                onClick = { onAvailabilitySelected(false) },
                label = { Text("Ocupadas ahora") },
            )
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    onClick: () -> Unit,
) {
    val badgeText = if (room.isOccupiedNowEffective()) "Reservada ahora" else "Libre"
    val badgeColor =
        if (room.isOccupiedNowEffective()) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.tertiaryContainer
    val onBadge =
        if (room.isOccupiedNowEffective()) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onTertiaryContainer

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = room.image,
                contentDescription = "Imagen de ${room.type}",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = room.type,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = onBadge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Text(
                    text = "${room.price_per_night} \u20AC / noche",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${room.rate} / 5.0", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "M\u00E1x. ${room.max_occupancy} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
