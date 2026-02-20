package com.example.hotel_pere_maria_app.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel

/**
 * Diálogo modal para seleccionar una habitación visualmente
 * Muestra todas las habitaciones disponibles con imagen, tipo y descripción
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSelectionDialog(
    onDismiss: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    selectedRoomId: String? = null,
    showOnlyAvailable: Boolean = true,
    checkInDate: String? = null,
    checkOutDate: String? = null,
    viewModel: RoomViewModel = viewModel()
) {
    // Cuando hay fechas, usar la lista de disponibles (del endpoint room/available con fallback)
    // Cuando no hay fechas, usar la lista filtrada de todas las habitaciones
    val hasDates = !checkInDate.isNullOrEmpty() && !checkOutDate.isNullOrEmpty()

    val allRooms by viewModel.filteredRooms.collectAsState()
    val availableByDates by viewModel.availableRooms.collectAsState()

    // Usar estados independientes para el diálogo (no contamina RoomList)
    val isLoadingAll by viewModel.isLoading.collectAsState()
    val isLoadingAvail by viewModel.availableLoading.collectAsState()
    val errorAll by viewModel.error.collectAsState()
    val errorAvail by viewModel.availableError.collectAsState()

    val isLoading = if (hasDates) isLoadingAvail else isLoadingAll
    val error = if (hasDates) errorAvail else errorAll

    // Cargar habitaciones al abrir el diálogo
    LaunchedEffect(checkInDate, checkOutDate) {
        if (hasDates) {
            viewModel.loadAvailableRoomsByDates(checkInDate!!, checkOutDate!!)
        } else {
            viewModel.loadRooms()
        }
    }

    // Lista final: si hay fechas el repositorio ya filtra/hace fallback por disponibilidad
    val displayRooms = if (hasDates) availableByDates else {
        if (showOnlyAvailable) allRooms.filter { it.isAvailable } else allRooms
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecciona una habitación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                HorizontalDivider()

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        error != null -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = error ?: "Error desconocido",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadRooms() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                        displayRooms.isEmpty() -> {
                            Text(
                                text = if (showOnlyAvailable) 
                                    "No hay habitaciones disponibles" 
                                else 
                                    "No se encontraron habitaciones",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(displayRooms) { room ->
                                    RoomCardCompact(
                                        room = room,
                                        isSelected = room.room_id == selectedRoomId,
                                        onClick = {
                                            onRoomSelected(room)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta compacta de habitación para usar en el diálogo de selección
 * Versión más pequeña del RoomCard con indicador de selección
 */
@Composable
fun RoomCardCompact(
    room: Room,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Imagen de la habitación
            Box {
                AsyncImage(
                    model = room.image,
                    contentDescription = "Imagen de ${room.type}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Indicador de selección
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Seleccionada",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la habitación
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tipo
                Text(
                    text = room.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción truncada
                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio y detalles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Precio
                    Text(
                        text = "€${room.price_per_night}/noche",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Valoración y ocupación
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Valoración
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Valoración",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${room.rate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Ocupación
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ocupación",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${room.max_occupancy}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
