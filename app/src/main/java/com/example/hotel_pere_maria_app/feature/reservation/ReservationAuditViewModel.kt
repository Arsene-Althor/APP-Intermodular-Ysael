package com.example.hotel_pere_maria_app.feature.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.data.model.BookingHistoryFriendlyMapper
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class ReservationAuditUiState(
    val reservationId: String = "",
    val historialItems: List<HistorialItemUi> = emptyList(),
    val historialCargando: Boolean = true,
)

/**
 * Solo historial amigable de una reserva (GET /reservation/:id/audit).
 */
class ReservationAuditViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val reservaId: String = checkNotNull(savedStateHandle["reservationId"])

    private val _uiState = MutableStateFlow(ReservationAuditUiState(reservationId = reservaId))
    val uiState: StateFlow<ReservationAuditUiState> = _uiState

    init {
        viewModelScope.launch { cargarHistorial() }
    }

    private suspend fun cargarHistorial() {
        _uiState.update { it.copy(historialCargando = true) }
        try {
            val response = RetrofitClient.reservationService.getBookingAudit(reservaId)
            val list = if (response.isSuccessful) response.body().orEmpty() else emptyList()
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES"))
            val items =
                list.map { entry ->
                    val fechaTexto = entry.timestamp?.let { fmt.format(it) } ?: "—"
                    val mensaje = BookingHistoryFriendlyMapper.toUserMessage(entry.action)
                    HistorialItemUi(fechaTexto = fechaTexto, mensaje = mensaje)
                }
            _uiState.update { it.copy(historialItems = items, historialCargando = false) }
        } catch (_: Exception) {
            _uiState.update { it.copy(historialItems = emptyList(), historialCargando = false) }
        }
    }
}

