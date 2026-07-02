# PLAN — Caso 1 · Almacenes Paris: Marketplace de Mejoramiento del Hogar (v2)

> DSY1103 Full Stack I · EP2 · Arquitectura de microservicios Spring Boot
> Segunda versión: reutiliza la descomposición y las reglas de negocio de la v1 del equipo,
> reescrita bajo las convenciones del repo de referencia del profesor (`dsy1103_bibliotecaduoc`).

---

## 0. Decisiones fijadas y convenciones

| Decisión | Valor |
|---|---|
| Stack | Java 21 + Spring Boot **3.3.6** + Maven (`./mvnw`), calcado del pom del profesor |
| Estructura | **Monorepo**: una carpeta por servicio bajo `Caso1/` |
| Base de datos | PostgreSQL en Neon: **un proyecto Neon, una base de datos lógica por servicio** (11 BDs, cero tablas compartidas) |
| Config | `application.properties` + `spring.profiles.include=secrets` → credenciales en `application-secrets.properties` (excluido de git, patrón del profesor) |
| Paquetes | `com.example.<servicio>` con capas `model / repository / service / controller / config / dto / mapper / exception` |
| Entidades | `@Entity` + `@Table(name = "<plural_snake_case>")`, `@Id @GeneratedValue(IDENTITY)`, `@Column(name, nullable, length)`, IDs `Integer`, **sin Lombok** |
| Repositorios | `JpaRepository<Entity, Integer>` + Query Methods (+ `@Query` nativa cuando haga falta) |
| REST | `/api/v1/<recurso>` con subrecursos anidados; `ResponseEntity`; 200/201/204 |
| DTOs | Records de entrada con Bean Validation (`@Valid`), mapper estático `XxxMapper.toModel()`; respuestas con DTO de salida cuando el recurso cruza servicios |
| Errores | `GlobalExceptionHandler` (`@RestControllerAdvice`) con **ProblemDetail RFC 7807**: 400 validación/JSON · 404 no existe · **409** conflicto de negocio (duplicado, estado inválido) · **503** servicio dependiente caído · 500 catch-all |
| WebClient | **Patrón extendido** (acordado): `config/WebClientConfig.java` con un `@Bean("<x>WebClient")` por servicio consumido + `@Qualifier`, `baseUrl` desde `<svc>.service.url` en properties, llamadas síncronas `.retrieve()...block()`, `onStatus` → excepciones propias (404/409), `WebClientRequestException` → 503, **referencia blanda** (id foráneo sin FK física entre BDs) |
| Moneda | Montos en CLP como enteros (`int`), sin decimales |
| Comisión | `paris.comision.porcentaje=10` en properties de `ventas` (cálculo siempre en servidor) |

Nota v1→v2: la v1 usaba Boot 4.0.6, `application.yml`, paquete `cl.paris.*`, IDs `Long`/UUID y códigos 422/502.
La v2 unifica todo a la convención del profesor (tabla anterior). Las **reglas de negocio de la v1 se conservan**.

---

## 1. Descomposición: 11 microservicios de negocio + gateway (infra)

| # | Servicio | Puerto | BD (Neon) | Responsabilidad única |
|---|---|---|---|---|
| 1 | `legacy` | 8081 | `legacydb` | Simula el sistema legacy: valida credenciales de los 5.000 clientes históricos |
| 2 | `clientes` | 8082 | `clientesdb` | Perfil de clientes nuevos y migrados; direcciones; login (migra históricos vía legacy) |
| 3 | `proveedores` | 8083 | `proveedoresdb` | Postulación de vendedores, documentos, estado (aprobación/rechazo) |
| 4 | `productos` | 8084 | `productosdb` | Catálogo por proveedor: categorías, precio, stock, ofertas |
| 5 | `ventas` | 8085 | `ventasdb` | Órdenes de compra; **orquestador** del flujo de pago; **cálculo de comisión** |
| 6 | `pagos` | 8086 | `pagosdb` | Pago, comprobante y reembolsos (ex `registro`) |
| 7 | `despacho` | 8087 | `despachodb` | Seguimiento del envío, estados, historial, "marcar enviado" (ex `estado`) |
| 8 | `tickets` | 8088 | `ticketsdb` | Reclamos, hilo de mensajes, mediación del admin, autorización de reembolso |
| 9 | `feedback` | 8089 | `feedbackdb` | Reseñas con compra verificada; promedio por producto; moderación |
| 10 | `notificaciones` | 8090 | `notificacionesdb` | Registro y consulta de avisos (despacho, aprobación, resolución) |
| 11 | `administrador` | 8091 | `administradordb` | Aprobación de proveedores, auditoría de acciones, **reporte semanal por categoría** |
| — | `gateway` | 8080 | — | Punto de entrada único. **Infra: no cuenta al mínimo de 10 ni puntúa EP2 → backlog** |

### 1.1 `legacy` (8081)

**Entidades:** `ClienteLegacy` (tabla `clientes_legacy`): id, rut UK, email UK, password, nombre, fecha_registro.
Seed de **5.000 filas** vía script SQL (`generate_series`) en el SQL Editor de Neon (o `CommandLineRunner` idempotente).
Password en texto plano: es una simulación de sistema antiguo; BCrypt queda en backlog EA3.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/legacy/validaciones` | Valida `{email, password}` → 200 con datos del cliente / 404 no existe / 409 credenciales inválidas |
| GET | `/api/v1/legacy/clientes/{email}` | Datos del cliente histórico (para migración) |

**Consume:** nada. **Lo consumen:** `clientes`.

### 1.2 `clientes` (8082)

**Entidades:** `Cliente` 1—N `Direccion`. Cliente: email UK, tipo `NUEVO|MIGRADO`, `legacy_id` (ref blanda → legacy).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/clientes` | Registro de cliente nuevo (Pedro) |
| POST | `/api/v1/clientes/login` | Login: valida local; si no existe, valida contra `legacy` y **migra** al histórico (María) sin re-registro |
| GET | `/api/v1/clientes/{id}` | Perfil (lo consume `ventas` para validar cliente) |
| PUT | `/api/v1/clientes/{id}` | Actualiza perfil |
| GET / POST | `/api/v1/clientes/{id}/direcciones` | Subrecurso anidado: direcciones de despacho |

**Consume:** `legacy`. **Lo consumen:** `ventas`.
**Reglas:** email único; un migrado conserva su `legacy_id`; login de migrado no duplica cliente.

### 1.3 `proveedores` (8083)

**Entidades:** `Proveedor` 1—N `DocumentoProveedor`. Estado `POSTULADO|APROBADO|RECHAZADO` + observaciones.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/proveedores` | Postulación (Ferretería Cóndor) |
| GET | `/api/v1/proveedores?estado=` | Lista/filtra (bandeja del admin) |
| GET | `/api/v1/proveedores/{id}` | Detalle (lo consume `productos`) |
| GET / POST | `/api/v1/proveedores/{id}/documentos` | Subrecurso: documentos de la postulación |
| PATCH | `/api/v1/proveedores/{id}/estado` | Aprueba/rechaza con observaciones (lo invoca `administrador`) |

**Consume:** nada. **Lo consumen:** `productos`, `administrador`.
**Reglas:** rut único; solo se puede resolver una postulación `POSTULADO`; el rechazo exige observaciones.

### 1.4 `productos` (8084)

**Entidades:** `Categoria` 1—N `Producto` 1—N `Oferta`. Producto con `proveedor_id` (ref blanda), precio, stock.
Oferta con tipo `PORCENTAJE|MONTO_FIJO`, vigencia y flag activa → el servicio expone el **precio vigente** calculado.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/productos` | Publica producto — **valida proveedor APROBADO** (WebClient → proveedores) |
| GET | `/api/v1/productos?categoria=&proveedorId=` | Catálogo con filtros |
| GET | `/api/v1/productos/{id}` | Detalle con precio vigente (lo consume `ventas`, `feedback`) |
| PUT | `/api/v1/productos/{id}` | Edita precio/stock/estado (vendedor) |
| PATCH | `/api/v1/productos/{id}/stock` | Descuenta stock al vender (lo invoca `ventas`) |
| GET / POST | `/api/v1/productos/{id}/ofertas` | Subrecurso: ofertas del producto |
| GET | `/api/v1/categorias` | Categorías del marketplace |

**Consume:** `proveedores`. **Lo consumen:** `ventas`, `feedback`, (`administrador` opcional).
**Reglas:** solo proveedor APROBADO publica; stock nunca negativo; una sola oferta activa por producto.

### 1.5 `ventas` (8085) — orquestador

**Entidades:** `Venta` 1—N `DetalleVenta`. Venta: `cliente_id` blando, estado `CREADA|PAGADA|ANULADA`, monto_total, comision_total.
Detalle: `producto_id`/`proveedor_id` blandos + **snapshots**: nombre_producto, `categoria` (insumo del reporte semanal sin depender de productos), precio_unitario, subtotal, comisión por línea.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/ventas` | Crea la orden (el "carrito" se materializa aquí): valida cliente (→ clientes), valida producto/stock/precio vigente por línea (→ productos), calcula totales y **comisión (10%)** |
| PATCH | `/api/v1/ventas/{id}/pagar` | **Orquestación**: CREADA→PAGADA, luego → `pagos` (registra pago), → `despacho` (crea seguimiento), → `notificaciones` (aviso), → `productos` (descuenta stock) |
| GET | `/api/v1/ventas/{id}` | Detalle (lo consumen pagos/despacho/tickets/feedback) |
| GET | `/api/v1/ventas?clienteId=&proveedorId=&desde=&hasta=` | Historial del cliente / órdenes del vendedor / rango para reporte |
| GET | `/api/v1/ventas/{id}/detalles` | Subrecurso: líneas de la venta |

**Consume:** `clientes`, `productos`, `pagos`, `despacho`, `notificaciones` (5 servicios — foco de la live demo).
**Lo consumen:** `pagos`, `despacho`, `tickets`, `feedback`, `administrador`.
**Reglas:** sin stock no hay venta; montos y comisión se calculan en servidor; solo una venta CREADA puede pagarse.

### 1.6 `pagos` (8086) — ex `registro`

**Entidades:** `Pago` 1—1 `Comprobante`, `Pago` 1—N `Reembolso`. Pago: `venta_id` blando UK, medio `TARJETA|DEBITO|TRANSFERENCIA`, estado `PAGADO|REEMBOLSADO|ANULADO`. Comprobante con folio único.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/pagos` | Registra pago — **valida contra `ventas`** que la venta exista, esté PAGADA y el monto coincida |
| GET | `/api/v1/pagos/{id}` · `/api/v1/pagos?ventaId=` | Consulta |
| GET | `/api/v1/pagos/{id}/comprobante` | Comprobante (folio) |
| POST | `/api/v1/pagos/{id}/reembolsos` | Registra reembolso (lo invoca `tickets` al autorizar) |
| GET | `/api/v1/pagos/{id}/reembolsos` | Reembolsos del pago |

**Consume:** `ventas`. **Lo consumen:** `ventas` (orquestación), `tickets`.
**Reglas:** un pago por venta; un comprobante por pago; reembolso ≤ monto pagado; reembolso deja el pago en REEMBOLSADO.

### 1.7 `despacho` (8087) — ex `estado`

**Entidades:** `Seguimiento` 1—N `HistorialEstado`. Seguimiento: `venta_id` blando UK, estados `PENDIENTE|PREPARACION|ENVIADO|EN_REPARTO|ENTREGADO|CANCELADO`, número de seguimiento único.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/envios` | Crea seguimiento — **valida venta PAGADA** (→ ventas) |
| GET | `/api/v1/envios/{id}` · `/api/v1/envios?ventaId=&clienteId=&proveedorId=` | Consulta del cliente ("sigue el despacho desde su perfil") y del vendedor |
| PATCH | `/api/v1/envios/{id}/estado` | **"Marcar enviado"** (vendedor); al pasar a ENVIADO → aviso vía `notificaciones` |
| GET | `/api/v1/envios/{id}/historial` | Trazabilidad completa |

**Consume:** `ventas`, `notificaciones`. **Lo consumen:** `ventas` (orquestación).
**Reglas:** un seguimiento por venta; sin retroceso desde estados terminales; todo cambio queda en historial.

### 1.8 `tickets` (8088)

**Entidades:** `Ticket` 1—N `TicketMensaje`. Ticket: refs blandas venta/cliente/proveedor, categoría `PRODUCTO_DEFECTUOSO|NO_RECIBIDO|OTRO`, estado `ABIERTO|EN_MEDIACION|RESUELTO|RECHAZADO`, `reembolso_autorizado`.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/tickets` | Abre reclamo — **valida compra real** (→ ventas) y toma cliente/proveedor de la venta |
| GET | `/api/v1/tickets?estado=&clienteId=` | Bandeja del admin / reclamos del cliente |
| GET | `/api/v1/tickets/{id}` | Detalle con hilo |
| POST | `/api/v1/tickets/{id}/mensajes` | Mediación: mensajes de cliente/proveedor/admin |
| PATCH | `/api/v1/tickets/{id}/resolver` | Admin resuelve (RESUELTO/RECHAZADO); si autoriza reembolso → `pagos` POST reembolso; → `notificaciones` aviso |

**Consume:** `ventas`, `pagos`, `notificaciones`. **Lo consumen:** —
**Reglas:** solo se reclama sobre venta real; ticket cerrado no se reabre ni re-resuelve; la resolución deja constancia.

### 1.9 `feedback` (8089)

**Entidades:** `Resena`: refs blandas producto/cliente/venta, calificación 1–5, UNIQUE(cliente, producto, venta).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/resenas` | Crea reseña — **compra verificada**: el producto está en los detalles de esa venta del cliente (→ ventas); producto existe (→ productos) |
| GET | `/api/v1/resenas/producto/{productoId}` | Reseñas de un producto |
| GET | `/api/v1/resenas/producto/{productoId}/promedio` | Promedio + total (reputación para "elegir entre ofertas") |
| DELETE | `/api/v1/resenas/{id}` | Moderación (acción exclusiva del admin) |

**Consume:** `ventas`, `productos`. **Lo consumen:** —
**Reglas:** solo reseña quien compró; una reseña por (cliente, producto, venta); calificación acotada 1–5.

### 1.10 `notificaciones` (8090)

**Entidades:** `Notificacion`: destinatario (tipo + id blando), tipo `DESPACHO|APROBACION_PROVEEDOR|RESOLUCION_RECLAMO|VENTA_CONFIRMADA`, estado `PENDIENTE|ENVIADA`, referencia_id.
El "envío" es simulado: registrar = enviar (correo real queda en backlog).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/notificaciones` | Crea aviso (lo invocan ventas, despacho, tickets, administrador) |
| GET | `/api/v1/notificaciones?destinatarioId=&tipo=` | Bandeja del cliente/proveedor |
| GET | `/api/v1/notificaciones/{id}` | Detalle |

**Consume:** nada (receptor puro). **Lo consumen:** `ventas`, `despacho`, `tickets`, `administrador`.

### 1.11 `administrador` (8091)

**Entidades:** `Administrador` 1—N `AccionAdmin` (auditoría), `ReporteSemanal` 1—N `ReporteCategoria` (el reporte generado se **persiste**).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/administradores/login` | Acceso del admin (simple en EP2; JWT en backlog) |
| GET | `/api/v1/admin/proveedores/pendientes` | Bandeja de postulaciones (→ proveedores `?estado=POSTULADO`) |
| POST | `/api/v1/admin/proveedores/{id}/aprobacion` | Aprueba/rechaza con observaciones → PATCH a `proveedores` + aviso a `notificaciones` + registra `AccionAdmin` |
| POST | `/api/v1/admin/reportes/semanal` | **Reporte semanal por categoría**: agrega (→ ventas, rango de fechas) usando el snapshot de categoría del detalle; persiste reporte + desglose |
| GET | `/api/v1/admin/reportes` · `/api/v1/admin/reportes/{id}` | "Descargar" reporte (JSON) |
| GET | `/api/v1/admin/acciones` | Auditoría de acciones administrativas |

**Consume:** `proveedores`, `ventas`, `notificaciones` (+ `productos` opcional si se quisiera la categoría canónica).

### gateway (8080) — backlog

Spring Cloud Gateway con rutas estáticas por puerto (sin Eureka en EP2). Se construye al final solo si sobra tiempo; la demo EP2 golpea los servicios directo por puerto.

---

## 2. Mapa de comunicación WebClient (ítem 2 de la rúbrica)

```
clientes ────────▶ legacy            valida credenciales del histórico y migra
productos ───────▶ proveedores       solo un proveedor APROBADO publica
ventas ──┬──────▶ clientes           cliente válido
         ├──────▶ productos          existencia + precio vigente + stock (y luego descuento)
         ├──────▶ pagos              registrar pago              ┐
         ├──────▶ despacho           crear seguimiento           ├ orquestación de PATCH /pagar
         └──────▶ notificaciones     aviso de compra             ┘
pagos ───────────▶ ventas            venta existe, PAGADA, monto coincide
despacho ─┬─────▶ ventas             venta PAGADA antes de crear seguimiento
          └─────▶ notificaciones     aviso al pasar a ENVIADO
tickets ──┬─────▶ ventas             la compra es real (toma cliente/proveedor)
          ├─────▶ pagos              registra el reembolso autorizado
          └─────▶ notificaciones     aviso de resolución
feedback ─┬─────▶ ventas             compra verificada (producto ∈ detalles de la venta)
          └─────▶ productos          el producto existe
administrador ┬─▶ proveedores        aprobar/rechazar postulación
              ├─▶ ventas             datos para el reporte semanal por categoría
              └─▶ notificaciones     aviso de aprobación/rechazo
```

8 servicios hacen llamadas salientes; todos los flujos incluyen **validación cruzada antes de escribir**.
Convención técnica por servicio consumidor:

- `config/WebClientConfig.java` con un bean por destino: `@Bean("ventasWebClient")`, inyección con `@Qualifier("ventasWebClient")`.
- URL por properties: `ventas.service.url=http://localhost:8085` (una propiedad por destino).
- Llamada síncrona: `.get().uri(...).retrieve().onStatus(...).bodyToMono(Dto.class).block()`.
- Errores: 404 remoto → `ResourceNotFoundException` (404 propio); 409 remoto → `BusinessConflictException` (409); `WebClientRequestException` (servicio caído) → `ServiceUnavailableException` (503). Timeouts de conexión/respuesta configurados.
- Referencia blanda: el id remoto se guarda como columna simple (`Integer`, sin FK física); la existencia se valida por WebClient al escribir.

---

## 3. Matriz roles × permisos (ítem 4)

| Acción (endpoint) | Cliente | Proveedor | Admin |
|---|:-:|:-:|:-:|
| Registrarse / login (`POST /clientes`, `/clientes/login`) | ✔ | — | — |
| Gestionar direcciones (`/clientes/{id}/direcciones`) | ✔ | — | — |
| Ver catálogo, ofertas y reseñas (`GET /productos`, `/resenas/producto/*`) | ✔ | ✔ | ✔ |
| **Crear venta y pagar** (`POST /ventas`, `PATCH /ventas/{id}/pagar`) | ✔ | — | — |
| Ver sus compras / seguimiento (`GET /ventas?clienteId=`, `GET /envios?clienteId=`) | ✔ | — | — |
| **Abrir reclamo** + mensajes (`POST /tickets`, `POST /tickets/{id}/mensajes`) | ✔ | ✔ (responder) | ✔ (mediar) |
| **Crear reseña** (`POST /resenas`) | ✔ | — | — |
| **Postular como vendedor** + documentos (`POST /proveedores`, `/{id}/documentos`) | — | ✔ | — |
| **Publicar/editar productos y ofertas** (`POST/PUT /productos`, `/{id}/ofertas`) | — | ✔ (solo APROBADO) | — |
| Ver órdenes de sus productos (`GET /ventas?proveedorId=`) | — | ✔ | — |
| **Marcar enviado** (`PATCH /envios/{id}/estado`) | — | ✔ | — |
| **Aprobar/rechazar proveedores** (`POST /admin/proveedores/{id}/aprobacion`) | — | — | ✔ |
| **Resolver tickets / autorizar reembolso** (`PATCH /tickets/{id}/resolver`) | — | — | ✔ |
| **Reporte semanal por categoría** (`POST/GET /admin/reportes*`) | — | — | ✔ |
| Moderar reseñas (`DELETE /resenas/{id}`) | — | — | ✔ |
| Ver notificaciones propias (`GET /notificaciones?destinatarioId=`) | ✔ | ✔ | — |

Cada rol tiene ≥4 acciones **exclusivas** (negrita). En EP2 la diferenciación es funcional (endpoints y flujo de demo por actor); RBAC/JWT queda en backlog EA3.

---

## 4. Modelo de datos por servicio

Resumen (detalle y diagramas Mermaid en [docs/diagramas-er.md](docs/diagramas-er.md)):

| Servicio | Entidades (tablas) | Relaciones internas | Refs blandas |
|---|---|---|---|
| legacy | ClienteLegacy | — | — |
| clientes | Cliente, Direccion | 1—N | legacy_id → legacy |
| proveedores | Proveedor, DocumentoProveedor | 1—N | — |
| productos | Categoria, Producto, Oferta | 1—N, 1—N | proveedor_id → proveedores |
| ventas | Venta, DetalleVenta | 1—N | cliente_id; producto_id, proveedor_id (+ snapshots nombre/categoría/precio) |
| pagos | Pago, Comprobante, Reembolso | 1—1, 1—N | venta_id, cliente_id, ticket_id |
| despacho | Seguimiento, HistorialEstado | 1—N | venta_id, cliente_id, proveedor_id |
| tickets | Ticket, TicketMensaje | 1—N | venta_id, cliente_id, proveedor_id |
| feedback | Resena | — (UNIQUE compuesto) | producto_id, cliente_id, venta_id |
| notificaciones | Notificacion | — | destinatario_id, referencia_id |
| administrador | Administrador, AccionAdmin, ReporteSemanal, ReporteCategoria | 1—N, 1—N | referencia_id |

Normalización: dentro de cada BD las relaciones 1—N usan FK reales; los snapshots en `detalle_venta`
(nombre, categoría, precio) son desnormalización **deliberada y documentada** — congelan el dato histórico
de la transacción y permiten el reporte por categoría sin acoplar `administrador` a `productos`.

---

## 5. Estrategia Neon (decisión: un proyecto, 11 BDs)

- **Un proyecto Neon** (free plan vigente: 0,5 GB y 100 CU-h/mes por proyecto, scale-to-zero a los 5 min). En el branch principal se crean las 11 bases: `legacydb`, `clientesdb`, `proveedoresdb`, `productosdb`, `ventasdb`, `pagosdb`, `despachodb`, `ticketsdb`, `feedbackdb`, `notificacionesdb`, `administradordb`.
- Cumple la rúbrica: **base de datos independiente por servicio, sin tablas compartidas** (separación lógica; ningún servicio conoce la BD de otro).
- Conexión con el **connection string pooled** (`-pooler`, como el profe) + `sslmode=require&channel_binding=require`. Solo cambia el nombre de BD por servicio.
- HikariCP acotado: `maximum-pool-size=3`, `minimum-idle=1` (11 servicios × 3 = 33 conexiones máx., holgado para el pooler; el profe usa 5/2 para un servicio).
- `ddl-auto=update`: cada servicio crea sus tablas al arrancar. Seed de legacy por SQL en el editor de Neon.
- Credenciales en `application-secrets.properties` por servicio (gitignored); un `application-secrets.properties.example` versionado como plantilla.
- Ventaja demo: un solo compute → un solo cold-start. Riesgo aceptado: presupuesto de cómputo/almacenamiento compartido (sobra para carga académica).

---

## 6. Roadmap por fases

Orden dictado por las dependencias WebClient (se construye primero lo que no llama a nadie).
Cada fase termina en estado **demostrable**.

**F0 — Preparación** *(sin dependencias)*
Repo GitHub monorepo `Caso1/` (ramas por funcionalidad, commits descriptivos — Directrices). Proyecto Neon + 11 BDs + plantilla de secrets. Skills en `.claude/`. Validar arranque de un módulo semilla contra Neon.

**F1 — Servicios hoja** *(dep: F0)*
`legacy` (con seed 5.000) → `proveedores` → `notificaciones`. Sin WebClient saliente; CRUD + reglas locales + ProblemDetail.

**F2 — Identidad y catálogo** *(dep: F1)*
`clientes` (→ legacy: login con migración, **primera validación cruzada demostrable**) → `productos` (→ proveedores: solo APROBADO publica).

**F3 — Núcleo transaccional** *(dep: F2)*
`ventas` (→ clientes, productos: crear orden con comisión) → `pagos` (→ ventas) → `despacho` (→ ventas, notificaciones) → cerrar la **orquestación** `PATCH /ventas/{id}/pagar` (pago + seguimiento + aviso + descuento de stock). Hito crítico de la live demo.

**F4 — Post-venta** *(dep: F3)*
`tickets` (→ ventas, pagos, notificaciones: reclamo→mediación→reembolso) → `feedback` (→ ventas, productos: compra verificada).

**F5 — Administración** *(dep: F1, F3)*
`administrador`: aprobación de proveedores (cierra el ciclo del vendedor), auditoría, **reporte semanal por categoría** (agrega desde ventas).

**F6 — Integración y demo** *(dep: todas)*
Datos semilla coherentes de todo el flujo; guion de live demo (§7.5); verificación de la matriz de roles endpoint por endpoint; ER final vs tablas reales en Neon; README de arranque (orden de levantamiento, puertos).

**Guion de demo E2E (ítem 5):**
1. Ferretería Cóndor postula (`proveedores`) → admin aprueba (`administrador`→`proveedores`+`notificaciones`).
2. El vendedor publica taladro con oferta (`productos` valida APROBADO).
3. María (histórica) hace login → migración automática vía `legacy` (`clientes`).
4. María compra: `POST /ventas` (valida cliente+producto+stock, calcula comisión) → `PATCH /pagar` (orquesta pagos+despacho+notificaciones+stock).
5. El vendedor marca ENVIADO (`despacho`) → notificación de despacho.
6. Producto defectuoso: María abre ticket → mensajes → admin resuelve con reembolso (`tickets`→`pagos`).
7. María reseña el producto (`feedback`, compra verificada).
8. Admin genera el reporte semanal por categoría (`administrador`→`ventas`).

---

## 7. Checklist rúbrica EP2 → dónde se cubre

| Ítem (20% c/u) | Cómo se cumple | Dónde |
|---|---|---|
| 1. Arquitectura | **11** microservicios (≥10) con responsabilidad única; rutas `/api/v1` RESTful con jerarquía y subrecursos | §1 |
| 2. Comunicación | WebClient en **8** servicios con validación cruzada antes de escribir; orquestación de `ventas` como pieza central | §2, F3 |
| 3. Persistencia | 11 BDs Neon independientes sin tablas compartidas; ER normalizado por servicio; DTOs record + Bean Validation | §4, §5, docs/ |
| 4. Roles | Cliente / Proveedor / Administrador con ≥4 acciones exclusivas c/u, mapeadas a endpoints | §3 |
| 5. Funcionalidades | Flujo E2E completo del Caso 1 en guion de demo con todos los servicios arriba | §6 (F6) |

---

## 8. Backlog proyecto completo (fuera de EP2 — no puntúa en esta entrega)

| Ítem | Alcance futuro |
|---|---|
| API Gateway (8080) | Spring Cloud Gateway, rutas estáticas por servicio |
| Eureka | Service discovery + resolución por nombre en WebClient |
| Kafka | Proyecciones sincronizadas / eventos (p. ej. `VentaPagada` → despacho/notificaciones asíncronos) |
| Seguridad | BCrypt (clientes, administradores, legacy), login con JJWT, RBAC por rol, filtros |
| Documentación | springdoc-openapi (OpenAPI 3) con bean de info por servicio — **no** springfox |
| Pruebas | JUnit 5 + Mockito por capa (EA3) |
| Despliegue | Docker Compose local; exposición Ngrok o cloud (Railway/Render) con URL funcional |
| Mejoras de dominio | Carrito persistente, paginación en listados, correo real en notificaciones, anulación de ventas con reposición de stock |
