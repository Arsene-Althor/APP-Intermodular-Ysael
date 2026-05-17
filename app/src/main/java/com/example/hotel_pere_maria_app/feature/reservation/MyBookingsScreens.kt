package com.example.hotel_pere_maria_app.feature.reservation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hotel_pere_maria_app.data.model.LoyaltyFeedback
import com.example.hotel_pere_maria_app.data.model.FlexibilityKind
import com.example.hotel_pere_maria_app.data.repository.FlexibilityRepository
import com.example.hotel_pere_maria_app.data.model.Reservation
import com.example.hotel_pere_maria_app.data.repository.ReservationRepository
import com.example.hotel_pere_maria_app.data.model.FlexibilityStatusResponse
import com.example.hotel_pere_maria_app.data.model.feeQuote
import com.example.hotel_pere_maria_app.data.model.formatFeePreviewLine
import com.example.hotel_pere_maria_app.data.repository.isActiveForClient
import com.example.hotel_pere_maria_app.data.repository.needsEndOfStayChoice
import com.example.hotel_pere_maria_app.feature.flexibility.EndOfStayDecisionDialog
import com.example.hotel_pere_maria_app.feature.flexibility.ExtendStayDateDialog
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityEarlyCheckInButton
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityPollWorker
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityRequestDialog
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityStatusSection
import com.example.hotel_pere_maria_app.feature.flexibility.SimulatedPaymentBottomSheet
import com.example.hotel_pere_maria_app.core.navigation.Routes
import com.example.hotel_pere_maria_app.core.util.InvoicePdfHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private fun Reservation.tieneFactura(): Boolean = !invoice_number.isNullOrBlank()

private data class FlexPaymentPending(
    val reserva: Reservation,
    val kind: FlexibilityKind,
    val hour: Int,
    val minute: Int,
    val amountEur: Double?,
)

private data class ExtendPaymentPending(
    val reserva: Reservation,
    val newCheckOutIso: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val reservas by ReservationRepository.reservations.collectAsState()
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pdfInvoiceBusyFor by remember { mutableStateOf<String?>(null) }
    var flexDialog by remember { mutableStateOf<Pair<Reservation, FlexibilityKind>?>(null) }
    var flexSubmitBusy by remember { mutableStateOf(false) }
    var extendDialog by remember { mutableStateOf<Reservation?>(null) }
    var extendBusy by remember { mutableStateOf(false) }
    var flexPreview by remember { mutableStateOf<FlexibilityStatusResponse?>(null) }
    var flexPreviewLoading by remember { mutableStateOf(false) }
    var flexPayment by remember { mutableStateOf<FlexPaymentPending?>(null) }
    var extendPayment by remember { mutableStateOf<ExtendPaymentPending?>(null) }
    var endOfStayFor by remember { mutableStateOf<Reservation?>(null) }
    val endOfStayDismissed = remember { mutableStateOf(setOf<String>()) }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        FlexibilityPollWorker.schedule(context)
        ReservationRepository.fetchReservations()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        ReservationRepository.fetchReservations()
                        FlexibilityPollWorker.runOnce(context)
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(reservas) {
        if (reservas.isNotEmpty()) {
            com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityNotificationHelper.checkStatusChanges(
                context,
                reservas,
            )
        }
    }

    val activas =
        reservas.filter { it.isActiveForClient() }.sortedBy { it.check_in }

    LaunchedEffect(activas.map { it.reservation_id to it.check_out.time }) {
        val candidate =
            activas.firstOrNull { r ->
                r.needsEndOfStayChoice() && r.reservation_id !in endOfStayDismissed.value
            }
        if (candidate != null && endOfStayFor == null) {
            endOfStayFor = candidate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
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
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { navController.navigate(Routes.InvoiceHistory.route) },
                    label = { Text("Facturas") },
                )
                AssistChip(
                    onClick = { navController.navigate(Routes.MyStays.route) },
                    label = { Text("Estancias") },
                )
                AssistChip(
                    onClick = { navController.navigate(Routes.ReservationHistory.route) },
                    label = { Text("Todas") },
                )
            }
            Text(
                text = "Toca una reserva para gestionarla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
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
                            onOpen = {
                                navController.navigate("${Routes.ModReserva.route}/${r.reservation_id}")
                            },
                            onHistorial = {
                                navController.navigate(Routes.ReservationAudit.createRoute(r.reservation_id))
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
                                                    "Instala un visor de PDF",
                                                    Toast.LENGTH_LONG,
                                                ).show()

                                            InvoicePdfHelper.Result.Ok -> {}
                                        }
                                    } finally {
                                        pdfInvoiceBusyFor = null
                                    }
                                }
                            },
                            onVerFacturas = { navController.navigate(Routes.InvoiceHistory.route) },
                            onRequestEarly = { flexDialog = r to FlexibilityKind.EARLY },
                            onChooseEndOfStay = {
                                endOfStayDismissed.value =
                                    endOfStayDismissed.value - r.reservation_id
                                endOfStayFor = null
                                endOfStayFor = r
                            },
                        )
                    }
                }
            }
        }
    }

    extendDialog?.let { reserva ->
        ExtendStayDateDialog(
            reservationId = reserva.reservation_id,
            currentCheckOut = reserva.check_out,
            busy = extendBusy,
            onDismiss = { if (!extendBusy) extendDialog = null },
            onConfirm = { iso ->
                extendPayment = ExtendPaymentPending(reserva, iso)
                extendDialog = null
            },
        )
    }

    extendPayment?.let { pending ->
        SimulatedPaymentBottomSheet(
            title = "Confirmar ampliación",
            subtitle = "${pending.reserva.reservation_id} · nueva salida",
            amountEur = null,
            busy = extendBusy,
            onDismiss = { if (!extendBusy) extendPayment = null },
            onConfirm = {
                extendBusy = true
                scope.launch {
                    try {
                        FlexibilityRepository.extendStay(
                            pending.reserva.reservation_id,
                            pending.newCheckOutIso,
                        ).fold(
                            onSuccess = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                extendPayment = null
                                LoyaltyFeedback.toastAfterAction(context)
                            },
                            onFailure = { e ->
                                Toast.makeText(
                                    context,
                                    e.message ?: "Error al ampliar estancia",
                                    Toast.LENGTH_LONG,
                                ).show()
                            },
                        )
                    } finally {
                        extendBusy = false
                        ReservationRepository.fetchReservations()
                    }
                }
            },
        )
    }

    LaunchedEffect(flexDialog?.first?.reservation_id, flexDialog?.second) {
        val pair = flexDialog ?: run {
            flexPreview = null
            return@LaunchedEffect
        }
        flexPreviewLoading = true
        flexPreview =
            FlexibilityRepository.getStatus(pair.first.reservation_id).getOrNull()
        flexPreviewLoading = false
    }

    flexDialog?.let { (reserva, kind) ->
        val tier = flexPreview?.loyalty_tier ?: reserva.early_checkin_requested?.loyalty_tier
        val feeLine = formatFeePreviewLine(kind.feeQuote(flexPreview))
        FlexibilityRequestDialog(
            kind = kind,
            reservationId = reserva.reservation_id,
            loyaltyTier = tier,
            feePreviewText = feeLine,
            loadingPreview = flexPreviewLoading,
            busy = flexSubmitBusy,
            onDismiss = {
                if (!flexSubmitBusy) {
                    flexDialog = null
                    flexPreview = null
                }
            },
            onConfirm = { hour, minute ->
                val quote = kind.feeQuote(flexPreview)
                val amount =
                    when {
                        quote?.free_access == true -> 0.0
                        else -> quote?.final_fee
                    }
                flexPayment =
                    FlexPaymentPending(reserva, kind, hour, minute, amount)
                flexDialog = null
                flexPreview = null
            },
        )
    }

    flexPayment?.let { pending ->
        val titulo =
            when (pending.kind) {
                FlexibilityKind.EARLY -> "Check-in anticipado"
                FlexibilityKind.LATE_FACILITIES -> "Instalaciones hasta 20:00"
                else -> "Salida tardía"
            }
        SimulatedPaymentBottomSheet(
            title = titulo,
            subtitle = "${pending.reserva.reservation_id} · ${pending.hour}:${"%02d".format(pending.minute)}",
            amountEur = pending.amountEur,
            busy = flexSubmitBusy,
            onDismiss = { if (!flexSubmitBusy) flexPayment = null },
            onConfirm = {
                flexSubmitBusy = true
                scope.launch {
                    try {
                        FlexibilityRepository.submitRequest(
                            pending.reserva,
                            pending.kind,
                            pending.hour,
                            pending.minute,
                        ).fold(
                            onSuccess = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                flexPayment = null
                                LoyaltyFeedback.toastAfterAction(context)
                            },
                            onFailure = { e ->
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
                            },
                        )
                    } finally {
                        flexSubmitBusy = false
                    }
                }
            },
        )
    }

    endOfStayFor?.let { reserva ->
        val checkOutFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        EndOfStayDecisionDialog(
            reservationId = reserva.reservation_id,
            checkOutFormatted = checkOutFmt.format(reserva.check_out),
            onDismiss = {
                endOfStayDismissed.value = endOfStayDismissed.value + reserva.reservation_id
                endOfStayFor = null
            },
            onExtendStay = {
                endOfStayFor = null
                extendDialog = reserva
            },
            onFacilitiesCheckout = {
                endOfStayFor = null
                flexDialog = reserva to FlexibilityKind.LATE_FACILITIES
            },
        )
    }
}

@Composable
private fun ActiveBookingCard(
    reserva: Reservation,
    fmt: SimpleDateFormat,
    invoiceDownloading: Boolean,
    onOpen: () -> Unit,
    onHistorial: () -> Unit,
    onRequestEarly: () -> Unit,
    onChooseEndOfStay: () -> Unit,
    onDescargarFactura: () -> Unit,
    onVerFacturas: () -> Unit,
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
            Spacer(modifier = Modifier.height(8.dp))
            FlexibilityStatusSection(reserva = reserva)
            Spacer(modifier = Modifier.height(8.dp))
            FlexibilityEarlyCheckInButton(reserva = reserva, onEarly = onRequestEarly)
            if (reserva.needsEndOfStayChoice()) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onChooseEndOfStay,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                ) {
                    Text("Hora de salida cumplida — elegir opción")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (reserva.tieneFactura()) {
                if (invoiceDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = onDescargarFactura, modifier = Modifier.fillMaxWidth()) {
                        Text("Descargar factura (PDF)")
                    }
                }
            } else {
                TextButton(onClick = onVerFacturas, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver mis facturas")
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
                    text = "Listado de todas tus reservas.",
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

