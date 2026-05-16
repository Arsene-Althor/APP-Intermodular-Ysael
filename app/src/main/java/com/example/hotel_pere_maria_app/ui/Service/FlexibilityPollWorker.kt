package com.example.hotel_pere_maria_app.ui.Service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.hotel_pere_maria_app.ui.Models.ReservationRepository
import java.util.concurrent.TimeUnit

/** Consulta reservas y dispara notificación local si cambió el estado P19. */
class FlexibilityPollWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (SessionManager.userToken.isNullOrBlank()) return Result.success()
        ReservationRepository.fetchReservations()
        FlexibilityNotificationHelper.checkStatusChanges(
            applicationContext,
            ReservationRepository.reservations.value,
        )
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "flexibility_status_poll"

        fun schedule(context: Context) {
            if (SessionManager.userToken.isNullOrBlank()) return
            val request =
                PeriodicWorkRequestBuilder<FlexibilityPollWorker>(5, TimeUnit.MINUTES)
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /** Consulta inmediata tras enviar solicitud (complementa el poll cada 15 min). */
        fun runOnce(context: Context) {
            if (SessionManager.userToken.isNullOrBlank()) return
            val request = OneTimeWorkRequestBuilder<FlexibilityPollWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
