package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pantalla provisional para la sección global de Reseñas. El requisito inicial era integrar las
 * reseñas en el detalle de la habitación, pero como se ha añadido un acceso directo en la
 * navegación, esta pantalla sirve como marcador de posición.
 */
@Composable
fun ReviewsScreen() {
    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = "Sección de Reseñas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "Aquí podrás ver todas las reseñas del hotel próximamente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
                text =
                        "Nota: Ya puedes ver y dejar reseñas entrando en el detalle de cualquier habitación en la sección 'Rooms'.",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
        )
    }
}
