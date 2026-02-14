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
                value = state.room,
                onValueChange = {
                    newText ->
                    addViewModel.onRoomChanged(newText)
                },
                readOnly = false,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Habitación") },
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.outline_bedroom_parent_24), contentDescription = null) }
            )


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

