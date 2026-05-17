package com.example.hotel_pere_maria_app.core.navigation



import androidx.navigation.NavHostController



/**

 * Vuelve a la pantalla de búsqueda principal (`booking/home`).

 *

 * Usa `popUpTo` con la misma ruta e `inclusive = true` para vaciar el back stack hasta el inicio

 * y volver a apilar **Inicio** limpio. Evita el patrón `findStartDestination + restoreState`, que en

 * algunos casos dejaba la navegación sin efecto al pulsar "Inicio" (sobre todo si el ítem ya

 * estaba seleccionado en la bottom bar).

 */

fun NavHostController.navigateToBookingHome() {

    val r = Routes.BookingHome.route

    navigate(r) {

        popUpTo(r) { inclusive = true }

        launchSingleTop = true

    }

}

