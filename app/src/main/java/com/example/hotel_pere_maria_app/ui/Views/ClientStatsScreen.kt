package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_pere_maria_app.ui.Models.ClientLoyaltyStatsDto
import com.example.hotel_pere_maria_app.ui.Models.LoyaltyStatsRepository
import com.example.hotel_pere_maria_app.ui.Models.TierThresholdsDto
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientStatsScreen() {
    val stats by LoyaltyStatsRepository.stats.collectAsState()
    val loading by LoyaltyStatsRepository.loading.collectAsState()
    val error by LoyaltyStatsRepository.error.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        LoyaltyStatsRepository.fetchMyStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis estadísticas") },
                actions = {
                    IconButton(
                        onClick = { scope.launch { LoyaltyStatsRepository.fetchMyStats() } },
                        enabled = !loading,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            if (loading && stats == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
            }
            stats?.let { s ->
                P9InsightsCard(s)
                Spacer(Modifier.height(12.dp))
                TierHeroCard(s)
                Spacer(Modifier.height(12.dp))
                StatsGrid(s)
                Spacer(Modifier.height(12.dp))
                TierProgressCard(s)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TierHeroCard(stats: ClientLoyaltyStatsDto) {
    val tierUi = tierUi(stats.loyalty_tier)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tierUi.bg),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Default.Star, null, tint = tierUi.accent, modifier = Modifier.size(48.dp))
            Column {
                Text("Tu rango", style = MaterialTheme.typography.labelLarge)
                Text(
                    tierUi.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = tierUi.accent,
                )
                Text(
                    "Descuentos en solicitudes especiales (entrada/salida flexible)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: ClientLoyaltyStatsDto) {
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("Noches", "${stats.total_nights}", Modifier.weight(1f))
        StatCard("Gastado", "${String.format(Locale.getDefault(), "%.0f", stats.total_spent)} €", Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("Completadas", "${stats.completed_stays_count}", Modifier.weight(1f))
        StatCard("Reservas", "${stats.summary?.total_reservations ?: 0}", Modifier.weight(1f))
    }
    stats.last_stay_checkout_at?.let {
        Spacer(Modifier.height(10.dp))
        Text(
            "Última salida: ${fmt.format(it)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun P9InsightsCard(stats: ClientLoyaltyStatsDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tu perfil de huésped", fontWeight = FontWeight.SemiBold)
            stats.favorite_season?.let {
                Text("Temporada favorita: $it", style = MaterialTheme.typography.bodyMedium)
            }
            stats.most_booked_room?.let { r ->
                Text(
                    "Habitación más reservada: ${r.room_id} (${r.type ?: "—"}) · ${r.bookings_count} veces",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (stats.max_stay_streak > 0) {
                Text(
                    "Racha máxima de estancias seguidas: ${stats.max_stay_streak}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TierProgressCard(stats: ClientLoyaltyStatsDto) {
    val t = stats.tier_thresholds ?: TierThresholdsDto()
    val tier = stats.loyalty_tier ?: "bronze"
    val (nextLabel, progress) =
        when (tier) {
            "gold" -> "Rango máximo alcanzado" to 1f
            "silver" -> {
                val pN = min(1f, stats.total_nights.toFloat() / t.gold_nights.coerceAtLeast(1))
                val pS = min(1f, stats.total_spent.toFloat() / t.gold_spent.coerceAtLeast(1))
                "Progreso a Oro" to maxOf(pN, pS)
            }
            else -> {
                val pN = min(1f, stats.total_nights.toFloat() / t.silver_nights.coerceAtLeast(1))
                val pS = min(1f, stats.total_spent.toFloat() / t.silver_spent.coerceAtLeast(1))
                "Progreso a Plata" to maxOf(pN, pS)
            }
        }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(nextLabel, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                "Plata: ${t.silver_nights} noches o ${t.silver_spent} € · Oro: ${t.gold_nights} noches o ${t.gold_spent} €",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class TierUi(val label: String, val bg: Color, val accent: Color)

private fun tierUi(tier: String?): TierUi =
    when (tier) {
        "gold" -> TierUi("Oro", Color(0xFFFFF8E1), Color(0xFFF9A825))
        "silver" -> TierUi("Plata", Color(0xFFECEFF1), Color(0xFF546E7A))
        else -> TierUi("Bronce", Color(0xFFFFF3E0), Color(0xFF8D6E63))
    }
