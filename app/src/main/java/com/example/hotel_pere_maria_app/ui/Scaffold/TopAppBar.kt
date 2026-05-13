package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.Navegation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarView() {
    TopAppBar(
        title = { Text("HOTEL PERE MARIA", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.hotel_logo),
                contentDescription = "Logo Hotel Pere Maria",
                modifier = Modifier.size(90.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarState(navController: NavHostController) {
    TopAppBar(
        title = { Text("HOTEL PERE MARIA", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.hotel_logo),
                contentDescription = "Logo Hotel Pere Maria",
                modifier = Modifier.size(90.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            )
        },
        actions = {
            IconButton(
                onClick = {
                    navController.navigate(Routes.Reservations.route) {
                        launchSingleTop = true
                    }
                }
            ) {
                Icon(Icons.Default.Event, contentDescription = "Mis reservas")
            }
            IconButton(
                onClick = {
                    navController.navigate(Routes.MyAccount.route) { launchSingleTop = true }
                }
            ) {
                Icon(Icons.Default.Person, contentDescription = "Mi cuenta")
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    )
}
