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
├── HotelApplication.kt                  # Clase Application (inicialización)
│
└── ui/
    ├── MainActivity.kt                  # Actividad principal (punto de entrada)
    │
    ├── Models/                          # Data classes y repositorios
    │   ├── LoginRequest.kt              # Petición de login
    │   ├── LoginResponse.kt             # Respuesta de login (token + usuario)
    │   ├── RegisterRequest.kt           # Petición de registro
    │   ├── Reservation.kt               # Modelo de reserva + repositorio
    │   ├── Review.kt                    # Modelo de reseña + repositorio
    │   ├── Room.kt                      # Modelo de habitación + repositorio
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
    │   ├── ReviewViewModel.kt           # Gestión de reseñas
    │   ├── ProfileViewModel.kt
    │   └── ForgotPasswordViewModel.kt
    │
    ├── Views/                           # Pantallas Compose
    │   ├── Login.kt
    │   ├── Register.kt
    │   ├── Home.kt                      # Pantalla principal con reservas
    │   ├── RoomList.kt                  # Listado de habitaciones
    │   ├── RoomDetail.kt                # Detalle de habitación + reseñas
    │   ├── Reviews.kt                   # Vista de reseñas del usuario
    │   ├── Add.kt                       # Formulario de nueva reserva
    │   ├── ModReserva.kt                # Edición de reserva
    │   ├── Profile.kt                   # Perfil de usuario
    │   └── ForgotPassword.kt
    │
    ├── Navegation/                      # Navegación
    │   ├── Routes.kt                    # Definición de rutas
    │   ├── Navegation.kt                # Grafo de navegación
    │   └── NavegationMain.kt
    │
    └── Scaffold/                        # Componentes de layout
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

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicios disponibles
    val reservationService: ReservationService by lazy { retrofit.create(ReservationService::class.java) }
    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    val roomService: RoomService by lazy { retrofit.create(RoomService::class.java) }
    val reviewService: ReviewService by lazy { retrofit.create(ReviewService::class.java) }
    val userService: UserService by lazy { retrofit.create(UserService::class.java) }
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

    @POST("reservation/getPrice")
    suspend fun getPrice(@Body datos: Map<String, String>): Response<Map<String, Double>>

    @POST("reservation/getCancelationPrice")
    suspend fun cancelationPrice(@Body datos: Map<String, String>): Response<Map<String, String>>
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

---

## Gestión de sesión

### `SessionManager.kt`

Almacena el token JWT y la información del usuario tanto en memoria como en `SharedPreferences` para permitir autologin:

```kotlin
object SessionManager {
    var userToken: String? = null
    var userInfo: UserInfo? = null

    // Recupera la sesión guardada al iniciar la app
    fun restoreSession(): Boolean {
        val token = prefs?.getString(KEY_TOKEN, null) ?: return false
        val json = prefs?.getString(KEY_USER_JSON, null) ?: return false
        userToken = token
        userInfo = gson.fromJson(json, UserInfo::class.java)
        return true
    }

    // Persiste la sesión tras un login exitoso
    fun saveSession() { /* guarda token y userInfo en SharedPreferences */ }

    // Cierra sesión: limpia memoria y preferencias
    fun clear() {
        userToken = null
        userInfo = null
        prefs?.edit()?.remove(KEY_TOKEN)?.remove(KEY_USER_JSON)?.apply()
    }
}
```

---

## Módulos principales

### Reseñas

El módulo de reseñas permite a los clientes valorar habitaciones en las que se hayan alojado.

#### Modelo y repositorio (`Review.kt`)

El `ReviewRepository` centraliza las operaciones y mantiene estado reactivo:

```kotlin
object ReviewRepository {
    val reviews: StateFlow<List<Review>>       // Reseñas de una habitación
    val myReviews: StateFlow<List<Review>>     // Reseñas del usuario

    suspend fun fetchReviewsByRoom(roomId: String) { /* GET /review/room/:roomId */ }
    suspend fun fetchMyReviews()                    { /* GET /review/mine */ }
    suspend fun createReview(roomId: String, rating: Int, comment: String): Result<String> { /* POST /review/create */ }
    suspend fun deleteReview(reviewId: String, roomId: String): Result<String>             { /* DELETE /review/delete */ }
}
```

#### ViewModel (`ReviewViewModel.kt`)

Gestiona la lógica de presentación: verifica si el usuario tiene una reserva en la habitación (condición para poder reseñar), detecta si ya existe una reseña propia, y controla el formulario de envío.

```kotlin
class ReviewViewModel : ViewModel() {
    val canReview: StateFlow<Boolean>     // ¿Tiene reserva en esta habitación?
    val userReview: StateFlow<Review?>    // Reseña existente del usuario (si la hay)

    fun loadReviews(roomId: String) { /* carga reseñas + reservas en paralelo */ }
    fun submitReview(roomId: String) { /* valida y envía POST /review/create */ }
    fun deleteMyReview(roomId: String) { /* envía DELETE /review/delete */ }
}
```

### Reservas

El flujo de reservas incluye consulta de precio, creación, modificación y cancelación. Todas las operaciones pasan por el `ReservationRepository` y sus ViewModels correspondientes (`AddViewModel`, `ModReservaViewModel`, `HomeViewModel`).

### Navegación (`Routes.kt`)

Define todas las rutas de la aplicación como objetos sellados:

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

### Correcciones en reseñas

- Se corrigió la obtención de reseñas por habitación (`GET /review/room/:roomId`), que fallaba por un error en la ruta.
- Se adaptó la creación de reseñas (`POST /review/create`): el campo `user_name` ya no se envía desde el cliente, la API lo resuelve internamente a partir del token JWT.
- Se implementó la eliminación de reseñas (`DELETE /review/delete`) utilizando `@HTTP(method = "DELETE", hasBody = true)` de Retrofit.

### Refactorización de verbos HTTP

- **Cancelación**: se migró de `POST /cancel` a `DELETE /cancel/:reservation_id?price=X`, utilizando `@DELETE` con `@Path` y `@Query`.
- **Actualización**: se migró de `PUT /update` a `PATCH /update`.

---
