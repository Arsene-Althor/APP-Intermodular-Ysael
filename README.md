# APP Android — Hotel Pere María

Aplicación móvil Android desarrollada con **Kotlin** y **Jetpack Compose** para los clientes del Hotel Pere María. Permite consultar habitaciones, gestionar reservas, escribir reseñas y administrar el perfil de usuario. Se comunica con la API REST del proyecto intermodular mediante **Retrofit**.

---

## Tabla de contenidos

- [Requisitos](#requisitos)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura](#arquitectura)
- [Conexión con la API](#conexión-con-la-api)
- [Gestión de sesión](#gestión-de-sesión)
- [Módulos principales](#módulos-principales)
- [Cambios recientes](#cambios-recientes)

---

## Requisitos

- Android Studio (Hedgehog o superior)
- SDK mínimo: API 24 (Android 7.0)
- Conexión a la API del proyecto (`API-Intermodular-Ysael`)

La URL base de la API se configura en `BuildConfig.API_BASE_URL` desde el archivo `build.gradle.kts`.

---

## Tecnologías utilizadas

| Tecnología         | Uso                                               |
|--------------------|----------------------------------------------------|
| Kotlin             | Lenguaje principal                                 |
| Jetpack Compose    | Interfaz de usuario declarativa                    |
| Retrofit + Gson    | Cliente HTTP y serialización JSON                  |
| OkHttp             | Interceptores para autenticación y logging         |
| Coroutines + Flow  | Programación asíncrona y estado reactivo           |
| SharedPreferences  | Persistencia local de sesión (autologin)           |
| Navigation Compose | Navegación entre pantallas                         |

---

## Estructura del proyecto

```
app/src/main/java/com/example/hotel_pere_maria_app/
├── HotelApplication.kt                  # Clase Application (inicialización global)
│
└── ui/
    ├── MainActivity.kt                  # Actividad principal (punto de entrada)
    │
    ├── Models/                          # Data classes y repositorios
    │   ├── LoginRequest.kt              # Petición de login
    │   ├── LoginResponse.kt             # Respuesta de login (token + usuario)
    │   ├── RegisterRequest.kt           # Petición de registro
    │   ├── Reservation.kt               # Modelo de reserva + repositorio
    │   ├── Review.kt                    # Modelo de reseña + ReviewRepository
    │   ├── Room.kt                      # Modelo de habitación (isOperational, isOccupiedNow)
    │   ├── RoomRepository.kt            # Repositorio de habitaciones (cache + fallback)
    │   └── User.kt                      # Modelo de usuario
    │
    ├── Service/                         # Interfaces Retrofit y utilidades
    │   ├── RetrofitClient.kt            # Configuración centralizada de Retrofit
    │   ├── AuthService.kt               # Endpoints de autenticación
    │   ├── ReservationService.kt        # Endpoints de reservas
    │   ├── RoomService.kt               # Endpoints de habitaciones
    │   ├── ReviewService.kt             # Endpoints de reseñas
    │   ├── UserService.kt               # Endpoints de usuarios
    │   ├── SessionManager.kt            # Gestión de sesión y autologin
    │   └── ThemeManager.kt              # Preferencias de tema
    │
    ├── ViewModels/                      # Lógica de presentación (MVVM)
    │   ├── LoginViewModel.kt
    │   ├── RegisterViewModel.kt
    │   ├── HomeViewModel.kt
    │   ├── RoomViewModel.kt
    │   ├── AddViewModel.kt              # Crear reserva
    │   ├── ModReservaViewModel.kt       # Modificar reserva
    │   ├── ReviewViewModel.kt           # Reseñas de una habitación
    │   ├── MyReviewsViewModel.kt        # Reseñas globales del usuario
    │   ├── ProfileViewModel.kt
    │   └── ForgotPasswordViewModel.kt
    │
    ├── Views/                           # Pantallas Compose
    │   ├── Login.kt / Register.kt
    │   ├── Home.kt                      # Dashboard con reservas del usuario
    │   ├── RoomList.kt                  # Listado de habitaciones
    │   ├── RoomDetail.kt                # Detalle + reseñas
    │   ├── Reviews.kt                   # Reseñas del usuario
    │   ├── Add.kt / ModReserva.kt       # Crear / editar reserva
    │   ├── Profile.kt
    │   ├── ForgotPassword.kt
    │   └── Components.kt                # Componentes reutilizables
    │
    ├── Components/
    │   └── RoomSelectionDialog.kt       # Diálogo de selección de habitación
    │
    ├── Navegation/                      # Navegación
    │   ├── Routes.kt                    # Definición de rutas
    │   ├── Navegation.kt                # Grafo de navegación
    │   └── NavegationMain.kt
    │
    ├── Scaffold/                        # Componentes de layout
    │
    └── theme/                           # Tema visual de la aplicación
```

---

## Arquitectura

La aplicación sigue el patrón **MVVM** (Model-View-ViewModel):

```
View (Compose) → ViewModel → Repository → Service (Retrofit) → API REST
```

- **View**: pantallas declarativas con Jetpack Compose que observan `StateFlow`.
- **ViewModel**: gestiona la lógica de negocio y expone estados reactivos.
- **Repository**: centraliza las llamadas a la API y mantiene el estado en memoria con `MutableStateFlow`.
- **Service**: interfaces Retrofit que definen los endpoints HTTP.

---

## Conexión con la API

### `RetrofitClient.kt`

Punto central de configuración HTTP. Incluye un interceptor que añade automáticamente el token JWT a cada petición:

```kotlin
object RetrofitClient {
    private val BASE_URL: String = BuildConfig.API_BASE_URL

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        SessionManager.userToken?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(request.build())
    }

    // Servicios disponibles
    val reservationService: ReservationService by lazy { retrofit.create(...) }
    val roomService: RoomService by lazy { retrofit.create(...) }
    val reviewService: ReviewService by lazy { retrofit.create(...) }
    val authService: AuthService by lazy { retrofit.create(...) }
    val userService: UserService by lazy { retrofit.create(...) }
}
```

### Endpoints consumidos

#### Reservas (`ReservationService.kt`)

```kotlin
interface ReservationService {
    @GET("reservation/mine")
    suspend fun getMine(): Response<List<Reservation>>

    @POST("reservation/add")
    suspend fun addReservation(@Body datos: Map<String, String>): Response<Map<String, String>>

    @DELETE("reservation/cancel/{reservation_id}")
    suspend fun cancelReservation(
        @Path("reservation_id") reservationId: String,
        @Query("price") price: Double
    ): Response<Map<String, Any>>

    @PATCH("reservation/update")
    suspend fun updateReservation(@Body datos: Map<String, String>): Response<Map<String, Any>>
}
```

#### Habitaciones (`RoomService.kt`)

```kotlin
interface RoomService {
    @GET("room/all")
    suspend fun getAllRooms(): Response<List<Room>>

    @GET("room/one")
    suspend fun getRoomById(@Query("id") roomId: String): Response<Room>

    @GET("room/available")
    suspend fun getAvailableRoomsByDates(
        @Query("check_in") checkIn: String,
        @Query("check_out") checkOut: String
    ): Response<List<Room>>
}
```

#### Reseñas (`ReviewService.kt`)

```kotlin
interface ReviewService {
    @GET("review/mine")
    suspend fun getMyReviews(): Response<List<Review>>

    @GET("review/room/{roomId}")
    suspend fun getReviewsByRoom(@Path("roomId") roomId: String): Response<List<Review>>

    @POST("review/create")
    suspend fun createReview(@Body body: Map<String, String>): Response<Map<String, String>>

    @HTTP(method = "DELETE", path = "review/delete", hasBody = true)
    suspend fun deleteReview(@Body body: Map<String, String>): Response<Map<String, String>>
}
```

> Se utiliza `@HTTP(method = "DELETE", hasBody = true)` porque Retrofit no permite body en `@DELETE` por defecto.

---

## Gestión de sesión

### `SessionManager.kt`

Almacena el token JWT y la información del usuario en memoria y en `SharedPreferences` (autologin):

```kotlin
object SessionManager {
    var userToken: String? = null
    var userInfo: UserInfo? = null

    fun restoreSession(): Boolean { /* recupera token y usuario de SharedPreferences */ }
    fun saveSession() { /* persiste tras login exitoso */ }
    fun clear() { /* cierra sesión: limpia memoria y preferencias */ }
}
```

---

## Módulos principales

### Habitaciones

#### Modelo — `Room.kt`

```kotlin
data class Room(
    val room_id: String,
    val type: String,
    val description: String,
    val image: String,
    val price_per_night: Double,
    val max_occupancy: Int,
    @SerializedName("is_operational") val isOperational: Boolean = true,
    @SerializedName("is_occupied_now") val isOccupiedNow: Boolean = false,
    val isAvailable: Boolean = true  // legacy
) {
    /** Libre para reservar ahora (en servicio y sin huésped en curso). */
    fun isFreeNow(): Boolean = isOperational && !isOccupiedNow
}
```

- **`isOperational`**: indica si la habitación está en servicio. La API excluye las habitaciones fuera de servicio de las búsquedas de disponibilidad.
- **`isOccupiedNow`**: campo calculado por la API que indica si hay una reserva activa en este momento.
- **`isFreeNow()`**: función de utilidad que combina ambos campos para determinar si la habitación está realmente libre.

#### Repositorio — `RoomRepository.kt`

Centraliza la obtención de habitaciones con caché en memoria y fallback automático:

```kotlin
object RoomRepository {
    val rooms: StateFlow<List<Room>>              // Todas las habitaciones
    val availableRooms: StateFlow<List<Room>>     // Habitaciones disponibles por fechas

    suspend fun fetchRooms()                      // GET /room/all
    suspend fun getRoomById(roomId: String): Room? // Busca en caché, recarga si no está
    suspend fun fetchAvailableRoomsByDates(checkIn: String, checkOut: String) // GET /room/available
}
```

Si `GET /room/available` falla, el repositorio ejecuta un **fallback** que carga todas las habitaciones con `GET /room/all` y filtra localmente por `isOperational` y `isFreeNow()`.

### Reseñas

#### `ReviewRepository` — Estado reactivo global

```kotlin
object ReviewRepository {
    val reviews: StateFlow<List<Review>>       // Reseñas de una habitación
    val myReviews: StateFlow<List<Review>>     // Reseñas del usuario logueado

    suspend fun fetchReviewsByRoom(roomId: String)
    suspend fun fetchMyReviews()
    suspend fun createReview(roomId: String, rating: Int, comment: String): Result<String>
    suspend fun deleteReview(reviewId: String, roomId: String): Result<String>
}
```

#### `ReviewViewModel` — Reseñas de una habitación

Gestiona la lógica de reseñas en el contexto de una habitación específica: verifica que el usuario tenga una reserva previa, detecta si ya existe una reseña propia, y controla el formulario.

#### `MyReviewsViewModel` — Reseñas globales del usuario

ViewModel dedicado a la pestaña de reseñas del usuario. Consume `ReviewRepository.myReviews`:

```kotlin
class MyReviewsViewModel : ViewModel() {
    val myReviews = ReviewRepository.myReviews
    val myReviewsLoading = ReviewRepository.myReviewsLoading

    fun refresh() {
        viewModelScope.launch { ReviewRepository.fetchMyReviews() }
    }
}
```

### Navegación — `Routes.kt`

```kotlin
sealed class Routes(val route: String) {
    object Login : Routes("Login")
    object Register : Routes("Register")
    object Home : Routes("Home")
    object RoomList : Routes("RoomList")
    object RoomDetail : Routes("RoomDetail/{roomId}") {
        fun createRoute(roomId: String) = "RoomDetail/$roomId"
    }
    object Reviews : Routes("Reviews")
    object Add : Routes("Add")
    object ModReserva : Routes("ModReserva")
    object User : Routes("User")
    object ForgotPassword : Routes("ForgotPassword")
}
```

---

## Cambios recientes

### Habitaciones — `isOperational` e `isOccupiedNow`

- El modelo `Room.kt` incorpora los nuevos campos `isOperational` e `isOccupiedNow` (mapeados con `@SerializedName` desde `is_operational` e `is_occupied_now`).
- Se añadió la función `isFreeNow()` que combina ambos campos.
- `RoomRepository.kt` ahora utiliza estos campos en el filtrado de habitaciones disponibles.

### Nuevo `RoomRepository.kt`

- Repositorio centralizado con caché en `StateFlow`, búsqueda por ID con fallback, y obtención de habitaciones disponibles por rango de fechas con fallback automático.

### Nuevo `MyReviewsViewModel.kt`

- ViewModel independiente para la pestaña global de reseñas del usuario, separado del `ReviewViewModel` que opera en el contexto de una habitación.

### `HotelApplication.kt` movido

- La clase `Application` se reubicó a la raíz del paquete principal para seguir las convenciones de Android.

### Correcciones previas en reseñas

- Ruta `GET /review/room/:roomId` corregida.
- Creación (`POST /review/create`): `user_name` resuelto por la API.
- Eliminación (`DELETE /review/delete`): implementada con `@HTTP(method = "DELETE", hasBody = true)`.

### Refactorización de verbos HTTP

- Cancelación: `DELETE /cancel/:reservation_id?price=X`.
- Actualización: `PATCH /update`.

---
