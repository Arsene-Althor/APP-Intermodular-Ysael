package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.UserStayRepository
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StayDetailScreen(navController: NavController, reservationId: String) {
    val history by UserStayRepository.history.collectAsState()
    val stay = history.find { it.reservation_id == reservationId }
    val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle estancia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
            )
        },
    ) { padding ->
        if (stay == null) {
            Text(
                "No se encontró la estancia.",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            return@Scaffold
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            stay.room?.image?.let { img ->
                AsyncImage(
                    model = img.split(',').firstOrNull()?.trim() ?: img,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(stay.reservation_id, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(stay.room?.type ?: "Habitación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            stay.room?.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fechas", fontWeight = FontWeight.SemiBold)
                    Text("Entrada: ${stay.check_in?.let { fmt.format(it) } ?: "—"}")
                    Text("Salida: ${stay.check_out?.let { fmt.format(it) } ?: "—"}")
                    Text("Noches: ${stay.nights}")
                    Text(
                        "Importe: ${String.format(Locale.getDefault(), "%.2f", stay.total_paid.toDouble())} €",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text("Estado: ${stay.status ?: "—"}")
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tu valoración", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    val r = stay.rating
                    if (r != null && r.rating > 0) {
                        RowWithStars(r.rating)
                        r.comment?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(
                            "No hay valoración vinculada a esta estancia. Puedes dejar una reseña de la habitación desde el detalle de la habitación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowWithStars(rating: Int) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { i ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint =
                    if (i < rating) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        }
        Text(" $rating/5", fontWeight = FontWeight.Bold)
    }
}
