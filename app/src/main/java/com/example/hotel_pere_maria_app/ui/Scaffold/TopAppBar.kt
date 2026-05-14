package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.hotel_pere_maria_app.ui.Navegation.navigateToBookingHome

/**
 * Barra superior: logo rectangular (sin recorte circular), nombre del hotel centrado en el espacio
 * disponible, solo acceso a perfil. Inicio y reservas están en [BottomBookingBar].
 */
@Composable
fun TopAppBarState(navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.hotel_logo),
                contentDescription = "Ir al inicio",
                modifier =
                    Modifier.size(width = 72.dp, height = 52.dp)
                        .clickable { navController.navigateToBookingHome() },
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "Hotel Pere Maria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier.weight(1f)
                        .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            IconButton(
                onClick = {
                    navController.navigate(Routes.User.route) { launchSingleTop = true }
                },
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Mi cuenta",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
