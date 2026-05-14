package com.example.hotel_pere_maria_app.ui.Navegation

sealed class Routes(val route: String) {

    object Login : Routes("Login")
    object Register : Routes("Register")
    object Scaffold : Routes("Scaffold")
    /** Entrada principal: motor de búsqueda estilo Booking. */
    object BookingHome : Routes("booking/home")
    object BookingResults : Routes("booking/results")
    object BookingConfirm : Routes("booking/confirm/{roomId}") {
        fun createRoute(roomId: String) = "booking/confirm/$roomId"
    }

    object User : Routes("User")
    object ForgotPassword : Routes("ForgotPassword")
    object ModReserva : Routes("ModReserva")
    /** Mis reseñas (acceso desde Perfil / Ajustes). */
    object Reviews : Routes("Reviews")
    /** Reservas activas + acceso a historial. */
    object Reservations : Routes("Reservations")
    /** Lista de reservas con factura (GET /invoices?userId=). */
    object InvoiceHistory : Routes("InvoiceHistory")

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
