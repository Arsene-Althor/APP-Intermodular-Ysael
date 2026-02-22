package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel

/**
 * Pantalla de detalles de una habitación específica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetail(
    navController: NavController,
    roomId: String,
    viewModel: RoomViewModel = viewModel()
) {
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val detailError by viewModel.detailError.collectAsState()

    // Cargar detalles de la habitación cuando se abre la pantalla
    LaunchedEffect(roomId) {
        viewModel.loadRoomDetails(roomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de la Habitación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoadingDetail -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                detailError != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = detailError ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadRoomDetails(roomId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                selectedRoom != null -> {
                    val room = selectedRoom!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Imagen principal
                        AsyncImage(
                            model = room.image,
                            contentDescription = "Imagen de ${room.type}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Contenido de detalles
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Tipo y estado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.type,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (room.isAvailable)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFF44336),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (room.isAvailable) "Disponible" else "Ocupada",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // ID de la habitación
                            Text(
                                text = "ID: ${room.room_id}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Precio destacado
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Precio por noche:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "€${room.price_per_night}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Información adicional
                            InfoSection(title = "Información General") {
                                InfoRow(
                                    icon = Icons.Default.Star,
                                    label = "Valoración",
                                    value = "${room.rate}/5.0",
                                    iconTint = Color(0xFFFFC107)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                InfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Ocupación máxima",
                                    value = "${room.max_occupancy} personas"
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Descripción
                            InfoSection(title = "Descripción") {
                                Text(
                                    text = room.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
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
 * Sección de información con título
 */
@Composable
fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Fila de información con icono
 */
@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
