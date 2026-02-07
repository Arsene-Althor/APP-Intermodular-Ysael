package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.Navegation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarView(onInfoClick: () -> Unit, onSettingsClick: () -> Unit){
    TopAppBar(
        title = { Text("HOTEL PERE MARIA", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.hotel_logo),
                contentDescription = "Logo HOtel Pere Maria",
                modifier = Modifier.size(90.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
        },
        actions = {
            IconButton(onClick = onInfoClick){ Icon(imageVector = Icons.Default.Info, contentDescription=null) }
            IconButton(onClick = onSettingsClick){ Icon(imageVector = Icons.Default.Settings, contentDescription=null) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun TopAppBarState(navController: NavHostController){
    fun navigateWithState(route: String){
        navController.navigate(route){
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun onInfoClick(){ navigateWithState(Routes.Home.route) }
    fun onSettingsClick(){ navigateWithState(Routes.Home.route) }
    TopAppBarView(
        onInfoClick = { onInfoClick() },
        onSettingsClick = { onSettingsClick() })
}