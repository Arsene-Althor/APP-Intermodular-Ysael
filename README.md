# APP Android — Hotel Pere María

Aplicación móvil Android desarrollada con **Kotlin** y **Jetpack Compose** para los clientes del Hotel Pere María. Ofrece una experiencia de búsqueda tipo Booking con filtros de fechas, capacidad y precio, gestión completa de reservas con historial de actividad, reseñas de habitaciones y perfil de usuario. Se comunica con la API REST del proyecto intermodular mediante **Retrofit**.

---

## Tabla de contenidos

- [Requisitos](#requisitos)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura](#arquitectura)
- [Navegación y layout](#navegación-y-layout)
- [Conexión con la API](#conexión-con-la-api)
- [Gestión de sesión](#gestión-de-sesión)
- [Módulos principales](#módulos-principales)
- [Cambios recientes](#cambios-recientes)

---

## Requisitos

- Android Studio (Hedgehog o superior)
- SDK mínimo: API 24 (Android 7.0)
- Conexión a la API del proyecto (`API-Intermodular-Ysael`)

La URL base de la API se configura en `BuildConfig.API_BASE_URL` dentro del archivo `build.gradle.kts`:

```kotlin
// app/build.gradle.kts
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3011/\"")
```

| Entorno             | URL base                                  |
|---------------------|-------------------------------------------|
| Emulador Android    | `http://10.0.2.2:3011/` (loopback al PC)  |
| Dispositivo físico  | `http://<IP_LOCAL_PC>:3011/`               |

> **Nota:** `10.0.2.2` es el alias del host del PC dentro del emulador de Android. En un dispositivo físico conectado a la misma red, se debe utilizar la IP local del equipo (ej: `192.168.1.X`). Tras modificar la URL, es necesario recompilar la app para regenerar `BuildConfig`.

---

## Tecnologías utilizadas

| Tecnología         | Uso                                               |
|--------------------|----------------------------------------------------|
| Kotlin             | Lenguaje principal                                 |
| Jetpack Compose    | Interfaz de usuario declarativa                    |
| Material 3         | Componentes y estilos visuales                     |
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
    ├── MainActivity.kt                  # Actividad principal
    │
    ├── Models/                          # Data classes, repositorios y estado
    │   ├── BookingAuditEntry.kt         # DTO de auditoría (campos mínimos para UI)
    │   ├── BookingHistoryFriendlyMapper.kt # Mapeo action → texto usuario
    │   ├── BookingSearchState.kt        # Criterios de búsqueda compartidos entre pantallas
    │   ├── LoginRequest.kt / LoginResponse.kt / RegisterRequest.kt / RegisterResponse.kt
    │   ├── Reservation.kt               # Modelo de reserva + ReservationRepository
    │   ├── Review.kt                    # Modelo de reseña + ReviewRepository
    │   ├── Room.kt                      # Modelo de habitación (isOperational, isOccupiedNow)
    │   ├── RoomRepository.kt            # Repositorio de habitaciones (cache + fallback)
    │   └── User.kt                      # Modelo de usuario
    │
    ├── Service/                         # Interfaces Retrofit y utilidades
    │   ├── RetrofitClient.kt            # Configuración centralizada de Retrofit
    │   ├── AuthService.kt               # Endpoints de autenticación
    │   ├── ReservationService.kt        # Endpoints de reservas + auditoría
    │   ├── RoomService.kt               # Endpoints de habitaciones
    │   ├── ReviewService.kt             # Endpoints de reseñas
    │   ├── UserService.kt               # Endpoints de usuarios
    │   ├── SessionManager.kt            # Gestión de sesión y autologin
    │   └── ThemeManager.kt              # Preferencias de tema
    │
    ├── ViewModels/
    │   ├── BookingHomeViewModel.kt      # Home tipo Booking (búsqueda)
    │   ├── SearchResultsViewModel.kt    # Resultados filtrados + ordenación
    │   ├── ModReservaViewModel.kt       # Edición de reserva + historial de auditoría
    │   ├── ReservationAuditViewModel.kt # Auditoría independiente de una reserva
    │   ├── ReviewViewModel.kt           # Reseñas de una habitación
    │   ├── MyReviewsViewModel.kt        # Reseñas globales del usuario
    │   ├── RoomViewModel.kt             # Listado y filtrado de habitaciones
    │   ├── AddViewModel.kt              # Crear reserva
    │   ├── LoginViewModel.kt / RegisterViewModel.kt / ForgotPasswordViewModel.kt
    │   ├── HomeViewModel.kt             # Información del hotel (mapa, contacto)
    │   └── ProfileViewModel.kt
    │
    ├── Views/                           # Pantallas Compose
    │   ├── BookingHomeScreen.kt         # Pantalla principal (búsqueda tipo Booking)
    │   ├── SearchResultsScreen.kt       # Resultados de búsqueda con chips de ordenación
    │   ├── MyBookingsScreens.kt         # Reservas activas + historial completo
    │   ├── ReservationAuditScreen.kt    # Auditoría amigable de una reserva
    │   ├── MyAccountScreen.kt           # Mi cuenta (soporte, reservas, perfil)
    │   ├── RoomList.kt                  # Catálogo de habitaciones (tarjetas M3)
    │   ├── RoomDetail.kt                # Detalle + galería + reseñas
    │   ├── Reviews.kt                   # Reseñas del usuario (acceso desde perfil)
    │   ├── ModReserva.kt                # Edición de reserva + actividad
    │   ├── Add.kt                       # Formulario de nueva reserva
    │   ├── Login.kt / Register.kt / ForgotPassword.kt
    │   ├── Profile.kt                   # Perfil y ajustes
    │   ├── Home.kt                      # (Legacy, no en NavHost)
    │   └── Components.kt                # Componentes reutilizables
    │
    ├── Components/
    │   └── RoomSelectionDialog.kt       # Diálogo de selección de habitación
    │
    ├── Navegation/
    │   ├── Routes.kt                    # Definición de rutas
    │   ├── Navegation.kt                # Grafo de navegación
    │   └── NavegationMain.kt
    │
    ├── Scaffold/
    │   ├── ScaffoldMain.kt              # Layout principal (sin bottom bar)
    │   └── TopAppBar.kt                 # Barra superior (Reservas + Mi Cuenta)
    │
    └── theme/                           # Tema visual
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

## Navegación y layout

### Rutas — `Routes.kt`

```kotlin
sealed class Routes(val route: String) {
    object Login : Routes("Login")
    object Register : Routes("Register")
    object Home : Routes("Home")                    // BookingHomeScreen
    object SearchResults : Routes("SearchResults")  // Resultados de búsqueda
    object MyAccount : Routes("MyAccount")          // Mi cuenta
    object Reservations : Routes("Reservations")    // Mis reservas activas
    object ReservationHistory : Routes("ReservationHistory")
    object ReservationAudit : Routes("ReservationAudit/{reservationId}") {
        fun createRoute(reservationId: String) = "ReservationAudit/$reservationId"
    }
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

### Scaffold — Sin bottom bar

El scaffold principal utiliza solo una **TopAppBar** con dos iconos de acción:

- **Calendario** (`Icons.Event`) → navega a `Reservations` (mis reservas).
- **Persona** (`Icons.Person`) → navega a `MyAccount` (mi cuenta).

Se oculta el scaffold completo en pantallas de lectura/gestión a pantalla completa:

```kotlin
val mostrarScafold = when {
    currentRoute == null -> true
    currentRoute.startsWith(Routes.ModReserva.route) -> false
    currentRoute.startsWith("ReservationAudit") -> false
    currentRoute == Routes.ReservationHistory.route -> false
    else -> true
}
```

---

## Conexión con la API

### `RetrofitClient.kt`

Punto central de configuración HTTP con interceptor JWT automático:

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

    @GET("reservation/{reservation_id}/audit")
    suspend fun getBookingAudit(
        @Path("reservation_id") reservationId: String
    ): Response<List<BookingAuditEntry>>

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

### Pantalla principal — Búsqueda tipo Booking

La pantalla de inicio (`BookingHomeScreen.kt`) sustituye al antiguo `Home.kt` y ofrece una experiencia de búsqueda con:

- **Próxima reserva** del usuario (si existe).
- **Selector de fechas** (check-in / check-out).
- **Slider de huéspedes** (1–8 personas).
- **RangeSlider de precio** (rango personalizable).
- **Botón Buscar** → navega a resultados.
- Accesos directos a: nueva reserva, catálogo de habitaciones, mis reservas.

#### `BookingSearchState.kt` — Estado compartido

Criterios de búsqueda que persisten en memoria entre la pantalla de búsqueda y la de resultados:

```kotlin
data class BookingSearchCriteria(
    val checkIn: String,
    val checkOut: String,
    val guests: Int,
    val priceMin: Double,
    val priceMax: Double,
)

object BookingSearchState {
    val criteria: StateFlow<BookingSearchCriteria?> = _criteria.asStateFlow()
    fun setCriteria(c: BookingSearchCriteria) { /* guarda en StateFlow */ }
    fun clear() { /* limpia criterios */ }
}
```

### Resultados de búsqueda — `SearchResultsScreen.kt`

Lista de habitaciones filtradas por los criterios de búsqueda. Incluye:

- **Chips de ordenación**: valoración (↑↓), precio (↑↓), tipo A–Z.
- **Chips de servicios extra** (Desayuno, Spa, Parking…): solo UI, pendiente de backend.
- Navegación a `RoomDetail` al pulsar una habitación.

```kotlin
enum class RoomSortOption(val label: String) {
    RATING_DESC("Valoración: mayor primero"),
    PRICE_ASC("Precio: más barato"),
    PRICE_DESC("Precio: más caro"),
    NAME("Tipo A–Z"),
}
```

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
    @SerializedName(value = "is_operational", alternate = ["isOperational"])
    val isOperational: Boolean? = null,
    @SerializedName(value = "is_occupied_now", alternate = ["isOccupiedNow"])
    val isOccupiedNow: Boolean? = null,
) {
    fun isInService(): Boolean = isOperational != false
    fun isOccupiedNowEffective(): Boolean = isOccupiedNow == true
    fun isFreeNow(): Boolean = isInService() && !isOccupiedNowEffective()
    fun galleryImageUrls(): List<String> = image.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
```

- **`isOperational`** / **`isOccupiedNow`**: ahora `Boolean?` con `alternate` en `@SerializedName` para manejar ambas formas de envío de la API (`is_operational` y `isOperational`). `null` se trata como "en servicio".
- **`isInService()`**: solo excluye si explícitamente `false`.
- **`galleryImageUrls()`**: separa múltiples URLs por coma para el `HorizontalPager` en `RoomDetail`.

#### Repositorio — `RoomRepository.kt`

Solo almacena habitaciones en servicio (`isInService()`):

```kotlin
object RoomRepository {
    val rooms: StateFlow<List<Room>>              // Solo habitaciones en servicio
    val availableRooms: StateFlow<List<Room>>     // Disponibles por fechas (en servicio)

    suspend fun fetchRooms()                      // GET /room/all → filtra isInService()
    suspend fun getRoomById(roomId: String): Room? // null si fuera de servicio
    suspend fun fetchAvailableRoomsByDates(...)    // GET /room/available → filtra isInService()
}
```

#### Vista — `RoomList.kt`

Tarjetas Material 3 con imagen arriba, datos debajo. Las habitaciones fuera de servicio no aparecen en la lista. Chips de filtro: "Ocupadas ahora" muestra solo las habitaciones con reserva activa (no las fuera de servicio).

### Reservas y actividad

#### `MyBookingsScreens.kt`

Dos pantallas en un mismo archivo:

- **MyBookingsScreen**: reservas no canceladas, con botón "Historial" para ver todas. Cada fila incluye un botón "Actividad" (auditoría amigable) y "Gestionar reserva" (navega a `ModReserva`).
- **ReservationHistoryScreen**: lista completa de reservas (incluidas canceladas y pasadas), con acceso a la auditoría de cada una.

#### `ReservationAuditScreen.kt` + `ReservationAuditViewModel.kt`

Pantalla dedicada a la auditoría amigable de una reserva. Utiliza `BookingHistoryFriendlyMapper` para convertir acciones en textos legibles:

```kotlin
class ReservationAuditViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val reservaId: String = checkNotNull(savedStateHandle["reservationId"])

    private suspend fun cargarHistorial() {
        val response = RetrofitClient.reservationService.getBookingAudit(reservaId)
        val items = list.map { entry ->
            HistorialItemUi(
                fechaTexto = entry.timestamp?.let { fmt.format(it) } ?: "—",
                mensaje = BookingHistoryFriendlyMapper.toUserMessage(entry.action)
            )
        }
    }
}
```

### Mi cuenta — `MyAccountScreen.kt`

Pantalla centralizada con acceso a:

- **Soporte**: mapa, llamada y correo del hotel (vía `HomeViewModel`).
- **Mis reservas** → navega a `Reservations`.
- **Perfil** → navega a `User`.
- **Asistente de reserva** → navega a `Add`.

### Reseñas

Accesibles desde **Perfil > Ajustes > Mis reseñas**. La vista `Reviews.kt` muestra una barra superior con botón de retroceso cuando se accede desde el perfil.

### Detalle de habitación — `RoomDetail.kt`

- **Galería**: `HorizontalPager` con múltiples imágenes vía `galleryImageUrls()`.
- **Reseñas**: título actualizado a "Reseñas de otros clientes".

### Historial de auditoría — `BookingHistoryFriendlyMapper.kt`

Mapeo de acciones de la API a textos de usuario:

| Acción API                              | Texto al usuario                |
|-----------------------------------------|----------------------------------|
| `CREATED`                               | "Reserva creada"                |
| `PAYMENT_RECEIVED` / `PAYMENT` / `PAID` | "Pago recibido"                 |
| `CHECK_IN` / `CHECKIN`                  | "Check-in realizado"            |
| `EXTRA_SERVICE` / `SERVICE_ADDED`       | "Servicio extra añadido"        |
| `UPDATED`                               | "Cambios en tu reserva"         |
| `CANCELED`                              | "Reserva cancelada"             |
| Otros                                   | "Actividad en tu reserva"       |

> La API actualmente registra solo `CREATED`, `UPDATED` y `CANCELED`. Los demás textos están preparados para futuras acciones del backend.

---

## Cambios recientes

### Rediseño de navegación y layout

- **Sin bottom bar**: el scaffold principal ya no incluye barra inferior de navegación.
- **TopAppBar**: iconos de Reservas (calendario) y Mi Cuenta (persona) en la barra superior.
- **Pantallas sin scaffold**: `ModReserva`, `ReservationAudit` y `ReservationHistory` se muestran a pantalla completa.

### Home tipo Booking

- `BookingHomeScreen.kt` reemplaza al antiguo `Home.kt` como pantalla principal.
- `BookingSearchState.kt` comparte criterios de búsqueda entre pantallas sin serializar en la ruta.
- `SearchResultsScreen.kt` muestra resultados filtrados con chips de ordenación y servicios extra (solo UI).

### Gestión de reservas

- `MyBookingsScreens.kt`: reservas activas con botones de auditoría y gestión; historial completo accesible.
- `ReservationAuditScreen.kt` + `ReservationAuditViewModel.kt`: auditoría amigable independiente.
- `MyAccountScreen.kt`: punto centralizado de acceso a reservas, perfil y soporte.

### Habitaciones fuera de servicio (filtro cliente)

- `RoomRepository` solo almacena habitaciones con `isInService() == true`.
- `RoomViewModel` filtra exclusivamente sobre habitaciones en servicio.
- `RoomList` usa tarjetas Material 3 (imagen arriba). Las habitaciones fuera de servicio no entran en la lista.
- `Room.kt`: campos `isOperational` / `isOccupiedNow` ahora `Boolean?` con `@SerializedName(alternate = [...])`. Nuevas funciones: `isInService()`, `isOccupiedNowEffective()`, `galleryImageUrls()`.

### Reseñas desde perfil

- Botón "Mis reseñas" en Perfil > Ajustes → pantalla de reseñas con barra de retroceso.

### Otros cambios previos

- `BookingAuditEntry.kt` y `BookingHistoryFriendlyMapper.kt` para historial amigable.
- `API_BASE_URL` actualizada a `http://10.0.2.2:3011/` (puerto 3011).
- Verbos HTTP: cancelación con `DELETE`, actualización con `PATCH`.

---
