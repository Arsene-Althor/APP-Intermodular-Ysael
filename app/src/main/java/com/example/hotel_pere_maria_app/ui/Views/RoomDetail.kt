package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Models.Review
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.ViewModels.ReviewViewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel
import com.example.hotel_pere_maria_app.ui.booking.BookingSearchSession

/**
 * Pantalla de detalles de una habitación específica. Incluye información de la habitación y sección
 * de reseñas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomDetail(
        navController: NavController,
        roomId: String,
        viewModel: RoomViewModel = viewModel(),
        reviewViewModel: ReviewViewModel = viewModel()
) {
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val detailError by viewModel.detailError.collectAsState()

    // Cargar detalles de la habitación y reseñas cuando se abre la pantalla
    LaunchedEffect(roomId) {
        viewModel.loadRoomDetails(roomId)
        reviewViewModel.loadReviews(roomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de la Habitación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoadingDetail -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                detailError != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = detailError ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.loadRoomDetails(roomId)
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                selectedRoom != null -> {
                    val room = selectedRoom!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        // Galería (varias URLs separadas por coma en API)
                        val urls = room.galleryImageUrls().filter { it.isNotBlank() }
                        if (urls.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {}
                        } else {
                            val pagerState = rememberPagerState(pageCount = { urls.size })
                            Column(Modifier.fillMaxWidth()) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth().height(280.dp),
                                    beyondViewportPageCount = 1,
                                ) { page ->
                                    AsyncImage(
                                        model = urls[page],
                                        contentDescription = "Imagen ${page + 1} de ${urls.size}",
                                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    repeat(urls.size) { i ->
                                        val active = pagerState.currentPage == i
                                        Surface(
                                            modifier =
                                                Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .size(if (active) 10.dp else 7.dp),
                                            shape = CircleShape,
                                            color =
                                                if (active) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant,
                                        ) {}
                                    }
                                }
                                Text(
                                    "Desliza izquierda/derecha para cambiar de foto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Precio destacado (oferta si aplica)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Precio por noche",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (room.offerActive && room.offerPercent > 0) {
                                        Text(
                                            text = "Oferta ${room.offerPercent.toInt()}% sobre tarifa base",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (room.offerActive && room.offerPercent > 0) {
                                        Text(
                                            text = "€${room.price_per_night}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = "€${room.displayPricePerNight()}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Información adicional
                        InfoSection(title = "Información General") {
                            InfoRow(
                                icon = Icons.Default.Star,
                                label = "Valoración",
                                value = "${room.rate}/5.0",
                                iconTint = Color(0xFFFFC107)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "Ocupación máxima",
                                value = "${room.max_occupancy} personas"
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Descripción
                        InfoSection(title = "Descripción") {
                            Text(
                                text = room.description,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── SECCIÓN DE RESEÑAS ────────────────────────────────
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Danos tu opinión",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Formulario para dejar reseña (Lo ponemos primero para que sea lo que el usuario ve "abajo de los detalles")
                        val canReview by reviewViewModel.canReview.collectAsState()
                        val userReview by reviewViewModel.userReview.collectAsState()

                        if (canReview && userReview == null) {
                            ReviewForm(roomId, reviewViewModel)
                            Spacer(modifier = Modifier.height(24.dp))
                        } else if (userReview != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.3f
                                    )
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                    Text(
                                        text = "Ya has dejado una reseña para esta habitación.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Lista de reseñas existentes
                        Text(
                            text = "Reseñas de otros clientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val reviews by reviewViewModel.reviews.collectAsState()
                        val isLoadingReviews by reviewViewModel.isLoading.collectAsState()

                        if (isLoadingReviews) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        } else if (reviews.isEmpty()) {
                            Text(
                                text = "Aún no hay reseñas para esta habitación.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            reviews.forEach { review ->
                                ReviewItem(review)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        if (BookingSearchSession.isComplete()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    navController.navigate(Routes.BookingConfirm.createRoute(room.room_id))
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Reservar con estas fechas", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

/** Muestra estrellas de solo lectura */
@Composable
fun StarRatingDisplay(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** Formatea la fecha ISO para mostrar */
fun formatReviewDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("T")
        if (parts.isNotEmpty()) parts[0] else isoDate
    } catch (e: Exception) {
        isoDate
    }
}

/** Sección de información con título */
@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { content() } }
    }
}

/** Fila de información con icono */
@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.user_name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                StarRatingDisplay(review.rating)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )

            if (review.createdAt != null) {
                Text(
                    text = formatReviewDate(review.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ReviewForm(roomId: String, viewModel: ReviewViewModel) {
    val rating by viewModel.selectedRating.collectAsState()
    val comment by viewModel.commentText.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.submitMessage.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.2f
            )
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Deja tu reseña",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de estrellas interactivo
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { viewModel.setRating(i) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "$i estrellas",
                            tint = if (i <= rating) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { viewModel.setComment(it) },
                label = { Text("Tu opinión") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitReview(roomId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Publicar Reseña")
                }
            }

            message?.let {
                Text(
                    text = it,
                    color = if (it.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
