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
- [Flujos principales](#flujos-principales)
- [Cambios recientes](#cambios-recientes)

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

`RetrofitClient` añade `Authorization: Bearer <token>` cuando hay sesión.

### Habitaciones (`RoomService.kt`)

Parámetros de disponibilidad alineados con la API (**camelCase**): `checkIn`, `checkOut`, `guests`. El **repositorio** convierte las fechas de la UI a **ISO (`YYYY-MM-DD`)** antes de la petición.

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

---

## Flujos principales

1. **Inicio** (`BookingHomeScreen`): el usuario elige fechas, ajusta personas y rango de precio; opcionalmente ve su **próxima reserva**.
2. **Resultados** (`BookingResultsScreen`): lista desde `GET /room/available`; orden por valoración; filtro de precio usando `displayPricePerNight()`; chips de **servicios extra** según catálogo (todos los seleccionados deben estar en la habitación).
3. **Detalle** (`RoomDetail`): carrusel de imágenes, descripción, reseñas y botón de reserva si hay sesión de búsqueda válida.
4. **Confirmación** (`BookingConfirmScreen`): precio vía `POST /reservation/getPrice` y alta con `POST /reservation/add`.
5. **Perfil** (`Profile`): datos de usuario + **Soporte** (mapa, teléfono, correo) centralizado aquí en lugar del antiguo home.

---

## Cambios recientes

### Estética y navegación

- Interfaz **blanca + acentos azul pastel** (Material 3); tipografía y espaciado tipo app de reservas.
- **Eliminación de la barra de navegación inferior**; accesos principales en **TopAppBar** (logo más visible, inicio, reservas, cuenta).
- Flujo de búsqueda **unificado** en el paquete `ui/booking/`; retirada del flujo antiguo de “añadir reserva” manual y del listado tipo catálogo con bottom bar.

### Funcionalidad alineada con la API

- Búsqueda de disponibilidad con **query correcta** (`checkIn` / `checkOut` / `guests`) y **normalización de fechas** (`toISO` acepta `YYYY-MM-DD`).
- Modelo `Room` con: `images`, `extra_services`, ofertas (`offer_*`), `effective_price_per_night`, y `displayPricePerNight()` para listados y filtros.
- **Filtros de servicios extra** conectados al catálogo `GET /room/extra-services`.
- **Errores de disponibilidad** más visibles en UI cuando la API responde con error antes del fallback.

### Detalle y soporte

- **Carrusel** de galería con indicadores e instrucción de gesto horizontal.
- **Perfil**: bloque “Más información (soporte)” (mapa, llamada, correo).

### Archivos retirados del flujo activo (referencia)

Se eliminaron del proyecto piezas ya sustituidas (por ejemplo formulario `Add`, listado `RoomList` clásico, diálogo de selección de habitación duplicado, barra inferior). El archivo `Home.kt` puede permanecer en el árbol pero **no** forma parte del `NavHost` actual.

---
