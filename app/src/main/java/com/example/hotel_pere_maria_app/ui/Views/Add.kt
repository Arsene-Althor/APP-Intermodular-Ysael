package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.ViewModels.AddViewModel
import com.example.ui.theme.AppTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.hotel_pere_maria_app.ui.ViewModels.AdduiState
import com.example.hotel_pere_maria_app.ui.Components.RoomSelectionDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(snackbarHostState : SnackbarHostState) {
    val addViewModel: AddViewModel = viewModel()
    val state by addViewModel.uiState.collectAsState()

    LaunchedEffect(state.mensajeRespuesta) {
        state.mensajeRespuesta?.let {
            snackbarHostState.showSnackbar(it)
            addViewModel.limpiarMensaje()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Nueva reserva!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Completa los datos para tu estancia en el Hotel Pere Maria.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Fechas de estancia",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FechaInputSimple(label = "Entrada", fecha = state.check_in, onFechaSelected = {
                        millis ->
                        addViewModel.onDateSelected(millis, true)
                    })
                }
                Box(modifier = Modifier.weight(1f)) {
                    FechaInputSimple(label = "Salida", fecha = state.check_out, onFechaSelected = {
                        millis ->
                        addViewModel.onDateSelected(millis, false)
                    })
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Selecciona tu habitación",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.selectedRoom?.type ?: "Selecciona una habitación",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = state.canSelectRoom) { 
                        if(state.canSelectRoom) {
                            addViewModel.onShowRoomDialog(true) 
                        }
                    },
                label = { Text("Habitación") },
                leadingIcon = { 
                    Icon(
                        painter = painterResource(id = R.drawable.outline_bedroom_parent_24), 
                        contentDescription = null
                    ) 
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Seleccionar"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = if(state.canSelectRoom) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = false
            )
            
            // Mensaje de ayuda cuando no se pueden seleccionar habitaciones
            if(!state.canSelectRoom) {
                Text(
                    text = "⚠️ Selecciona las fechas de check-in y check-out primero",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Vista previa de la habitación seleccionada
            state.selectedRoom?.let { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Imagen
                        AsyncImage(
                            model = room.image,
                            contentDescription = "Imagen de ${room.type}",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Información
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = room.type,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = room.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${room.rate}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Máx. ${room.max_occupancy}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total estimado", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${state.price} €",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = { addViewModel.onShowResumen(true)},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = state.check_in.isNotEmpty() && state.check_out.isNotEmpty() && state.room.isNotEmpty()
        ) {
            Text(text = "CONTINUAR AL PAGO", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if(state.showResumen){
            SummaryBottomSheet(
                state = state,
                onDismiss = { addViewModel.onShowResumen(false) },
                onConfirm = {
                    addViewModel.onShowResumen(false)
                    addViewModel.realizarReserva()
                }
            )
        }

        // Diálogo de selección de habitación
        if(state.showRoomDialog) {
            RoomSelectionDialog(
                onDismiss = { addViewModel.onShowRoomDialog(false) },
                onRoomSelected = { room ->
                    addViewModel.onRoomSelected(room)
                },
                selectedRoomId = state.room,
                showOnlyAvailable = true,
                checkInDate = state.check_in,
                checkOutDate = state.check_out
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(state: AdduiState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Confirmar Reserva",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            ResumenRow(label = "Habitación:", value = state.room)
            ResumenRow(label = "Check-in:", value = state.check_in)
            ResumenRow(label = "Check-out:", value = state.check_out)

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total a pagar:", style = MaterialTheme.typography.titleMedium)
                Text("${String.format("%.2f", state.price)}€",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PAGAR AHORA")
            }
        }
    }
}

@Composable
fun ResumenRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

