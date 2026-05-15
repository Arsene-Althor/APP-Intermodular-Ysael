package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.BookingAuditEntry
import com.example.hotel_pere_maria_app.ui.Models.InvoicesByUserResponse
import com.example.hotel_pere_maria_app.ui.Models.Reservation
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReservationService{
    @GET("reservation/mine")
    suspend fun getMine(): Response<List<Reservation>>

    /** Historial de auditoría (solo lectura). El cliente usa [BookingHistoryFriendlyMapper] para el texto. */
    @GET("reservation/{reservation_id}/audit")
    suspend fun getBookingAudit(
        @Path("reservation_id") reservationId: String,
    ): Response<List<BookingAuditEntry>>

    @POST("reservation/getPrice")
    suspend fun getPrice(@Body datos : Map<String, String>): Response<Map<String, Any>>

    @POST("reservation/add")
    suspend fun addReservation(@Body datos: Map<String, String>): Response<Map<String, String>>

    @DELETE("reservation/cancel/{reservation_id}")
    suspend fun cancelReservation(
        @Path("reservation_id") reservationId: String,
        @Query("price") price: Double
    ): Response<Map<String, Any>>

    @POST("reservation/getCancelationPrice")
    suspend fun cancelationPrice(@Body datos: Map<String, String>): Response<Map<String, String>>

    @PATCH("reservation/update")
    suspend fun updateReservation(@Body datos : Map<String, String>): Response<Map<String, Any>>

    /** PDF factura fiscal (binario). Requiere checkout en recepción. */
    @Streaming
    @GET("reservation/{reservation_id}/invoice")
    suspend fun downloadInvoicePdf(
        @Path("reservation_id") reservationId: String,
    ): Response<ResponseBody>

    /** Justificante de reserva / pago simulado (PDF no fiscal). Siempre que seas dueño o personal. */
    @Streaming
    @GET("reservation/{reservation_id}/booking-receipt")
    suspend fun downloadBookingReceiptPdf(
        @Path("reservation_id") reservationId: String,
    ): Response<ResponseBody>

    /** Listado de reservas con factura emitida para un usuario. */
    @GET("invoices")
    suspend fun getInvoicesByUser(
        @Query("userId") userId: String,
    ): Response<InvoicesByUserResponse>
}