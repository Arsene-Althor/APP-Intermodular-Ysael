package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import com.example.hotel_pere_maria_app.ui.ViewModels.ReviewViewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel

/**
 * Pantalla de detalles de una habitación específica. Incluye información de la habitación y sección
 * de reseñas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetail(
        navController: NavController,
        roomId: String,
        viewModel: RoomViewModel = viewModel(),
        reviewViewModel: ReviewViewModel = viewModel()
) {
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val canReview by reviewViewModel.canReview.collectAsState()
    val userReview by reviewViewModel.userReview.collectAsState()
    val isSubmitting by reviewViewModel.isSubmitting.collectAsState()
    val submitMessage by reviewViewModel.submitMessage.collectAsState()
    val selectedRating by reviewViewModel.selectedRating.collectAsState()
    val commentText by reviewViewModel.commentText.collectAsState()

    // Cargar detalles de la habitación y reseñas cuando se abre la pantalla
    LaunchedEffect(roomId) {
        viewModel.loadRoomDetails(roomId)
        reviewViewModel.loadReviews(roomId)
    }

    // Snackbar para mensajes de feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(submitMessage) {
        submitMessage?.let {
            snackbarHostState.showSnackbar(it)
            reviewViewModel.clearMessage()
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Detalles de la Habitación") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Volver"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            selectedRoom?.let { room ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Imagen principal
                    AsyncImage(
                            model = room.image,
                            contentDescription = "Imagen de ${room.type}",
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentScale = ContentScale.Crop
                    )

                    // Contenido de detalles
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        // Tipo y estado
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = room.type,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                            )

                            Surface(
                                    color =
                                            if (room.isAvailable) Color(0xFF4CAF50)
                                            else Color(0xFFF44336),
                                    shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                        text = if (room.isAvailable) "Disponible" else "Ocupada",
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 8.dp
                                                ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ID de la habitación
                        Text(
                                text = "ID: ${room.room_id}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Precio destacado
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        ) {
                            Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        text = "Precio por noche:",
                                        style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                        text = "€${room.price_per_night}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                )
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

                        // Botón de reservar
                        if (room.isAvailable) {
                            Button(
                                    onClick = { /* TODO: Implementar reserva */},
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                        text = "Reservar Habitación",
                                        style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else {
                            OutlinedButton(
                                    onClick = { /* No disponible */},
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                        text = "Habitación no disponible",
                                        style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(24.dp))

                        // ═══════════════════════════════════════════
                        // SECCIÓN DE RESEÑAS
                        // ═══════════════════════════════════════════

                        Text(
                                text = "Reseñas (${reviews.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Formulario para dejar reseña ─────────────────
                        if (canReview && userReview == null) {
                            ReviewForm(
                                    selectedRating = selectedRating,
                                    commentText = commentText,
                                    isSubmitting = isSubmitting,
                                    onRatingChange = { reviewViewModel.setRating(it) },
                                    onCommentChange = { reviewViewModel.setComment(it) },
                                    onSubmit = { reviewViewModel.submitReview(roomId) }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // ── Reseña propia del usuario ────────────────────
                        userReview?.let { myReview ->
                            Text(
                                    text = "Tu reseña",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewCard(
                                    review = myReview,
                                    isOwn = true,
                                    onDelete = { reviewViewModel.deleteMyReview(roomId) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // ── Lista de reseñas de otros usuarios ───────────
                        val userId = SessionManager.userInfo?.user_id
                        val otherReviews = reviews.filter { it.user_id != userId }

                        if (otherReviews.isEmpty() && userReview == null) {
                            Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                            CardDefaults.cardColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.surfaceVariant
                                            )
                            ) {
                                Text(
                                        text =
                                                "Aún no hay reseñas para esta habitación. ¡Sé el primero en opinar!",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            otherReviews.forEach { review ->
                                ReviewCard(review = review)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
                    ?: run {
                        // Mostrar loading mientras se carga
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// COMPONENTES DE RESEÑAS
// ═══════════════════════════════════════════════════════════════════

/** Formulario para escribir una nueva reseña */
@Composable
fun ReviewForm(
        selectedRating: Int,
        commentText: String,
        isSubmitting: Boolean,
        onRatingChange: (Int) -> Unit,
        onCommentChange: (String) -> Unit,
        onSubmit: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
            shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                    text = "Deja tu reseña",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de estrellas
            Text(text = "Puntuación:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            StarRatingSelector(rating = selectedRating, onRatingChange = onRatingChange)

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de comentario
            OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentChange,
                    label = { Text("Tu comentario") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón enviar
            Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting && selectedRating > 0 && commentText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Publicar reseña")
            }
        }
    }
}

/** Selector de estrellas interactivo (1-5) */
@Composable
fun StarRatingSelector(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrella $i",
                    tint = if (i <= rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                    modifier = Modifier.size(36.dp).clickable { onRatingChange(i) }
            )
        }
    }
}

/** Tarjeta de una reseña individual */
@Composable
fun ReviewCard(review: Review, isOwn: Boolean = false, onDelete: (() -> Unit)? = null) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isOwn)
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.5f
                                            )
                                    else MaterialTheme.colorScheme.surfaceVariant
                    ),
            shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Cabecera: nombre + estrellas + botón borrar
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar + nombre
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                    text = review.user_name.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = review.user_name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }

                // Estrellas + borrar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRatingDisplay(rating = review.rating)
                    if (isOwn && onDelete != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar reseña",
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comentario
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)

            // Fecha
            review.createdAt?.let { date ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = formatReviewDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                colors =
                        CardDefaults.cardColors(
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
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
