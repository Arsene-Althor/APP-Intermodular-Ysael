package com.example.hotel_pere_maria_app.ui.Navegation

sealed class Routes(val route: String) {

    object Login : Routes("Login")
    object Register : Routes("Register")
    object Scaffold : Routes("Scaffold")
    object Home : Routes("Home")
    object SearchResults : Routes("SearchResults")
    object MyAccount : Routes("MyAccount")
    object Add : Routes("Add")
    object User : Routes("User")
    object ForgotPassword : Routes("ForgotPassword")
    object ModReserva : Routes("ModReserva")
    object RoomList : Routes("RoomList")
    /** Mis reseñas (acceso desde Perfil / Ajustes). */
    object Reviews : Routes("Reviews")
    /** Reservas activas + acceso a historial. */
    object Reservations : Routes("Reservations")
    /** Lista completa de reservas (pasadas y canceladas). */
    object ReservationHistory : Routes("ReservationHistory")
    /** Historial amigable de auditoría de una reserva. */
    object ReservationAudit : Routes("ReservationAudit/{reservationId}") {
        fun createRoute(reservationId: String) = "ReservationAudit/$reservationId"
    }
    object RoomDetail : Routes("RoomDetail/{roomId}") {
        fun createRoute(roomId: String) = "RoomDetail/$roomId"
    }
}
