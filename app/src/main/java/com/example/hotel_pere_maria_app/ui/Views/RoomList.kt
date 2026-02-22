package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
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
 * Pantalla principal que muestra la lista de habitaciones con filtros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomList(
    navController: NavController,
    viewModel: RoomViewModel = viewModel()
) {
    val filteredRooms by viewModel.filteredRooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedAvailability by viewModel.selectedAvailability.collectAsState()

    // Cargar habitaciones cuando se abre la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            FilterSection(
                selectedType = selectedType,
                selectedAvailability = selectedAvailability,
                onTypeSelected = { viewModel.setTypeFilter(it) },
                onAvailabilitySelected = { viewModel.setAvailabilityFilter(it) },
                roomTypes = viewModel.getRoomTypes()
            )

            // Contenido principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        ErrorMessage(
                            message = error ?: "Error desconocido",
                            onRetry = { viewModel.loadRooms() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    filteredRooms.isEmpty() -> {
                        Text(
                            text = "No se encontraron habitaciones",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRooms) { room ->
                                RoomCard(
                                    room = room,
                                    onClick = {
                                        navController.navigate(
                                            Routes.RoomDetail.createRoute(room.room_id)
                                        )
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

/**
 * Sección de filtros con chips para tipo y disponibilidad
 */
@Composable
fun FilterSection(
    selectedType: String,
    selectedAvailability: Boolean?,
    onTypeSelected: (String) -> Unit,
    onAvailabilitySelected: (Boolean?) -> Unit,
    roomTypes: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Filtrar por tipo:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filtros de tipo
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roomTypes) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Filtrar por disponibilidad:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filtros de disponibilidad
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedAvailability == null,
                onClick = { onAvailabilitySelected(null) },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = selectedAvailability == true,
                onClick = { onAvailabilitySelected(true) },
                label = { Text("Disponibles") }
            )
            FilterChip(
                selected = selectedAvailability == false,
                onClick = { onAvailabilitySelected(false) },
                label = { Text("No disponibles") }
            )
        }
    }
}

/**
 * Tarjeta individual de habitación
 */
@Composable
fun RoomCard(
    room: Room,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Imagen de la habitación
            AsyncImage(
                model = room.image,
                contentDescription = "Imagen de ${room.type}",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la habitación
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tipo y disponibilidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Badge de disponibilidad
                    Surface(
                        color = if (room.isAvailable) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFF44336),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (room.isAvailable) "Disponible" else "Ocupada",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Precio
                Text(
                    text = "€${room.price_per_night}/noche",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Valoración
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Valoración",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.rate}/5.0",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Ocupación máxima
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ocupación",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Máx. ${room.max_occupancy} personas",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar mensajes de error
 */
@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
