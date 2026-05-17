package com.example.hotel_pere_maria_app.feature.loyalty

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.data.model.UserStayHistoryItem
import com.example.hotel_pere_maria_app.data.repository.UserStayRepository
import com.example.hotel_pere_maria_app.core.navigation.Routes
import com.example.hotel_pere_maria_app.core.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStaysScreen(navController: NavController) {
    val stays by UserStayRepository.history.collectAsState()
    val loading by UserStayRepository.historyLoading.collectAsState()
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val uid = SessionManager.userInfo?.user_id

    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            UserStayRepository.fetchHistory(uid, status = "completed")
            UserStayRepository.fetchStats(uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis estancias") },
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
        if (loading && stays.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Estancias completadas. Pulsa una para ver el detalle y tu valoración.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            if (stays.isEmpty()) {
                item { Text("Aún no tienes estancias completadas.") }
            } else {
                items(stays, key = { it.reservation_id }) { stay ->
                    StayListCard(stay, fmt) {
                        navController.navigate(Routes.StayDetail.createRoute(stay.reservation_id))
                    }
                }
            }
        }
    }
}

@Composable
private fun StayListCard(
    stay: UserStayHistoryItem,
    fmt: SimpleDateFormat,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val img = stay.room?.image
            if (!img.isNullOrBlank()) {
                AsyncImage(
                    model = img.split(',').firstOrNull()?.trim() ?: img,
                    contentDescription = stay.room?.type,
                    modifier =
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stay.room?.type ?: stay.room?.room_id ?: "Habitación",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "${stay.check_in?.let { fmt.format(it) } ?: "—"} → ${stay.check_out?.let { fmt.format(it) } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${String.format(Locale.getDefault(), "%.2f", stay.total_paid.toDouble())} € · ${stay.nights} noches",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                stay.rating?.takeIf { it.rating > 0 }?.let {
                    Text("★ ${it.rating}/5", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

