# APP Android — Hotel Pere María

Cliente móvil para huéspedes del **Hotel Pere María**, desarrollado con **Kotlin** y **Jetpack Compose**. La app se conecta a la API REST del proyecto intermodular (**Retrofit** + **Gson**) y prioriza una experiencia de reserva **inspirada en portales tipo Booking**: búsqueda por fechas, huéspedes y presupuesto, resultados filtrables y flujo de confirmación sin formularios legacy.

---

## Tabla de contenidos

- [Requisitos y configuración](#requisitos-y-configuración)
- [Identidad visual (UX)](#identidad-visual-ux)
- [Tecnologías](#tecnologías)
- [Estructura del código](#estructura-del-código)
- [Arquitectura](#arquitectura)
- [Navegación](#navegación)
- [Conexión con la API](#conexión-con-la-api)
- [Ejemplos de código](#ejemplos-de-código)
- [Flujos principales](#flujos-principales)
- [Evolución del proyecto](#evolución-del-proyecto-desde-la-creación)

---

## Requisitos y configuración

| Requisito | Detalle |
|-----------|---------|
| IDE | Android Studio (Hedgehog o superior) |
| SDK mínimo | API 24 (Android 7.0) |
| Red | Misma red que el PC que ejecuta la API, o emulador con `10.0.2.2` |

La URL base se define en `app/build.gradle.kts` como `BuildConfig.API_BASE_URL`. También se puede sobrescribir en `local.properties`:

```properties
hotel.api.base.url=http://192.168.x.x:3011/
```

| Entorno | URL típica |
|---------|------------|
| Emulador | `http://10.0.2.2:3011/` (alias al host) |
| Dispositivo físico | `http://<IP_del_PC>:3011/` |

Tras cambiar la URL, **recompilar** la app para regenerar `BuildConfig`.

---

## Identidad visual (UX)

La interfaz actual sigue una línea **moderna, limpia y orientada a reservas**, alineada con lo que los usuarios esperan de apps de viaje:

| Principio | Cómo se aplica |
|-----------|----------------|
| **Fondo y superficies** | Predominio de **blanco** y grises muy suaves (`surface`, `surfaceVariant`) para legibilidad y sensación de amplitud. |
| **Acento** | **Azul pastel** del tema Material 3 (`primary`, `primaryContainer`) para botones, enlaces, chips activos y barra superior; evita el exceso de color en pantallas densas. |
| **Jerarquía** | Títulos claros, tarjetas con sombra ligera y mucho espacio en blanco entre bloques (motor de búsqueda, próxima reserva, resultados). |
| **Navegación** | **Sin barra inferior fija**; acceso a inicio, reservas y perfil desde la **TopAppBar** (logo grande + iconos). Menos ruido visual y foco en la tarea (buscar → resultados → detalle). |
| **Detalle de habitación** | **Carrusel horizontal** de fotos con indicadores y texto guía; precio destacado con soporte para **ofertas** si la API las envía. |

El archivo `ui/theme/` define la paleta (`Color.kt`, `Theme.kt`). El objetivo es **profesional y calmado**, no recargado.

---

## Tecnologías

| Tecnología | Uso |
|------------|-----|
| Kotlin | Lenguaje |
| Jetpack Compose + Material 3 | UI declarativa y componentes |
| Navigation Compose | Grafo de navegación dentro del scaffold |
| Retrofit + Gson | HTTP y JSON |
| OkHttp | JWT en interceptor + logging opcional |
| Coroutines + Flow | Async y estado (`StateFlow` en repositorios) |
| Coil | Imágenes en listas y carrusel |

---

## Estructura del código

Ruta base: `app/src/main/java/com/example/hotel_pere_maria_app/`

```
com/example/hotel_pere_maria_app/
├── HotelApplication.kt
└── ui/
    ├── MainActivity.kt
    │
    ├── booking/                     # Flujo “tipo Booking”
    │   ├── BookingHomeScreen.kt
    │   ├── BookingResultsScreen.kt
    │   ├── BookingConfirmScreen.kt
    │   └── BookingSearchSession.kt
    │
    ├── Models/
    │   ├── Room.kt
    │   ├── ExtraService.kt
    │   ├── RoomRepository.kt
    │   └── …
    │
    ├── Service/
    │   ├── RetrofitClient.kt
    │   ├── RoomService.kt
    │   └── …
    │
    ├── Navegation/
    │   ├── NavegationMain.kt
    │   ├── Navegation.kt
    │   └── Routes.kt
    │
    ├── Scaffold/
    │   ├── ScaffoldMain.kt
    │   └── TopAppBar.kt
    │
    ├── Views/
    │   ├── RoomDetail.kt
    │   ├── Profile.kt
    │   └── …
    │
    ├── ViewModels/
    │   └── …
    │
    └── theme/
```

---

## Arquitectura

```
Pantalla (Compose) → ViewModel (opcional) → Repository / Service (Retrofit) → API
```

Los repositorios (`RoomRepository`, etc.) centralizan red y exponen `StateFlow` para que las pantallas reaccionen sin bloquear el hilo principal.

---

## Navegación

- **`NavegationMain`**: login / registro / scaffold autenticado.
- **Destino inicial del área cliente**: `booking/home`.
- **Rutas relevantes** (`Routes.kt`): `BookingHome`, `BookingResults`, `BookingConfirm`, `RoomDetail`, `Reservations`, `ReservationHistory`, `ReservationAudit`, `Reviews`, `User`, `ModReserva`, …

El **scaffold** (barra superior) se oculta en algunas pantallas a pantalla completa (`ModReserva`, auditoría, historial), igual que antes.

---

## Conexión con la API

`RetrofitClient` centraliza la base URL (`BuildConfig.API_BASE_URL`) y añade el token en cada petición autenticada:

```kotlin
private val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
    SessionManager.userToken?.let {
        request.addHeader("Authorization", "Bearer $it")
    }
    chain.proceed(request.build())
}
```

### Habitaciones (`RoomService.kt`)

Parámetros **camelCase** (`checkIn`, `checkOut`, `guests`) como espera la API. El repositorio normaliza fechas a **ISO** antes de llamar (tanto si el usuario escribió `dd/MM/yyyy` como si ya viene `yyyy-MM-dd`):

```kotlin
private fun toISO(date: String): String {
    val t = date.trim()
    if (t.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return t
    return try {
        val inp = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed: Date? = inp.parse(t)
        if (parsed != null) out.format(parsed) else t
    } catch (e: Exception) {
        t
    }
}
```

```kotlin
@GET("room/available")
suspend fun getAvailableRoomsByDates(
    @Query("checkIn") checkIn: String,
    @Query("checkOut") checkOut: String,
    @Query("guests") guests: Int? = null,
): Response<List<Room>>

@GET("room/extra-services")
suspend fun listExtraServices(): Response<List<ExtraService>>
```

Si `room/available` devuelve error HTTP, el repositorio guarda el cuerpo en `availableError` para mostrarlo en la UI (no solo un fallo silencioso).

---

## Ejemplos de código

### Rutas de navegación (`Routes.kt`)

El grafo actual gira en torno al flujo **booking** y pantallas de cuenta / reservas:

```kotlin
sealed class Routes(val route: String) {
    object BookingHome : Routes("booking/home")
    object BookingResults : Routes("booking/results")
    object BookingConfirm : Routes("booking/confirm/{roomId}") {
        fun createRoute(roomId: String) = "booking/confirm/$roomId"
    }
    object Reservations : Routes("Reservations")
    object ReservationHistory : Routes("ReservationHistory")
    object ReservationAudit : Routes("ReservationAudit/{reservationId}") {
        fun createRoute(reservationId: String) = "ReservationAudit/$reservationId"
    }
    object RoomDetail : Routes("RoomDetail/{roomId}") {
        fun createRoute(roomId: String) = "RoomDetail/$roomId"
    }
    // … Login, Register, Reviews, User, ModReserva, …
}
```

### Sesión de búsqueda (`BookingSearchSession.kt`)

Estado en memoria (no persistido) compartido entre **inicio**, **resultados** y **confirmación**: fechas en millis, huéspedes, rango de precio y helpers `checkInIso()` / `checkOutIso()` para la API.

```kotlin
object BookingSearchSession {
    var checkInMillis: Long? by mutableStateOf(null)
    var checkOutMillis: Long? by mutableStateOf(null)
    var guests: Int by mutableIntStateOf(2)
    var priceMin: Double by mutableDoubleStateOf(20.0)
    var priceMax: Double by mutableDoubleStateOf(250.0)

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun checkInIso(): String? = checkInMillis?.let { isoFmt.format(Date(it)) }
    fun checkOutIso(): String? = checkOutMillis?.let { isoFmt.format(Date(it)) }

    fun isComplete(): Boolean {
        val ci = checkInMillis ?: return false
        val co = checkOutMillis ?: return false
        return co > ci
    }
}
```

### Modelo `Room` — precio mostrado y galería (`Room.kt`)

La API puede enviar `effective_price_per_night` y listas `images` / `extra_services`. El cliente unifica lo que ve el usuario:

```kotlin
data class Room(
    val room_id: String,
    val image: String,
    val price_per_night: Double,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("extra_services") val extraServices: List<String> = emptyList(),
    @SerializedName("offer_active") val offerActive: Boolean = false,
    @SerializedName("offer_percent") val offerPercent: Double = 0.0,
    @SerializedName("effective_price_per_night") val effectivePricePerNight: Double? = null,
    // … is_operational, is_occupied_now, …
) {
    fun displayPricePerNight(): Double = effectivePricePerNight ?: price_per_night

    fun galleryImageUrls(): List<String> {
        val fromList = images.map { it.trim() }.filter { it.isNotEmpty() }
        if (fromList.isNotEmpty()) return fromList
        return image.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    }
}
```

### Carrusel en detalle (`RoomDetail.kt`)

`HorizontalPager` + indicadores circulares + texto de ayuda; las URLs salen de `galleryImageUrls()`:

```kotlin
val urls = room.galleryImageUrls().filter { it.isNotBlank() }
val pagerState = rememberPagerState(pageCount = { urls.size })
HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxWidth().height(280.dp),
    beyondViewportPageCount = 1,
) { page ->
    AsyncImage(
        model = urls[page],
        contentDescription = "Imagen ${page + 1} de ${urls.size}",
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        contentScale = ContentScale.Crop,
    )
}
```

---

## Flujos principales

1. **Inicio** (`BookingHomeScreen`): el usuario elige fechas, ajusta personas y rango de precio; opcionalmente ve su **próxima reserva**.
2. **Resultados** (`BookingResultsScreen`): lista desde `GET /room/available`; orden por valoración; filtro de precio usando `displayPricePerNight()`; chips de **servicios extra** según catálogo (todos los seleccionados deben estar en la habitación).
3. **Detalle** (`RoomDetail`): carrusel de imágenes, descripción, reseñas y botón de reserva si hay sesión de búsqueda válida.
4. **Confirmación** (`BookingConfirmScreen`): precio vía `POST /reservation/getPrice` y alta con `POST /reservation/add`.
5. **Perfil** (`Profile`): datos de usuario + **Soporte** (mapa, teléfono, correo) centralizado aquí en lugar del antiguo home.

---

## Evolución del proyecto (desde la creación)

Orden aproximado de **novedades** que se fueron incorporando; cada bloque explica el *porqué* y enlaza con el código de arriba cuando aplica.

### 1. Base de la app cliente

- **Login / registro / recuperación** contra la API; **sesión JWT** persistida (`SessionManager` + `SharedPreferences`) para no pedir credenciales en cada arranque.
- **Retrofit** por módulos (`AuthService`, `ReservationService`, `RoomService`, `ReviewService`) y patrón **MVVM** con pantallas Compose.

### 2. Reservas y reseñas en el móvil

- Listado y gestión de **mis reservas**; creación y modificación alineadas con los endpoints de la API.
- **Reseñas** por habitación y listado “mis reseñas” desde perfil.

### 3. Habitaciones fuera de servicio y datos enriquecidos

- La API empezó a enviar `is_operational` / `is_occupied_now`; el **repositorio** filtra habitaciones no operativas para no ofrecerlas al huésped.
- Más tarde llegaron **galería**, **ofertas** y **servicios extra** en el JSON: el modelo `Room` con `SerializedName` y funciones `displayPricePerNight()` / `galleryImageUrls()` concentran la lógica de presentación.

### 4. Flujo tipo Booking (búsqueda → resultados → confirmar)

- Sustituye el flujo antiguo de “elegir habitación en diálogo / formulario aislado” por un **túnel claro**: `BookingHomeScreen` → `BookingResultsScreen` → `RoomDetail` → `BookingConfirmScreen`.
- **`BookingSearchSession`**: fechas, huéspedes y rango de precio compartidos entre pantallas sin serializar todo en la ruta de navegación.
- **`BookingResultsScreen`**: consume `GET /room/available` con fechas ISO; ordenación por valoración; filtro de precio sobre `displayPricePerNight()`; **chips de servicios extra** alimentados por `GET /room/extra-services` (la habitación debe cumplir todos los IDs seleccionados).

### 5. Carrusel de fotos y precio con oferta en detalle

- **`RoomDetail`**: `HorizontalPager` + **indicadores** + texto “desliza…”; imágenes desde `galleryImageUrls()` (soporta tanto `images[]` como `image` con comas legacy).
- Bloque de precio que muestra **tachado** el precio base y el **efectivo** cuando `offerActive` y `offer_percent` > 0.

### 6. Historial de reservas y auditoría “humana”

- **Reservas activas** y acceso a **historial completo** (incluye canceladas y pasadas).
- Pantalla **`ReservationAudit`**: lista de eventos de `GET /reservation/{id}/audit` con textos amigables vía `BookingHistoryFriendlyMapper` (mapea `CREATED`, `UPDATED`, `CANCELED`, y deja preparadas etiquetas para futuras acciones del backend).

### 7. Estética y navegación tipo app de viajes

- **Material 3** con fondo claro y acento azul pastel (`ui/theme/`).
- **Sin bottom navigation bar**: la **TopAppBar** concentra inicio, reservas y perfil; menos ruido visual y foco en buscar y reservar.
- **Soporte** (mapa, teléfono, correo) agrupado en **Perfil** en lugar de disperso en un home legacy.

### 8. Limpieza del árbol de código

- Eliminación de flujos sustituidos (p. ej. formulario `Add` antiguo, listado `RoomList` como entrada principal, barra inferior duplicada). `Home.kt` puede existir como referencia pero **no** es el destino del `NavHost` actual.

---
