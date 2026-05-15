package com.example.hotel_pere_maria_app.ui.Views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hotel_pere_maria_app.ui.Service.InvoicePdfHelper
import com.example.hotel_pere_maria_app.ui.ViewModels.ReservationAuditViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAuditScreen(
    navController: NavController,
    vm: ReservationAuditViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var receiptBusy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Actividad de la reserva", style = MaterialTheme.typography.titleMedium)
                        Text(
                            state.reservationId,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Mensajes que ves como huésped (sin detalles técnicos).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Justificante de reserva (PDF, no fiscal): puedes descargarlo en cualquier momento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (receiptBusy) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("Descargando…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                TextButton(
                    onClick = {
                        receiptBusy = true
                        scope.launch {
                            try {
                                when (
                                    val res =
                                        InvoicePdfHelper.downloadAndOpenBookingReceipt(context, state.reservationId)
                                ) {
                                    is InvoicePdfHelper.Result.Error ->
                                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()

                                    InvoicePdfHelper.Result.NoPdfViewer ->
                                        Toast.makeText(
                                            context,
                                            "No hay visor de PDF instalado",
                                            Toast.LENGTH_LONG,
                                        ).show()

                                    InvoicePdfHelper.Result.Ok -> {}
                                }
                            } finally {
                                receiptBusy = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Descargar justificante (PDF)")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HistorialReservaSection(
                cargando = state.historialCargando,
                items = state.historialItems,
            )
        }
    }
}
