package com.example.hotel_pere_maria_app.ui.Service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object InvoicePdfHelper {
    sealed class Result {
        data object Ok : Result()

        data class Error(val message: String) : Result()

        data object NoPdfViewer : Result()
    }

    /**
     * Descarga GET /reservation/{id}/invoice y abre el PDF (visor externo).
     * Debe llamarse desde una corrutina (hace red + disco en IO).
     */
    suspend fun downloadAndOpenPdf(
        context: Context,
        reservationId: String,
    ): Result =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.reservationService.downloadInvoicePdf(reservationId)
                if (!response.isSuccessful) {
                    val errBody = response.errorBody()?.string()?.take(500)
                    return@withContext Result.Error(errBody ?: "Error HTTP ${response.code()}")
                }
                val body = response.body() ?: return@withContext Result.Error("Respuesta vacía")
                val bytes =
                    body.byteStream().use { input ->
                        input.readBytes()
                    }
                if (bytes.size < 4 || !bytes.copyOfRange(0, 4).contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46))) {
                    return@withContext Result.Error("La respuesta no parece un PDF")
                }
                val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
                val safeId = reservationId.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val file = File(dir, "Factura-$safeId.pdf")
                file.writeBytes(bytes)

                withContext(Dispatchers.Main) {
                    try {
                        val appCtx = context.applicationContext
                        val uri =
                            FileProvider.getUriForFile(
                                appCtx,
                                "${appCtx.packageName}.fileprovider",
                                file,
                            )
                        val viewIntent =
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        val chooser =
                            Intent.createChooser(viewIntent, "Abrir factura").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        appCtx.startActivity(chooser)
                        Result.Ok
                    } catch (_: ActivityNotFoundException) {
                        Result.NoPdfViewer
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "No se pudo abrir el visor")
                    }
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de red")
            }
        }
}
