package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.MyReviewsViewModel

/**
 * Reseñas que el usuario logueado ha publicado (GET /review/mine). Para crear reseñas nuevas sigue
 * entrando en el detalle de la habitación (Rooms).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(onBack: (() -> Unit)? = null, vm: MyReviewsViewModel = viewModel()) {
    val lista by vm.myReviews.collectAsState()
    val cargando by vm.myReviewsLoading.collectAsState()
    val error by vm.myReviewsError.collectAsState()

    Scaffold(
            topBar = {
                if (onBack != null) {
                    TopAppBar(
                            title = { Text("Mis reseñas") },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver",
                                    )
                                }
                            },
                            colors =
                                    TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                    )
                }
            },
    ) { padding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onBack == null) {
                Text(
                        text = "Mis reseñas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                )
            }
            Text(
                    text =
                            "Valoraciones que has publicado. Para valorar una estancia, abre el detalle de una habitación en «Rooms».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(onClick = { vm.refresh() }, modifier = Modifier.fillMaxWidth()) { Text("Actualizar lista") }

            when {
                cargando && lista.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null -> {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                }
                lista.isEmpty() -> {
                    Text(
                            text = "Aún no has publicado ninguna reseña.",
                            style = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> {
                    LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                    ) {
                        items(lista, key = { it.review_id }) { review ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                        text = "Habitación: ${review.room_id}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ReviewItem(review)
                            }
                        }
                    }
                }
            }
        }
    }
}
