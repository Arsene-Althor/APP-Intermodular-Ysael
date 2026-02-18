package com.example.hotel_pere_maria_app.ui.Service

import com.example.hotel_pere_maria_app.ui.Models.RegisterRequest
import com.example.hotel_pere_maria_app.ui.Models.RegisterResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Interfaz que define las llamadas a la API relacionadas con usuarios
interface UserService {

        // Envía los datos de registro al servidor (POST /user/register)
        @POST("user/register")
        suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

        // Modifica los datos de un usuario existente (PATCH /user/modify/{userId})
        // Se usa PATCH porque solo actualizamos los campos que han cambiado
        @PATCH("user/modify/{userId}")
        suspend fun modifyUser(
                @Path("userId") userId: String,
                @Body
                updates: Map<String, String?> // Mapa con clave-valor de los campos a actualizar
        ): Response<RegisterResponse>

        // Sube la foto de perfil usando multipart (PATCH /user/modify/{userId})
        // El backend espera un campo 'profileImage' con el archivo
        @Multipart
        @PATCH("user/modify/{userId}")
        suspend fun uploadProfileImage(
                @Path("userId") userId: String,
                @Part image: MultipartBody.Part
        ): Response<RegisterResponse>

        // Desactiva la cuenta del usuario logueado (PATCH /user/deactivate)
        @PATCH("user/deactivate") suspend fun deactivateAccount(): Response<Map<String, String>>
}
