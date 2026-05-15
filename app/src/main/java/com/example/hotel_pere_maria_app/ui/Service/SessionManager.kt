package com.example.hotel_pere_maria_app.ui.Service

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.json.JSONObject

// Almacena la información del usuario logueado en memoria (+ persistencia para autologin)
data class UserInfo(
        val user_id: String,
        val email: String,
        val name: String,
        val surname: String = "",
        val role: String,
        val dni: String = "",
        val birthDate: String? = null,
        val city: String? = null,
        val gender: String = "Other",
        val profileImage: String? = null,
        val isVIP: Boolean = false,
        val discount: Double = 0.0,
        val isActive: Boolean = true
)

object SessionManager {
    private const val PREFS = "hotel_pere_maria_session"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_JSON = "user_json"

    private val unauthorizedLock = Any()

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    var userToken: String? = null
    var userInfo: UserInfo? = null

    /** Lo asigna el NavHost (pantalla principal): vuelve a login si la API indica JWT inválido/caducado. */
    var onSessionExpired: (() -> Unit)? = null

    /**
     * Respuesta API típica de JWT caducado: **403** `{ "error": "Token expirado" }` (ver `authMiddleware.js`).
     * 401 sin credenciales de login válidas también cierra sesión.
     */
    fun shouldLogoutForApiError(code: Int, errorBody: String?): Boolean {
        val b = errorBody.orEmpty()
        if (code == 403) {
            return try {
                val e = JSONObject(b).optString("error")
                e == "Token expirado" || e == "Token inválido"
            } catch (_: Exception) {
                false
            }
        }
        if (code == 401) {
            if (b.contains("Credenciales inválidas", ignoreCase = true)) return false
            if (b.contains("token no proporcionado", ignoreCase = true)) return true
            return try {
                val e = JSONObject(b).optString("error")
                e.contains("token", ignoreCase = true)
            } catch (_: Exception) {
                false
            }
        }
        return false
    }

    /** JWT inválido o caducado: limpia sesión y navega a login si hay callback registrado. */
    fun handleUnauthorized() {
        synchronized(unauthorizedLock) {
            if (userToken == null && userInfo == null) return
            clear()
        }
        onSessionExpired?.invoke()
    }

    /** Recupera token y usuario guardados (autologin). Llamar tras init(). */
    fun restoreSession(): Boolean {
        val p = prefs ?: return false
        val t = p.getString(KEY_TOKEN, null) ?: return false
        val json = p.getString(KEY_USER_JSON, null) ?: return false
        return try {
            userToken = t
            userInfo = gson.fromJson(json, UserInfo::class.java)
            true
        } catch (_: Exception) {
            clear()
            false
        }
    }

    /** Guarda sesión en disco tras login correcto. */
    fun saveSession() {
        val p = prefs ?: return
        val ed = p.edit()
        userToken?.let { ed.putString(KEY_TOKEN, it) } ?: ed.remove(KEY_TOKEN)
        userInfo?.let { ed.putString(KEY_USER_JSON, gson.toJson(it)) } ?: ed.remove(KEY_USER_JSON)
        ed.apply()
    }

    /** Cierra sesión: borra memoria y preferencias. */
    fun clear() {
        userToken = null
        userInfo = null
        prefs?.edit()?.remove(KEY_TOKEN)?.remove(KEY_USER_JSON)?.apply()
    }
}
