package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun Home() {
    val homeviewModel : HomeViewModel = viewModel()
    val reservas by homeviewModel.listMisReservas.collectAsState(initial = emptyList())
    val reservaReciente by homeviewModel.proximaReserva.collectAsState(initial = null)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Hola, Bienvenido!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column() {
                Text(text = "Servicios del Hotel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceItem(Icons.Default.LocationOn, "Mapa")
                    ServiceItem(Icons.Default.Phone, "Llamar")
                    ServiceItem(Icons.Default.Email, "Correo")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item{
                Text(text = "Proxima estancia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if(reservaReciente != null){
                    proximaEstancia(reservaReciente!!)
                }else{
                    SinproxEstancia()
                }
                Text(text = "Todas tus estancias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if(reservas.isEmpty()){
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.7f),
                        contentAlignment = Alignment.Center
                        ){
                        Text(
                            text = "¿A qué esperas para tu primera reserva?",
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }else{

                items(reservas){ reserva ->
                    CardReserva(reserva)
                }

            }
        }

    }
}

@Composable
fun proximaEstancia(reserva: Reservation){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = reserva.reservation_id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reserva.room_id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_in)}" +
                            " - " +
                            "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_out)}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

        }
    }
}
@Composable
fun SinproxEstancia(){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "No tienes reservas próximas. ¿A que esperas?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CardReserva(reserva: Reservation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reserva.reservation_id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton (onClick = {},modifier = Modifier.size(14.dp)){
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar reserva",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reserva.room_id,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_in)}" +
                            " - " +
                            "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reserva.check_out)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ServiceItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomePreview(){
    Home()
}

