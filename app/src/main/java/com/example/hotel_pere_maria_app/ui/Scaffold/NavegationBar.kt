package com.example.hotel_pere_maria_app.ui.Scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hotel_pere_maria_app.ui.Navegation.Navitems
import com.example.hotel_pere_maria_app.ui.Navegation.Routes
import com.example.ui.theme.AppTheme

@Composable
fun NavigationBarView(items: List<Navitems>, currentRoute : String, onItemSelected: (String) -> Unit){
    NavigationBar(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        for(item in items){
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.name) },
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {onItemSelected(item.route)},
                alwaysShowLabel = false,
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            )
        }

    }
}

@Composable
fun NavigationBarState(navController : NavHostController) {
    val items = listOf<Navitems>(
        Navitems("ADD", Icons.Default.Add, Routes.Add.route),
        Navitems("Home", Icons.Default.Home, Routes.Home.route),
        Navitems("Profile", Icons.Default.AccountCircle, Routes.User.route)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: items[1].route

    fun onItemSelected(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
   NavigationBarView(items = items, currentRoute = currentRoute, onItemSelected = {route:String -> onItemSelected(route)})

}

@Preview(showSystemUi = true)
@Composable
fun NavegationPreview(){
    AppTheme(dynamicColor = false, darkTheme = false) {
        ScaffoldMain()
    }
}