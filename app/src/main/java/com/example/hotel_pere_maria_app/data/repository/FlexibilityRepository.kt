package com.example.hotel_pere_maria_app.data.repository

import android.util.Log
import com.example.hotel_pere_maria_app.HotelApplication
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityNotificationHelper
import com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityPollWorker
import com.example.hotel_pere_maria_app.core.network.RetrofitClient
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.data.model.FlexibilityKind
import com.example.hotel_pere_maria_app.data.model.FlexibilityStatusResponse
import com.example.hotel_pere_maria_app.data.model.Reservation
import com.example.hotel_pere_maria_app.core.util.parseApiError
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FlexibilityRepository {
    private const val TAG = "FlexibilityRepository"

    suspend fun getStatus(reservationId: String): Result<FlexibilityStatusResponse> {
        return try {
            val response = RetrofitClient.flexibilityService.getStatus(reservationId)
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(response.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                return Result.failure(Exception(parseApiError(err) ?: "Error ${response.code()}"))
            }
            val body = response.body()
                ?: return Result.failure(Exception("Respuesta vacía"))
            Result.success(body)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            Result.failure(e)
        }
    }

    suspend fun submitRequest(
        reservation: Reservation,
        kind: FlexibilityKind,
        hour: Int,
        minute: Int,
    ): Result<String> {
        val baseDate = if (kind == FlexibilityKind.EARLY) reservation.check_in else reservation.check_out
        val iso = buildRequestedTimeIso(baseDate, hour, minute)
        val body =
            buildMap {
                put("requested_time", iso)
                if (kind == FlexibilityKind.LATE_FACILITIES) put("mode", "facilities")
            }
        return try {
            val response =
                when (kind) {
                    FlexibilityKind.EARLY ->
                        RetrofitClient.flexibilityService.requestEarlyCheckin(
                            reservation.reservation_id,
                            body,
                        )
                    FlexibilityKind.LATE, FlexibilityKind.LATE_FACILITIES ->
                        RetrofitClient.flexibilityService.requestLateCheckout(
                            reservation.reservation_id,
                            body,
                        )
                }
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(response.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                return Result.failure(Exception(parseApiError(err) ?: "Error ${response.code()}"))
            }
            val msg =
                (response.body()?.get("mensaje") as? String)
                    ?: "Solicitud enviada"
            notifyFlexibilityAfterRefresh()
            Result.success(msg)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            Result.failure(e)
        }
    }

    suspend fun extendStay(reservationId: String, newCheckOutIso: String): Result<String> {
        return try {
            val response =
                RetrofitClient.flexibilityService.extendStay(
                    reservationId,
                    mapOf("new_check_out" to newCheckOutIso),
                )
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string().orEmpty()
                if (SessionManager.shouldLogoutForApiError(response.code(), err)) {
                    SessionManager.handleUnauthorized()
                }
                return Result.failure(Exception(parseApiError(err) ?: "Error ${response.code()}"))
            }
            val body = response.body()
            val msg = body?.mensaje?.takeIf { it.isNotBlank() } ?: "Estancia ampliada"
            notifyFlexibilityAfterRefresh()
            Result.success(msg)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            Result.failure(e)
        }
    }

    private suspend fun notifyFlexibilityAfterRefresh() {
        ReservationRepository.fetchReservations()
        val ctx = HotelApplication.appContext ?: return
        FlexibilityNotificationHelper.checkStatusChanges(
            ctx,
            ReservationRepository.reservations.value,
        )
        FlexibilityPollWorker.runOnce(ctx)
    }

    private fun buildRequestedTimeIso(day: Date, hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            time = day
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).format(cal.time)
    }

}

/** Reserva visible y usable en la app cliente. */
fun Reservation.isActiveForClient(): Boolean =
    cancelation_date == null && superseded_by_reservation_id.isNullOrBlank()

fun Reservation.isEligibleForFlexRequest(): Boolean = isActiveForClient()

fun Reservation.canRequestEarly(): Boolean =
    isEligibleForFlexRequest() &&
        (early_checkin_requested?.canSubmitNew() != false) &&
        !check_in.before(startOfToday())

fun Reservation.isCheckOutDayToday(): Boolean {
    val today = Calendar.getInstance()
    val out = Calendar.getInstance().apply { time = check_out }
    return sameCalendarDay(today, out)
}

/** Salida estándar del hotel: 11:00 del día de check_out. */
fun Reservation.standardCheckoutCalendar(): Calendar =
    Calendar.getInstance().apply {
        time = check_out
        set(Calendar.HOUR_OF_DAY, 11)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

/** Cliente: máx. 12 h desde las 11:00 del día de salida para late / ampliación corta. */
fun Reservation.isWithinClientFlexRequestWindow(windowHours: Int = 12): Boolean {
    val deadline =
        standardCheckoutCalendar().apply { add(Calendar.HOUR_OF_DAY, windowHours) }
    return !Calendar.getInstance().after(deadline)
}

/** Hora de salida estándar ya pasó (fin de estancia en habitación). */
fun Reservation.isPastStandardCheckout(): Boolean {
    val now = Calendar.getInstance()
    return now.after(standardCheckoutCalendar())
}

/** Mostrar decisión fin de estancia: ampliar o instalaciones. */
fun Reservation.needsEndOfStayChoice(): Boolean =
    isEligibleForFlexRequest() &&
        isPastStandardCheckout() &&
        isWithinClientFlexRequestWindow() &&
        (late_checkout_requested?.canSubmitNew() != false)

/** Instalaciones (sin habitación), tras fin de estancia o mismo día de salida. */
fun Reservation.canRequestLateFacilities(): Boolean =
    isEligibleForFlexRequest() &&
        (late_checkout_requested?.canSubmitNew() != false) &&
        (isPastStandardCheckout() || isCheckOutDayToday()) &&
        isWithinClientFlexRequestWindow()

/** Ampliar noches / nueva fecha de salida. */
fun Reservation.canExtendStayNights(): Boolean =
    isEligibleForFlexRequest() &&
        !check_out.before(startOfToday())

private fun sameCalendarDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun startOfToday(): Date {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

