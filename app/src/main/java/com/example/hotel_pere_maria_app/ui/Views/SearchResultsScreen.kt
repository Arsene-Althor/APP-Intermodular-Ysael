package com.example.hotel_pere_maria_app.ui.Views

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomSortOption
import com.example.hotel_pere_maria_app.ui.ViewModels.SearchResultsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchResultsScreen(
    navController: NavHostController,
    vm: SearchResultsViewModel = viewModel(),
) {
    val rooms by vm.rooms.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val sortBy by vm.sortBy.collectAsState()
    val selected by vm.selectedAmenities.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Resultados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        when {
            loading ->
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
                )
            error != null ->
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp),
                )
            else -> {
                Text(
                    text = "Ordenar por",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(RoomSortOption.entries.toList()) { opt ->
                        FilterChip(
                            selected = sortBy == opt,
                            onClick = { vm.setSort(opt) },
                            label = {
                                Text(
                                    text = opt.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                )
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Servicios extra (próxima versión)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    vm.extraAmenityOptions.forEach { label ->
                        FilterChip(
                            selected = selected.contains(label),
                            onClick = { vm.toggleExtraAmenity(label) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (rooms.isEmpty()) {
                    Text(
                        text = "No hay habitaciones que coincidan. Prueba a ampliar el rango de precio o las fechas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(rooms, key = { it.room_id }) { room ->
                            SearchResultRoomCard(room = room) {
                                navController.navigate(Routes.RoomDetail.createRoute(room.room_id))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRoomCard(room: Room, onClick: () -> Unit) {
    val thumb = room.galleryImageUrls().firstOrNull() ?: room.image
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = thumb,
                contentDescription = room.type,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = room.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = room.room_id,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = String.format("%.1f", room.rate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "${room.price_per_night.toInt()} € / noche",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = room.description,
                maxLines = 2,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
