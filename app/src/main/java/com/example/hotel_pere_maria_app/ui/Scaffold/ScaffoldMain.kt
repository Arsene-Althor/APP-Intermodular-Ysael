package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hotel_pere_maria_app.ui.Navegation.NavigationScaffold

@Composable
fun ScaffoldMain(){
    val ScaffoldnavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState ) },
        bottomBar = {NavigationBarState(ScaffoldnavController)},
        topBar = {TopAppBarState(ScaffoldnavController )}
    ) { innerpadding ->
        NavigationScaffold(ScaffoldnavController, modifier = Modifier.padding(innerpadding), snackbarHostState )
    }
}