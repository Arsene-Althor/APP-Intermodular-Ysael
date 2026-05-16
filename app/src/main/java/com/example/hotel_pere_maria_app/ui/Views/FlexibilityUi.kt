package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_pere_maria_app.ui.Models.FlexibilityKind
import com.example.hotel_pere_maria_app.ui.Models.FlexibilityRequestBlock
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.Models.canRequestEarly
import com.example.hotel_pere_maria_app.ui.Models.loyaltyTierLabel
import java.text.SimpleDateFormat
import java.util.Locale

data class FlexStatusUi(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
)

fun flexStatusUi(status: String?): FlexStatusUi? =
    when (status) {
        "pending" ->
            FlexStatusUi(
                "Pendiente",
                Color(0xFFFFF3E0),
                Color(0xFFE65100),
            )
        "approved" ->
            FlexStatusUi(
                "Aprobada",
                Color(0xFFE8F5E9),
                Color(0xFF2E7D32),
            )
        "rejected" ->
            FlexStatusUi(
                "Rechazada",
                Color(0xFFFFEBEE),
                Color(0xFFC62828),
            )
        else -> null
    }

@Composable
fun FlexibilityStatusChip(block: FlexibilityRequestBlock?, prefix: String) {
    val ui = flexStatusUi(block?.status) ?: return
    val timeFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val hora = block?.requested_time?.let { timeFmt.format(it) } ?: ""
    val modo =
        if (block?.late_mode == "facilities") " · instalaciones" else ""
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = ui.containerColor) {
            Text(
                text = "$prefix: ${ui.label}" + if (hora.isNotBlank()) " ($hora)$modo" else modo,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = ui.contentColor,
            )
        }
        if (block?.status == "rejected" && !block.review_note.isNullOrBlank()) {
            Text(
                text = "Motivo: ${block.review_note}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlexibilityRequestDialog(
    kind: FlexibilityKind,
    reservationId: String,
    loyaltyTier: String?,
    feePreviewText: String?,
    loadingPreview: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val defaultHour = if (kind == FlexibilityKind.EARLY) 10 else 14
    val timeState = rememberTimePickerState(initialHour = defaultHour, initialMinute = 0, is24Hour = true)
    val titulo =
        when (kind) {
            FlexibilityKind.EARLY -> "Check-in anticipado"
            FlexibilityKind.LATE_FACILITIES -> "Instalaciones (sin habitación)"
            else -> "Salida tardía (habitación)"
        }
    val horaStd = if (kind == FlexibilityKind.EARLY) "12:00" else "11:00 · máx. 20:00"

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "$reservationId · hora estándar $horaStd · ${loyaltyTierLabel(loyaltyTier)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                when {
                    loadingPreview ->
                        Text("Calculando tarifa…", style = MaterialTheme.typography.bodySmall)
                    !feePreviewText.isNullOrBlank() ->
                        Text(feePreviewText, style = MaterialTheme.typography.bodySmall)
                }
                TimePicker(state = timeState)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timeState.hour, timeState.minute) },
                enabled = !busy,
            ) {
                Text(if (busy) "Enviando…" else "Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") }
        },
    )
}

@Composable
fun FlexibilityStatusSection(reserva: Reservation) {
    val hasAny =
        reserva.early_checkin_requested?.status != null ||
            reserva.late_checkout_requested?.status != null
    if (!hasAny) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Solicitudes",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlexibilityStatusChip(reserva.early_checkin_requested, "Entrada anticipada")
        FlexibilityStatusChip(reserva.late_checkout_requested, "Salida tardía")
    }
}

@Composable
fun FlexibilityEarlyCheckInButton(
    reserva: Reservation,
    onEarly: () -> Unit,
) {
    if (!reserva.canRequestEarly()) return
    OutlinedButton(onClick = onEarly, modifier = Modifier.fillMaxWidth()) {
        Text("Check-in anticipado")
    }
}

/** Tras la hora de salida: ampliar habitación o instalaciones sin habitación. */
@Composable
fun EndOfStayDecisionDialog(
    reservationId: String,
    checkOutFormatted: String,
    onDismiss: () -> Unit,
    onExtendStay: () -> Unit,
    onFacilitiesCheckout: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fin de estancia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Su salida ($checkOutFormatted) ya pasó. Reserva $reservationId.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Libere la habitación o amplíe. También puede quedarse en instalaciones del hotel (sin habitación) hasta las 20:00.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onExtendStay, modifier = Modifier.fillMaxWidth()) {
                    Text("Ampliar estancia")
                }
                OutlinedButton(onClick = onFacilitiesCheckout, modifier = Modifier.fillMaxWidth()) {
                    Text("Checkout tardío — solo instalaciones")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Más tarde") }
        },
    )
}

/** Modal de pago simulado (paridad con reserva nueva / ModReserva). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatedPaymentBottomSheet(
    title: String,
    subtitle: String,
    amountEur: Double?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total a pagar:", style = MaterialTheme.typography.titleMedium)
                val amountLabel =
                    when {
                        amountEur == null -> "Según tarifa"
                        amountEur <= 0.0 -> "Sin cargo"
                        else -> "%.2f €".format(amountEur)
                    }
                Text(
                    amountLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                "Pago simulado. No se realizará ningún cargo real.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onConfirm,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (busy) "Procesando…" else "Confirmar pago")
            }
            TextButton(onClick = onDismiss, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendStayDateDialog(
    reservationId: String,
    currentCheckOut: Date,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newCheckOutIso: String) -> Unit,
) {
    val checkoutCal =
        Calendar.getInstance().apply {
            time = currentCheckOut
            add(Calendar.HOUR_OF_DAY, 2)
        }
    val datePickerState = rememberDatePickerState()
    val timeState =
        rememberTimePickerState(
            initialHour = checkoutCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = checkoutCal.get(Calendar.MINUTE),
            is24Hour = true,
        )
    val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)

    DatePickerDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@Button
                    val utcDay =
                        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                    val cal =
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcDay.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcDay.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcDay.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                    onConfirm(isoFmt.format(cal.time))
                },
                enabled = !busy,
            ) {
                Text(if (busy) "Procesando…" else "Ampliar y pagar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ampliar estancia", style = MaterialTheme.typography.titleMedium)
            Text(
                "$reservationId · salida ${dateFmt.format(currentCheckOut)}",
                style = MaterialTheme.typography.bodySmall,
            )
            DatePicker(state = datePickerState)
            TimePicker(state = timeState)
        }
    }
}
