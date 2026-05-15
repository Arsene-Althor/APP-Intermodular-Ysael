package com.example.hotel_pere_maria_app.ui.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Models.RoomRepository
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(navController: NavHostController, roomId: String) {
    val scope = rememberCoroutineScope()
    var room by remember { mutableStateOf<Room?>(null) }
    var price by remember { mutableDoubleStateOf(0.0) }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId) {
        loading = true
        error = null
        val r = RoomRepository.getRoomById(roomId)
        room = r
        if (r == null) {
            error = "No se encontró la habitación"
            loading = false
            return@LaunchedEffect
        }
        val userId = SessionManager.userInfo?.user_id ?: ""
        val ci = BookingSearchSession.checkInIso()
        val co = BookingSearchSession.checkOutIso()
        if (userId.isBlank() || ci == null || co == null) {
            error = "Faltan fechas o sesión"
            loading = false
            return@LaunchedEffect
        }
        try {
            val body =
                mapOf(
                    "room_id" to roomId,
                    "user_id" to userId,
                    "check_in" to ci,
                    "check_out" to co,
                )
            val resp = RetrofitClient.reservationService.getPrice(body)
            if (!resp.isSuccessful) {
                val rawErr = resp.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(resp.code(), rawErr)) {
                    SessionManager.handleUnauthorized()
                    return@LaunchedEffect
                }
                error =
                    try {
                        JSONObject(rawErr).optString("error", rawErr).ifBlank { "No se pudo calcular el precio" }
                    } catch (_: Exception) {
                        rawErr.ifBlank { "No se pudo calcular el precio (${resp.code()})" }
                    }
            } else if (resp.body() != null) {
                val raw = resp.body()!!["precio"]
                price =
                    when (raw) {
                        is Double -> raw
                        is Float -> raw.toDouble()
                        is Int -> raw.toDouble()
                        is Number -> raw.toDouble()
                        else -> 0.0
                    }
                if (!price.isFinite() || price < 0) {
                    error = "Precio no válido en la respuesta del servidor"
                }
            } else {
                error = "Respuesta vacía del servidor"
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar reserva") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
        ) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                room != null -> {
                    Text(room!!.type, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(room!!.room_id, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Entrada: ${BookingSearchSession.checkInDisplay()} · Salida: ${BookingSearchSession.checkOutDisplay()}",
                    )
                    Text("Huéspedes: ${BookingSearchSession.guests}")
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    ) {
                        Text(
                            "Total estimado: ${"%.2f".format(price)} €",
                            Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val userId = SessionManager.userInfo?.user_id ?: return@Button
                            val ci = BookingSearchSession.checkInIso() ?: return@Button
                            val co = BookingSearchSession.checkOutIso() ?: return@Button
                            submitting = true
                            scope.launch {
                                try {
                                    val req =
                                        mapOf(
                                            "room_id" to roomId,
                                            "user_id" to userId,
                                            "check_in" to ci,
                                            "check_out" to co,
                                            "price" to price.toString(),
                                        )
                                    @Suppress("UNCHECKED_CAST")
                                    val res =
                                        RetrofitClient.reservationService.addReservation(
                                            req as Map<String, String>,
                                        )
                                    if (!res.isSuccessful) {
                                        val raw = res.errorBody()?.string().orEmpty()
                                        if (SessionManager.shouldLogoutForApiError(res.code(), raw)) {
                                            SessionManager.handleUnauthorized()
                                            return@launch
                                        }
                                        message =
                                            try {
                                                val jo = JSONObject(raw)
                                                val det = jo.optString("detalle", "").trim()
                                                val err = jo.optString("error", "").trim()
                                                when {
                                                    det.isNotBlank() && err.isNotBlank() -> "$err: $det"
                                                    det.isNotBlank() -> det
                                                    err.isNotBlank() -> err
                                                    else -> raw.ifBlank { "Error al reservar (${res.code()})" }
                                                }
                                            } catch (_: Exception) {
                                                raw.ifBlank { "Error al reservar (${res.code()})" }
                                            }
                                    } else {
                                        ReservationRepository.fetchReservations()
                                        navController.navigate(Routes.Reservations.route) {
                                            popUpTo(Routes.BookingHome.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    message = e.message
                                } finally {
                                    submitting = false
                                }
                            }
                        },
                        enabled = !submitting && price > 0,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Confirmar y pagar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
