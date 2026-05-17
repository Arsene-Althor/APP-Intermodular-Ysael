package com.example.hotel_pere_maria_app.feature.invoice

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.hotel_pere_maria_app.data.model.HotelInvoiceItem
import com.example.hotel_pere_maria_app.data.repository.ReservationRepository
import com.example.hotel_pere_maria_app.core.util.InvoicePdfHelper
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.core.util.parseApiError
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<HotelInvoiceItem>>(emptyList()) }
    var pdfBusyId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        val uid = SessionManager.userInfo?.user_id
        if (uid.isNullOrBlank()) {
            error = "No hay usuario en sesión"
            loading = false
            return@LaunchedEffect
        }
        try {
            ReservationRepository.fetchReservations()
            val activas =
                ReservationRepository.reservations.value.filter { it.cancelation_date == null }
            for (r in activas) {
                if (r.invoice_number.isNullOrBlank()) {
                    try {
                        RetrofitClient.reservationService.confirmPayment(
                            r.reservation_id,
                            mapOf("amount" to r.price.toString()),
                        )
                    } catch (_: Exception) {
                        // ignorar; se reintenta al recargar
                    }
                }
            }
            ReservationRepository.fetchReservations()

            val inv = RetrofitClient.reservationService.getInvoicesByUser(uid)
            val invErr = inv.errorBody()?.string().orEmpty()
            if (SessionManager.shouldLogoutForApiError(inv.code(), invErr)) {
                SessionManager.handleUnauthorized()
                return@LaunchedEffect
            }
            if (inv.isSuccessful) {
                items = inv.body()?.invoices.orEmpty()
            } else {
                error = parseApiError(invErr)
            }
        } catch (e: Exception) {
            error = e.message ?: "Error de red"
        } finally {
            loading = false
        }
    }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Aún no tienes facturas. Aparecerán al reservar y pagar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                    items(items, key = { it.invoice_number }) { inv ->
                        InvoiceHistoryRow(
                            inv = inv,
                            fmt = fmt,
                            pdfBusy = pdfBusyId == inv.invoice_number,
                            onPdf = {
                                pdfBusyId = inv.invoice_number
                                scope.launch {
                                    try {
                                        when (
                                            val res =
                                                InvoicePdfHelper.downloadAndOpenPdf(
                                                    context,
                                                    inv.reservation_id,
                                                    inv.invoice_number,
                                                )
                                        ) {
                                            is InvoicePdfHelper.Result.Error ->
                                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                            InvoicePdfHelper.Result.NoPdfViewer ->
                                                Toast.makeText(context, "Instala un visor de PDF", Toast.LENGTH_LONG).show()
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
            }
        }
    }
}

@Composable
private fun InvoiceHistoryRow(
    inv: HotelInvoiceItem,
    fmt: SimpleDateFormat,
    pdfBusy: Boolean,
    onPdf: () -> Unit,
) {
    val fecha = inv.issued_at ?: inv.check_out
    val totalStr = String.format(Locale.getDefault(), "%.2f €", inv.amount.toDouble())
    val tipo = inv.type_label ?: inv.type ?: "Factura"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(inv.invoice_number, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(tipo, style = MaterialTheme.typography.labelMedium)
                Text(inv.reservation_id, style = MaterialTheme.typography.bodySmall)
                fecha?.let {
                    Text(
                        fmt.format(it),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text("Total: $totalStr", fontWeight = FontWeight.SemiBold)
            }
            if (pdfBusy) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
            } else {
                TextButton(onClick = onPdf) {
                    Text("PDF")
                }
            }
        }
    }
}

