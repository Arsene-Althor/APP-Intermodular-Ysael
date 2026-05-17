package com.example.hotel_pere_maria_app.core.network

import android.os.Handler
import android.os.Looper
import com.example.hotel_pere_maria_app.BuildConfig
import com.example.hotel_pere_maria_app.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val BASE_URL: String = BuildConfig.API_BASE_URL

    private val authInterceptor =
        Interceptor { chain ->
            val request = chain.request().newBuilder()
            SessionManager.userToken?.let { request.addHeader("Authorization", "Bearer ${it}") }
            chain.proceed(request.build())
        }

    /** Tras cada respuesta: JWT caducado viene como **403** en esta API, no solo 401. */
    private val sessionInvalidatingInterceptor =
        Interceptor { chain ->
            val req = chain.request()
            val path = req.url.encodedPath
            if (path.contains("auth/login", ignoreCase = true)) {
                return@Interceptor chain.proceed(req)
            }
            val response = chain.proceed(req)
            val code = response.code
            if (code != 401 && code != 403) return@Interceptor response
            val body = runCatching { response.peekBody(8192L).string() }.getOrDefault("")
            if (SessionManager.shouldLogoutForApiError(code, body)) {
                Handler(Looper.getMainLooper()).post {
                    SessionManager.handleUnauthorized()
                }
            }
            response
        }

    private val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(sessionInvalidatingInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            )
            .build()
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Creación de servicios
    val reservationService: ReservationService by lazy {
        retrofit.create(ReservationService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val roomService: RoomService by lazy {
        retrofit.create(RoomService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val reviewService: ReviewService by lazy {
        retrofit.create(ReviewService::class.java)
    }

    val flexibilityService: FlexibilityService by lazy {
        retrofit.create(FlexibilityService::class.java)
    }

    val loyaltyStatsService: LoyaltyStatsService by lazy {
        retrofit.create(LoyaltyStatsService::class.java)
    }

    val userStayService: UserStayService by lazy {
        retrofit.create(UserStayService::class.java)
    }
}

