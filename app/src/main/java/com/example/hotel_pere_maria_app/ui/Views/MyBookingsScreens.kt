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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.Service.InvoicePdfHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private fun Reservation.tieneFactura(): Boolean = !invoice_number.isNullOrBlank()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val reservas by ReservationRepository.reservations.collectAsState()
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pdfInvoiceBusyFor by remember { mutableStateOf<String?>(null) }
    var pdfReceiptBusyFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ReservationRepository.fetchReservations()
    }

    val activas =
        reservas.filter { it.cancelation_date == null }.sortedBy { it.check_in }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
                actions = {
                    TextButton(
                        onClick = { navController.navigate(Routes.InvoiceHistory.route) },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Mis facturas")
                        }
                    }
                    TextButton(
                        onClick = { navController.navigate(Routes.ReservationHistory.route) },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Historial")
                        }
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
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Reservas activas (no canceladas). Pulsa una para modificar o cancelar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            if (activas.isEmpty()) {
                Text(
                    text = "No tienes reservas activas. Explora habitaciones y crea una nueva.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(activas, key = { it.reservation_id }) { r ->
                        ActiveBookingCard(
                            reserva = r,
                            fmt = fmt,
                            invoiceDownloading = pdfInvoiceBusyFor == r.reservation_id,
                            receiptDownloading = pdfReceiptBusyFor == r.reservation_id,
                            onOpen = {
                                navController.navigate("${Routes.ModReserva.route}/${r.reservation_id}")
                            },
                            onHistorial = {
                                navController.navigate(Routes.ReservationAudit.createRoute(r.reservation_id))
                            },
                            onDescargarJustificante = {
                                pdfReceiptBusyFor = r.reservation_id
                                scope.launch {
                                    try {
                                        when (
                                            val res =
                                                InvoicePdfHelper.downloadAndOpenBookingReceipt(context, r.reservation_id)
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
                                        pdfReceiptBusyFor = null
                                    }
                                }
                            },
                            onDescargarFactura = {
                                pdfInvoiceBusyFor = r.reservation_id
                                scope.launch {
                                    try {
                                        when (
                                            val res =
                                                InvoicePdfHelper.downloadAndOpenPdf(context, r.reservation_id)
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
                                        pdfInvoiceBusyFor = null
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBookingCard(
    reserva: Reservation,
    fmt: SimpleDateFormat,
    invoiceDownloading: Boolean,
    receiptDownloading: Boolean,
    onOpen: () -> Unit,
    onHistorial: () -> Unit,
    onDescargarJustificante: () -> Unit,
    onDescargarFactura: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = reserva.reservation_id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = onHistorial) { Text("Actividad") }
            }
            Text(text = reserva.room_id, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text =
                    "${fmt.format(reserva.check_in)} → ${fmt.format(reserva.check_out)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Justificante: PDF no fiscal (pago simulado en la app).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (receiptDownloading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("Descargando justificante…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                TextButton(onClick = onDescargarJustificante, modifier = Modifier.fillMaxWidth()) {
                    Text("Descargar justificante (PDF)")
                }
            }
            if (reserva.tieneFactura()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Factura fiscal: ${reserva.invoice_number}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (invoiceDownloading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Text("Descargando factura…", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    TextButton(onClick = onDescargarFactura, modifier = Modifier.fillMaxWidth()) {
                        Text("Descargar factura fiscal (PDF)")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onOpen, modifier = Modifier.align(Alignment.End)) {
                Text("Gestionar reserva")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationHistoryScreen(navController: NavController) {
    val reservas by ReservationRepository.reservations.collectAsState()
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pdfInvoiceBusyFor by remember { mutableStateOf<String?>(null) }
    var pdfReceiptBusyFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ReservationRepository.fetchReservations()
    }

    val ordenadas = reservas.sortedByDescending { it.check_in }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de reservas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate(Routes.InvoiceHistory.route) }) {
                        Text("Mis facturas")
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Todas tus reservas. Justificante (pago simulado) en PDF siempre; factura fiscal solo tras checkout en recepción.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            if (ordenadas.isEmpty()) {
                item {
                    Text("Aún no hay reservas en tu cuenta.", modifier = Modifier.padding(top = 24.dp))
                }
            } else {
                items(ordenadas, key = { it.reservation_id }) { r ->
                    HistoryBookingRow(
                        reserva = r,
                        fmt = fmt,
                        navController = navController,
                        invoiceDownloading = pdfInvoiceBusyFor == r.reservation_id,
                        receiptDownloading = pdfReceiptBusyFor == r.reservation_id,
                        onDescargarJustificante = {
                            pdfReceiptBusyFor = r.reservation_id
                            scope.launch {
                                try {
                                    when (
                                        val res =
                                            InvoicePdfHelper.downloadAndOpenBookingReceipt(context, r.reservation_id)
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
                                    pdfReceiptBusyFor = null
                                }
                            }
                        },
                        onDescargarFactura = {
                            pdfInvoiceBusyFor = r.reservation_id
                            scope.launch {
                                try {
                                    when (
                                        val res =
                                            InvoicePdfHelper.downloadAndOpenPdf(context, r.reservation_id)
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
                                    pdfInvoiceBusyFor = null
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryBookingRow(
    reserva: Reservation,
    fmt: SimpleDateFormat,
    navController: NavController,
    invoiceDownloading: Boolean,
    receiptDownloading: Boolean,
    onDescargarJustificante: () -> Unit,
    onDescargarFactura: () -> Unit,
) {
    val estado =
        when {
            reserva.cancelation_date != null -> "Cancelada"
            reserva.check_out.before(Date()) -> "Finalizada"
            else -> "Activa"
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reserva.reservation_id, fontWeight = FontWeight.SemiBold)
                    Text(reserva.room_id, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${fmt.format(reserva.check_in)} → ${fmt.format(reserva.check_out)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(estado, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                TextButton(
                    onClick = { navController.navigate(Routes.ReservationAudit.createRoute(reserva.reservation_id)) },
                ) {
                    Text("Actividad")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (receiptDownloading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Justificante…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                TextButton(onClick = onDescargarJustificante) {
                    Text("Justificante reserva (PDF)")
                }
            }
            if (reserva.tieneFactura()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (invoiceDownloading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Factura…", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    TextButton(onClick = onDescargarFactura) {
                        Text("Factura fiscal (PDF)")
                    }
                }
            }
        }
    }
}
