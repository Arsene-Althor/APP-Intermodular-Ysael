package com.example.hotel_pere_maria_app.data.model

import java.util.Date

data class Reservation(
    val reservation_id: String,
    val room_id: String,
    val user_id: String,
    val check_in: Date,
    val check_out: Date,
    val price: Number,
    val cancelation_date: Date? = null,
    val createdBy: String,
    /** Asignado tras checkout en recepción (API). Si no es null, existe PDF en GET …/invoice. */
    val invoice_number: String? = null,
    val checkout_completed_at: Date? = null,
    val early_checkin_requested: FlexibilityRequestBlock? = null,
    val late_checkout_requested: FlexibilityRequestBlock? = null,
    val superseded_by_reservation_id: String? = null,
)

/** Fila de factura fiscal (GET /invoices o /reservation/invoices/history). */
data class HotelInvoiceItem(
    val invoice_number: String,
    val reservation_id: String,
    val user_id: String? = null,
    val room_id: String? = null,
    val type: String? = null,
    val type_label: String? = null,
    val amount: Number = 0,
    val description: String? = null,
    val issued_at: Date? = null,
    val check_in: Date? = null,
    val check_out: Date? = null,
)

/** Respuesta de `GET /invoices?userId=`. */
data class InvoicesByUserResponse(
    val user_id: String? = null,
    val count: Int = 0,
    val invoices: List<HotelInvoiceItem> = emptyList(),
    /** Compat API antigua. */
    val reservations: List<Reservation> = emptyList(),
)

