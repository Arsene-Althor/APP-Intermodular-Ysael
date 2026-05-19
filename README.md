# Android — Propuestas P11, P5, P9 y P19

Implementación móvil (Kotlin + Jetpack Compose) de las cuatro propuestas del Proyecto Individual. API: [README API](../API-Intermodular-Ysael/README.md). URL base: `BuildConfig.API_BASE_URL` en `app/build.gradle.kts`.

---

## P11 · Auditoría (historial simplificado para el cliente)

El huésped **no** ve JSON ni snapshots técnicos: solo mensajes legibles derivados del campo `action` del log.

### Mapeo de acciones

`data/model/BookingHistoryFriendlyMapper.kt`:

| `action` (API) | Mensaje en app |
|----------------|----------------|
| `CREATED` | Reserva creada |
| `UPDATED` | Cambios en tu reserva |
| `CANCELED` | Reserva cancelada |
| Otros / futuros | Actividad en tu reserva (también preparado: pago, check-in, servicio extra) |

### Dónde se muestra

| Pantalla | Ruta nav | API |
|----------|----------|-----|
| **Actividad** | `ReservationAudit/{reservationId}` | `GET /reservation/{id}/audit` |
| **Gestionar reserva** | `ModReserva` | Misma API; sección `HistorialReservaSection` |

**ViewModels:** `ReservationAuditViewModel`, `ModReservaViewModel` — listan `HistorialItemUi` (fecha + mensaje amigable).

**Acceso:** en **Mis reservas**, botón **Actividad** en cada tarjeta.

---

## P5 · Factura en PDF descargable

La **factura fiscal** exige checkout en recepción (`invoice_number` en la reserva). Hasta entonces el huésped puede usar el justificante si la app lo ofrece en flujos de pago simulado; la propuesta P5 se centra en la factura completa post-checkout.

### Mis reservas

| Elemento | Implementación |
|----------|----------------|
| Botón | **Descargar factura** — visible si `reservation.tieneFactura()` (`invoice_number != null`) |
| API | `GET /reservation/{id}/invoice` (`@Streaming`) |
| Apertura | `InvoicePdfHelper.downloadAndOpenPdf` → caché en dispositivo + visor PDF del sistema (`FileProvider`) |

También disponible en **Gestionar reserva** (`ModReservaScreen`) e **Historial de reservas**.

### Historial personal de facturas

| Pantalla | Ruta | API |
|----------|------|-----|
| **Mis facturas** | `InvoiceHistory` | `GET /invoices?userId=` |

- Lista de facturas emitidas (`HotelInvoice`).
- **Ver PDF** por fila (query `invoice_number` si aplica).
- Al abrir la pantalla puede llamarse `confirm-payment` en reservas activas sin factura (emisión tras pago simulado).

**Archivos:** `feature/invoice/InvoiceHistoryScreen.kt`, `core/util/InvoicePdfHelper.kt`, `core/network/ReservationService.kt`.

---

## P9 · Historial de estancias y estadísticas personales

### Mis estancias

| Elemento | Implementación |
|----------|----------------|
| Acceso | **Mis reservas** → chip **Estancias** |
| Pantalla | `feature/loyalty/MyStaysScreen.kt` — ruta `MyStays` |
| API | `GET /users/{userId}/history` |
| UI | Lista cronológica: foto habitación (Coil), fechas, precio, estado |
| Detalle | `StayDetailScreen` — datos de la estancia + valoración si la API la devuelve |

Filtros en API (`?year=`, `?room_type=`, `?status=`, paginación) disponibles para futuras mejoras de UI; la pantalla actual consume el listado paginado por defecto.

### Mis estadísticas

| Elemento | Implementación |
|----------|----------------|
| Acceso | Barra inferior → **Estadísticas** |
| Pantalla | `feature/loyalty/ClientStatsScreen.kt` — ruta `ClientStats` |
| API | `GET /loyalty/me` |
| UI | Tarjetas: rango fidelidad (bronce/plata/oro), total noches, total gastado, reservas completadas, barra de progreso hacia siguiente nivel |
| Insights | `P9InsightsCard` — temporada favorita, habitación más usada, racha (campos enriquecidos tras recálculo en API) |

**Repositorio / red:** `LoyaltyService` o equivalente en `core/network/`.

---

## P19 · Check-in anticipado y check-out tardío

### Mis reservas — solicitud y estado

| Elemento | Implementación |
|----------|----------------|
| Botones | **Check-in anticipado** y **Check-out tardío (hoy)** — `FlexibilityUi.kt` |
| Selector | `FlexibilityRequestDialog` con `TimePicker` + tarifa mínima (`GET /bookings/{id}/flexibility`) |
| API solicitud | `PATCH /bookings/{id}/request-early-checkin` · `PATCH /bookings/{id}/request-late-checkout` |
| Estado | `FlexibilityStatusSection` — chips de color: pendiente / aprobada / rechazada + hora |
| Fin de estancia | `EndOfStayDecisionDialog` el día de salida (opciones relacionadas con ampliación / instalaciones; fuera del alcance estricto de P19 pero en el mismo flujo de salida) |

### Notificaciones

| Propuesta | Implementación real |
|-----------|---------------------|
| Push FCM | **No** — notificaciones **locales** en el dispositivo |
| Mecanismo | `FlexibilityPollWorker` (cada ~15 min) + `FlexibilityNotificationHelper` comparan estado anterior/nuevo de `GET /reservation/mine` |
| Email | Lo envía el **servidor** al aprobar/rechazar (SMTP), no la app |

Cuando recepción procesa una solicitud en WPF, el cliente ve el cambio de chip en la app y puede recibir la notificación local y/o el correo del hotel.

### Archivos P19

```
feature/flexibility/
  FlexibilityUi.kt
  FlexibilityRepository.kt
  FlexibilityService.kt (Retrofit)
  FlexibilityModels.kt
  FlexibilityNotificationHelper.kt
  FlexibilityPollWorker.kt
feature/reservation/MyBookingsScreens.kt  ← integración en tarjetas
```

---

## Navegación relacionada con las propuestas

| Ruta (`Routes.kt`) | Propuesta |
|--------------------|-----------|
| `Reservations` | P5, P11, P19 — Mis reservas |
| `ReservationAudit/{id}` | P11 |
| `ModReserva` | P5, P11 |
| `InvoiceHistory` | P5 |
| `MyStays` · `StayDetail/{id}` | P9 |
| `ClientStats` | P9 |

Barra inferior: **Reservas** · **Estadísticas** (P9).

---

## Configuración

```properties
# local.properties (opcional)
hotel.api.base.url=http://10.0.2.2:3011/
```

Emulador: `10.0.2.2` apunta al PC host. Dispositivo físico: IP LAN del PC con la API.
