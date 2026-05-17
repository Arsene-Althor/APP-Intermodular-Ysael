package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.core.navigation.Routes
import com.example.hotel_pere_maria_app.core.navigation.navigateToBookingHome
import com.example.hotel_pere_maria_app.core.util.MediaUrls
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.core.session.SessionUi

/**
 * Barra superior: logo del hotel ([R.drawable.hotel_logo]), título centrado, foto de perfil.
 */
@Composable
fun TopAppBarState(navController: NavHostController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    fun refreshProfileUrl() {
        profileImageUrl = MediaUrls.profileImage(SessionManager.userInfo?.profileImage)
    }

    LaunchedEffect(backStackEntry?.id, SessionUi.userInfoTick) { refreshProfileUrl() }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) refreshProfileUrl()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                TopBarProfileAvatar(imageUrl = profileImageUrl)
            }
        }
    }
}

@Composable
private fun TopBarProfileAvatar(imageUrl: String?) {
    val size = 40.dp
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Mi cuenta",
            modifier =
                Modifier.size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                Modifier.size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Mi cuenta",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

