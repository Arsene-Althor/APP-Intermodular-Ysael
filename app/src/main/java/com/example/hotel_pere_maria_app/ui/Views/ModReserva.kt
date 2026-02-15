package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.ViewModels.ModReservaViewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.ModuiState
import com.example.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModReserva(snackbarHostState : SnackbarHostState, reservaId: String, onBack: () -> Unit) {
    val ModViewModel: ModReservaViewModel = viewModel()
    val state by ModViewModel.uiState.collectAsState()

    LaunchedEffect(state.mensajeRespuesta) {
        state.mensajeRespuesta?.let {
            snackbarHostState.showSnackbar(it)
            ModViewModel.limpiarMensaje()
        }
    }
    LaunchedEffect(key1 = reservaId) {
        ModViewModel.cargarDatos()
    }
    LaunchedEffect(Unit) {
        ModViewModel.navigationEvent.collect {
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant) ) {
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(color = MaterialTheme.colorScheme.surface)
                ,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.reservation_id,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Completa los datos para modificar tu estancia en el Hotel Pere Maria.",
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FechaInputSimple(label = "Entrada", fecha = state.check_in, onFechaSelected = {
                                    millis ->
                                ModViewModel.onDateSelected(millis, true)
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FechaInputSimple(label = "Salida", fecha = state.check_out, onFechaSelected = {
                                    millis ->
                                ModViewModel.onDateSelected(millis, false)
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
                            ModViewModel.onRoomChanged(newText)
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column() {
                            Text(text = "Total anterior", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = "${state.priceActual} €",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total modificado", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = "${state.priceNuevo} €",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Button(
                    onClick = { ModViewModel.onShowResumenMod(true)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.check_in.isNotEmpty() && state.check_out.isNotEmpty() && state.room.isNotEmpty()
                ) {
                    Text(text = "CONTINUAR AL PAGO", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { ModViewModel.onShowResumenCancel(true)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "CANCELAR RESERVA", fontWeight = FontWeight.Bold)
                }

                if(state.showResumenMod){
                    SummaryBottomConfirmar(
                        state = state,
                        onDismiss = { ModViewModel.onShowResumenMod(false) },
                        onConfirm = {
                            ModViewModel.onShowResumenMod(false)
                            ModViewModel.modificarReserva()
                        }
                    )
                }
                if(state.showResumenCancel){
                    SummaryBottomCancel(
                        state = state,
                        onDismiss = { ModViewModel.onShowResumenCancel(false) },
                        onConfirm = {
                            ModViewModel.onShowResumenMod(false)
                            ModViewModel.cancelarReserva()
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomConfirmar(state: ModuiState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
                Text("${String.format("%.2f", state.priceNuevo-state.priceActual)}€",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomCancel(state: ModuiState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
            Text("Confirmar Cancelación",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            ResumenRow(label = "Habitación:", value = state.room)
            ResumenRow(label = "Check-in:", value = state.check_in)
            ResumenRow(label = "Check-out:", value = state.check_out)

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Devolución: :", style = MaterialTheme.typography.titleMedium)
                Text("${String.format("%.2f", state.priceActual - state.precioCancel)}€",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Cancelar ahora")
            }
        }
    }
}
