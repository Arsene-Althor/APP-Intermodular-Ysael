package com.example.hotel_pere_maria_app.ui.Views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.hotel_pere_maria_app.ui.Service.InvoicePdfHelper
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var pendingNoInvoice by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    /** Fallo al cargar solo GET /invoices (no bloquea la sección de reservas sin facturar). */
    var invoiceFetchWarning by remember { mutableStateOf<String?>(null) }
    var pdfBusyId by remember { mutableStateOf<String?>(null) }
    var receiptPdfBusyId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        invoiceFetchWarning = null
        val uid = SessionManager.userInfo?.user_id
        if (uid.isNullOrBlank()) {
            error = "No hay usuario en sesión"
            loading = false
            return@LaunchedEffect
        }
        try {
            val inv = RetrofitClient.reservationService.getInvoicesByUser(uid)
            val mine = RetrofitClient.reservationService.getMine()
            val invErr = inv.errorBody()?.string().orEmpty()
            val mineErr = mine.errorBody()?.string().orEmpty()
            if (SessionManager.shouldLogoutForApiError(inv.code(), invErr) ||
                SessionManager.shouldLogoutForApiError(mine.code(), mineErr)
            ) {
                SessionManager.handleUnauthorized()
                return@LaunchedEffect
            }
            if (inv.isSuccessful) {
                items = inv.body()?.reservations.orEmpty()
            } else {
                items = emptyList()
                invoiceFetchWarning =
                    try {
                        JSONObject(invErr).optString("error", invErr).ifBlank { "Error ${inv.code()}" }
                    } catch (_: Exception) {
                        invErr.ifBlank { "Error ${inv.code()}" }
                    }
            }
            if (mine.isSuccessful) {
                val all = mine.body().orEmpty()
                pendingNoInvoice =
                    all.filter { r ->
                        r.cancelation_date == null &&
                            (r.invoice_number == null || r.invoice_number.isBlank())
                    }
            } else {
                pendingNoInvoice = emptyList()
                val w =
                    try {
                        JSONObject(mineErr).optString("error", mineErr).ifBlank { "Error ${mine.code()}" }
                    } catch (_: Exception) {
                        mineErr.ifBlank { "Error ${mine.code()}" }
                    }
                invoiceFetchWarning =
                    listOfNotNull(invoiceFetchWarning, "No se pudieron cargar tus reservas: $w")
                        .joinToString("\n")
            }
        } catch (e: Exception) {
            error = e.message ?: "Error de red"
        } finally {
            loading = false
        }
    }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis facturas") },
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
        when {
            loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        invoiceFetchWarning?.let { w ->
                            Text(
                                text = w,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        Text(
                            text =
                                "Facturas emitidas (tras checkout en recepción). «Ver PDF» descarga y abre el documento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                    if (items.isEmpty()) {
                        item {
                            Text("No hay facturas emitidas todavía para tu cuenta.")
                        }
                    } else {
                        items(items, key = { it.reservation_id }) { r ->
                            InvoiceHistoryRow(
                                r = r,
                                fmt = fmt,
                                pdfBusy = pdfBusyId == r.reservation_id,
                                onPdf = {
                                    pdfBusyId = r.reservation_id
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
                                            pdfBusyId = null
                                        }
                                    }
                                },
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Reservas sin factura fiscal aún",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Puedes descargar el justificante de reserva (PDF, no fiscal). La factura con IVA y numeración se emite en recepción al hacer checkout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                        )
                    }
                    if (pendingNoInvoice.isEmpty()) {
                        item {
                            Text(
                                "Ninguna reserva activa pendiente de facturar.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        items(pendingNoInvoice, key = { it.reservation_id }) { r ->
                            PendingInvoiceBookingCard(
                                r = r,
                                fmt = fmt,
                                receiptDownloading = receiptPdfBusyId == r.reservation_id,
                                onDescargarJustificante = {
                                    receiptPdfBusyId = r.reservation_id
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
                                            receiptPdfBusyId = null
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
}

@Composable
private fun PendingInvoiceBookingCard(
    r: Reservation,
    fmt: SimpleDateFormat,
    receiptDownloading: Boolean,
    onDescargarJustificante: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(r.reservation_id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(r.room_id, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${fmt.format(r.check_in)} → ${fmt.format(r.check_out)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Factura fiscal: tras checkout en recepción.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            if (receiptDownloading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("Descargando…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                TextButton(onClick = onDescargarJustificante, modifier = Modifier.fillMaxWidth()) {
                    Text("Descargar justificante (PDF)")
                }
            }
        }
    }
}

@Composable
private fun InvoiceHistoryRow(
    r: Reservation,
    fmt: SimpleDateFormat,
    pdfBusy: Boolean,
    onPdf: () -> Unit,
) {
    val fecha = r.checkout_completed_at ?: r.check_out
    val totalStr = String.format(Locale.getDefault(), "%.2f €", r.price.toDouble())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    r.invoice_number ?: "—",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(r.reservation_id, style = MaterialTheme.typography.bodySmall)
                Text(
                    fmt.format(fecha),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text("Total: $totalStr", fontWeight = FontWeight.SemiBold)
            }
            if (pdfBusy) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
            } else {
                TextButton(onClick = onPdf) {
                    Text("Ver PDF")
                }
            }
        }
    }
}
