package com.example.hotel_pere_maria_app.core.util

import com.example.hotel_pere_maria_app.BuildConfig

/** Rutas relativas de la API (`uploads/...`) → URL absoluta con [BuildConfig.API_BASE_URL]. */
object MediaUrls {
    fun resolve(path: String?): String? {
        val p = path?.trim().orEmpty()
        if (p.isEmpty()) return null
        if (p.startsWith("http://", ignoreCase = true) || p.startsWith("https://", ignoreCase = true)) {
            return p
        }
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val rel = p.trimStart('/')
        return "$base/$rel"
    }

    fun profileImage(path: String?): String? = resolve(path)
}

