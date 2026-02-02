package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.ui.Navegation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarView(onInfoClick: () -> Unit, onShareClick: () -> Unit, onSettingsClick: () -> Unit){
    TopAppBar(
        title = { Text("My App") },
        navigationIcon = { Icon( painter=painterResource(R.drawable.ic_launcher_foreground),contentDescription=null) },
        actions = {
            IconButton(onClick = onInfoClick){ Icon(imageVector = Icons.Default.Info, contentDescription=null) }
            IconButton(onClick = onShareClick) { Icon(imageVector = Icons.Default.Share, contentDescription=null) }
            IconButton(onClick = onSettingsClick){ Icon(imageVector = Icons.Default.Settings, contentDescription=null) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Blue,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
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
    fun onShareClick(){ navigateWithState(Routes.Home.route) }
    fun onSettingsClick(){ navigateWithState(Routes.Home.route) }
    TopAppBarView(
        onShareClick = { onShareClick() },
        onInfoClick = { onInfoClick() },
        onSettingsClick = { onSettingsClick() })
}