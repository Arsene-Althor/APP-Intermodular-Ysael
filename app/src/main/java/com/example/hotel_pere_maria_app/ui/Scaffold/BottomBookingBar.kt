package com.example.hotel_pere_maria_app.ui.Scaffold



import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.RowScope

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.layout.navigationBarsPadding

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Event

import androidx.compose.material.icons.filled.Home

import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.unit.dp

import androidx.navigation.NavHostController

import com.example.hotel_pere_maria_app.ui.Navegation.Routes

import com.example.hotel_pere_maria_app.ui.Navegation.navigateToBookingHome



/**

 * Barra inferior con [clickable] en cada pestaña (no [NavigationBarItem]), para que **cada toque**

 * ejecute la navegación aunque la pestaña ya esté visualmente seleccionada.

 */

@Composable

fun BottomBookingBar(navController: NavHostController, currentRoute: String?) {

    val homeBranchSelected =

        currentRoute == Routes.BookingHome.route ||

            currentRoute == Routes.BookingResults.route ||

            currentRoute?.startsWith("booking/confirm/") == true



    Column(

        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),

    ) {

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(

            modifier =

                Modifier.fillMaxWidth()

                    .heightIn(min = 56.dp)

                    .background(MaterialTheme.colorScheme.surface),

            verticalAlignment = Alignment.CenterVertically,

        ) {

            BottomTab(

                selected = homeBranchSelected,

                onClick = { navController.navigateToBookingHome() },

                icon = Icons.Default.Home,

                label = "Inicio",

            )

            BottomTab(

                selected = currentRoute == Routes.Reservations.route,

                onClick = {

                    navController.navigate(Routes.Reservations.route) { launchSingleTop = true }

                },

                icon = Icons.Default.Event,

                label = "Reservas",

            )

        }

    }

}



@Composable

private fun RowScope.BottomTab(

    selected: Boolean,

    onClick: () -> Unit,

    icon: ImageVector,

    label: String,

) {

    val tint =

        if (selected) MaterialTheme.colorScheme.primary

        else MaterialTheme.colorScheme.onSurfaceVariant



    Column(

        modifier =

            Modifier.weight(1f)

                .clickable(onClick = onClick)

                .padding(vertical = 8.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),

    ) {

        Icon(

            imageVector = icon,

            contentDescription = label,

            tint = tint,

            modifier = Modifier.size(26.dp),

        )

        Text(

            text = label,

            style = MaterialTheme.typography.labelMedium,

            color = tint,

        )

    }

}

