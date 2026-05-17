package com.example.hotel_pere_maria_app.feature.flexibility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hotel_pere_maria_app.R
import com.example.hotel_pere_maria_app.MainActivity
import com.example.hotel_pere_maria_app.data.model.FlexibilityRequestBlock
import com.example.hotel_pere_maria_app.core.session.SessionManager
import com.example.hotel_pere_maria_app.data.model.Reservation

/**
 * Notificaciones locales al cambiar estado de solicitud.
 * Canal actual: **solo en dispositivo** (WorkManager cada 5 min + tras acciones).
 * El servidor envía **email** vía SMTP ([flexibilityNotificationService.js]).
 * No hay FCM en este proyecto.
 */
object FlexibilityNotificationHelper {
    const val CHANNEL_ID = "flexibility_status"
    private const val PREFS = "flexibility_status_cache"
    private var notificationId = 4000

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Solicitudes especiales",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Check-in, check-out y ampliaciones"
                enableVibration(true)
            }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    fun checkStatusChanges(context: Context, reservations: List<Reservation>) {
        if (SessionManager.userToken.isNullOrBlank()) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var changed = false

        for (r in reservations) {
            if (processBlock(context, prefs, editor, r.reservation_id, "early", r.early_checkin_requested) == true) {
                changed = true
            }
            if (processBlock(context, prefs, editor, r.reservation_id, "late", r.late_checkout_requested) == true) {
                changed = true
            }
        }
        if (changed) editor.apply()
    }

    private fun processBlock(
        context: Context,
        prefs: android.content.SharedPreferences,
        editor: android.content.SharedPreferences.Editor,
        reservationId: String,
        suffix: String,
        block: FlexibilityRequestBlock?,
    ): Boolean? {
        val key = "${reservationId}_$suffix"
        val newStatus = block?.status
        val oldStatus = prefs.getString(key, null)

        if (newStatus != null) {
            val terminal = newStatus == "approved" || newStatus == "rejected"
            val statusChanged = oldStatus != newStatus
            if (statusChanged && terminal) {
                notifyStatusChange(context, reservationId, suffix, newStatus, block)
            } else if (oldStatus == null && terminal) {
                notifyStatusChange(context, reservationId, suffix, newStatus, block)
            }
            if (statusChanged || oldStatus == null) {
                editor.putString(key, newStatus)
                return true
            }
        } else if (oldStatus != null) {
            editor.remove(key)
            return true
        }
        return null
    }

    private fun notifyStatusChange(
        context: Context,
        reservationId: String,
        suffix: String,
        newStatus: String,
        block: FlexibilityRequestBlock?,
    ) {
        val tipo =
            when {
                suffix == "early" -> "entrada anticipada"
                block?.late_mode == "facilities" -> "instalaciones (sin habitación)"
                else -> "salida tardía"
            }
        val titulo =
            when (newStatus) {
                "approved" -> "Solicitud aprobada"
                "rejected" -> "Solicitud rechazada"
                else -> "Actualización"
            }
        val base = "Reserva $reservationId: $tipo ${statusLabel(newStatus)}."
        val nota = block?.review_note?.trim().orEmpty()
        val texto =
            if (newStatus == "rejected" && nota.isNotBlank()) {
                "$base Motivo: $nota"
            } else {
                base
            }
        showNotification(context, titulo, texto)
    }

    private fun statusLabel(status: String): String =
        when (status) {
            "approved" -> "aprobada"
            "rejected" -> "rechazada"
            "pending" -> "pendiente"
            else -> status
        }

    private fun showNotification(context: Context, title: String, text: String) {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending =
            PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (_: SecurityException) {
            // Sin permiso POST_NOTIFICATIONS
        }
    }
}

