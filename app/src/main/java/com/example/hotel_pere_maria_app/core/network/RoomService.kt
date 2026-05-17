package com.example.hotel_pere_maria_app.core.network

import com.example.hotel_pere_maria_app.data.model.ExtraService
import com.example.hotel_pere_maria_app.data.model.Room
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Servicio de Retrofit para gestionar las peticiones HTTP relacionadas con habitaciones */
interface RoomService {

    /**
     * Obtiene todas las habitaciones disponibles en el sistema
     * @return Response con lista de habitaciones
     */
    @GET("room/all") suspend fun getAllRooms(): Response<List<Room>>

    @GET("room/extra-services") suspend fun listExtraServices(): Response<List<ExtraService>>

    /**
     * Obtiene una habitación específica por su ID
     * @param roomId ID de la habitación a obtener
     * @return Response con los datos de la habitación
     */
    @GET("room/one")
    suspend fun getRoomById(@Query("id") roomId: String): Response<Room>
    
    /**
     * Obtiene habitaciones disponibles en un rango de fechas
     * @param checkIn Fecha de check-in (formato: dd/MM/yyyy)
     * @param checkOut Fecha de check-out (formato: dd/MM/yyyy)
     * @return Response con lista de habitaciones disponibles
     */
    @GET("room/available")
    suspend fun getAvailableRoomsByDates(
            @Query("checkIn") checkIn: String,
            @Query("checkOut") checkOut: String,
            @Query("guests") guests: Int? = null,
    ): Response<List<Room>>
}

