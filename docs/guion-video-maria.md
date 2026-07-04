# Guión de video — Historia de María, endpoint por endpoint (Swagger)

> Historia (Caso 1): *María ingresa con su correo habitual; el sistema valida sus credenciales
> contra la API legacy. Busca un taladro, elige entre ofertas de distintos vendedores, paga y
> sigue el estado del despacho desde su perfil. Cuando el producto llega defectuoso, abre un
> reclamo; el Administrador media la disputa y autoriza el reembolso.*

Todo el video se graba en **una sola pestaña** del Swagger unificado:

- **Nube:** https://paris-gateway.onrender.com/swagger-ui.html
- **Local (idéntico):** http://localhost:8080/swagger-ui.html

Se cambia de servicio con el desplegable **"Select a definition"** (arriba a la derecha).
El token pegado en **Authorize** se conserva al cambiar de definición; solo hay que
re-autorizar cuando cambia el actor (María → vendedor → admin).

## Reparto y credenciales

| Actor | Login | Credenciales |
|---|---|---|
| **María** (cliente histórica) | `clientes` → `POST /api/v1/clientes/login` | `cliente30@paris.cl` / `pass30` |
| **Ferretería Cóndor** (vendedor) | `proveedores` → `POST /api/v1/proveedores/login` | `condor@ferreteria.cl` / `condor123` |
| **Admin Paris** | `administrador` → `POST /api/v1/administradores/login` | `admin` / `admin123` |

> María debe ser un `cliente<N>` del seed legacy que **nunca haya iniciado sesión** (para que
> la migración ocurra en cámara). `cliente30` está libre; si por algún motivo diera cuenta ya
> existente, usa `cliente31`/`pass31`, `cliente32`/`pass32`, etc. **No pruebes el login antes
> de grabar**: el primer login ES la migración.

## Valores que hay que ir anotando (post-it o bloc de notas)

| Variable | Sale de | Se usa en |
|---|---|---|
| `TOKEN_MARIA` | login de María (Escena 1) | Escenas 3, 4, 6, 8 |
| `MARIA_ID` | `cliente.id` del login (Escena 1) | Escenas 3 y 4 |
| `VENTA_ID` | crear venta (Escena 3) | pagar, pagos, ticket |
| `PAGO_ID` | listar pagos (Escena 3) | comprobante y reembolso |
| `ENVIO_ID` | listar envíos (Escena 4) | cambiar estado e historial |
| `TICKET_ID` | abrir reclamo (Escena 5) | mensajes y resolución |
| `TOKEN_CONDOR` / `TOKEN_ADMIN` | sus logins | Escenas 4 y 6 |

---

## Antes de grabar (checklist, NO sale en el video)

1. **Pre-calentar los 12 servicios** (plan free: dormidos tras ~15 min; despiertan en ~1 min):

   ```bash
   for s in legacy clientes proveedores productos ventas pagos despacho tickets feedback notificaciones administrador; do
     curl -s -o /dev/null -w "paris-$s: %{http_code}\n" --max-time 90 https://paris-$s.onrender.com/v3/api-docs &
   done
   curl -s -o /dev/null -w "paris-gateway: %{http_code}\n" --max-time 90 https://paris-gateway.onrender.com/v3/api-docs/swagger-config &
   wait
   ```

   Repetir hasta que **los 12 den 200**. Si la grabación se pausa más de 15 minutos, volver a correrlo.

2. **Verificar el escenario** (sin tocar nada): abrir la definición `productos (catálogo)` →
   `GET /api/v1/productos` con `categoria=Herramientas`. Deben aparecer los **dos taladros**:

   | id | Producto | Vendedor | Precio lista | Precio vigente |
   |---|---|---|---|---|
   | 1 | Taladro percutor 750W | 1 (Cóndor) | 50.000 | **40.000** (oferta 20%) |
   | 2 | Taladro inalámbrico 20V | 2 (Toolmax) | 45.000 | 45.000 |

   - Si el producto 1 muestra `precioVigente: 50000`, la oferta expiró (**la actual vence el
     2026-07-09**) → recrearla con el Apéndice A.
   - Si el producto 1 tiene `stock: 0` → subirlo con el Apéndice A.
   - Si no hay taladros (BD truncada) → montar el escenario completo con el Apéndice B.

3. Dejar el Swagger abierto en la definición `clientes (login María)`, con este guión y el
   bloc de notas en otra pantalla.

---

## Escena 0 — Presentación (~20 s, sin ejecutar nada)

**Mostrar:** el Swagger unificado con el desplegable abierto (los 11 servicios).

**Decir:** "Este es el marketplace de Paris: 11 microservicios Spring Boot desplegados en
Render, cada uno con su base de datos en Neon. Entramos por un solo punto: el API Gateway,
que además unifica la documentación OpenAPI de todo el sistema. Vamos a recorrer la historia
de María, una clienta histórica de Paris."

---

## Escena 1 — "María ingresa con su correo habitual" (migración desde el legacy)

**Definición:** `clientes (login María)`

1. Expandir `POST /api/v1/clientes/login` → **Try it out** → body:

   ```json
   {"email": "cliente30@paris.cl", "password": "pass30"}
   ```

   → **Execute**.

2. **Mostrar en la respuesta (200):**
   - `"tipo": "MIGRADO"` y `"legacyId": 30` → María no existía en el marketplace: `clientes`
     validó sus credenciales **contra la API legacy por WebClient** y la migró automáticamente,
     sin re-registrarse. Su clave quedó re-almacenada con BCrypt.
   - El `token` (rol CLIENTE) y el `cliente.id` → **anotar ambos** (`TOKEN_MARIA`, `MARIA_ID`).

3. Botón **Authorize** (candado, arriba a la derecha) → pegar `TOKEN_MARIA`
   (**sin** el prefijo `Bearer `) → Authorize → Close.

**Decir:** "María entra con el mismo correo de siempre. El sistema no la encuentra localmente,
valida contra el sistema legacy de Paris —donde hay 5.000 clientes históricos— y la migra en
el acto. Desde ahora es una clienta más del marketplace."

---

## Escena 2 — "Busca un taladro y compara ofertas" (catálogo público)

**Definición:** `productos (catálogo)` — no requiere token (decirlo).

1. `GET /api/v1/productos` → Try it out → parámetro `categoria` = `Herramientas` → Execute.
   **Mostrar:** dos taladros **de vendedores distintos**; el de Cóndor (id 1) lista a 50.000
   pero `precioVigente: 40000` por la **oferta del 20%**; el de Toolmax (id 2) vale 45.000.

2. (Opcional) `GET /api/v1/productos/1` para ver el detalle con la oferta aplicada.

**Definición:** `feedback (reseñas)` — también público.

3. `GET /api/v1/resenas/producto/{productoId}/promedio` con `productoId` = 1 → Execute.
   (Repetir con 2 si se quiere comparar reputación.)

**Decir:** "El catálogo y las reseñas son públicos, no piden token. María compara los dos
taladros: el percutor de Ferretería Cóndor tiene una oferta del 20% y queda en 40.000 —el
precio vigente lo calcula el servidor, no el cliente—. Se decide por ese."

---

## Escena 3 — "Paga" (la orquestación)

**Definición:** `ventas (orquestador)` — con `TOKEN_MARIA` ya autorizada.

1. `POST /api/v1/ventas` → Try it out → body (usar el `MARIA_ID` anotado):

   ```json
   {"clienteId": MARIA_ID, "detalles": [{"productoId": 1, "cantidad": 1}]}
   ```

   **Mostrar:** estado `CREADA`, `montoTotal: 40000` (el precio **vigente**, no el de
   lista), `comisionTotal: 4000` (10% calculado **en el servidor**) y el detalle con el
   **snapshot** del producto (nombre, categoría, precio al momento de la compra).
   → **anotar `VENTA_ID`** (campo `id`).

2. `PATCH /api/v1/ventas/{id}/pagar` → `id` = `VENTA_ID` → body:

   ```json
   {"medioPago": "TARJETA", "direccionEntrega": "Av. Providencia 123, Santiago"}
   ```

   **Mostrar:** estado pasa a `PAGADA`.

**Decir:** "Al pagar, ventas orquesta cuatro servicios por WebClient: descuenta stock en
productos, registra el pago con su comprobante en pagos, crea el seguimiento del envío en
despacho y deja el aviso en notificaciones. Veámoslo."

**Evidencias de la orquestación (rápido, una por definición):**

3. `productos (catálogo)`: `GET /api/v1/productos/1` → **stock bajó en 1** (público).
4. `pagos`: `GET /api/v1/pagos` con `ventaId` = `VENTA_ID` → pago `PAGADO` por 40.000
   → **anotar `PAGO_ID`**.
5. `pagos`: `GET /api/v1/pagos/{id}/comprobante` con `PAGO_ID` → **folio `F-...`** (boleta).

---

## Escena 4 — "Sigue el estado del despacho desde su perfil"

**Definición:** `despacho` — aún con `TOKEN_MARIA`.

1. `GET /api/v1/envios` con `clienteId` = `MARIA_ID` → **mostrar** el envío `PENDIENTE` con
   `numeroSeguimiento` y la dirección de entrega → **anotar `ENVIO_ID`**.

**Cambio de actor → el vendedor despacha** (acciones exclusivas del rol PROVEEDOR):

2. Definición `proveedores`: `POST /api/v1/proveedores/login` (público) → body:

   ```json
   {"email": "condor@ferreteria.cl", "password": "condor123"}
   ```

   → copiar token → **Authorize → Logout → pegar `TOKEN_CONDOR`**.

3. Definición `despacho`: `PATCH /api/v1/envios/{id}/estado` con `ENVIO_ID` → body:

   ```json
   {"estado": "ENVIADO", "comentario": "Retirado por courier"}
   ```

4. Repetir con:

   ```json
   {"estado": "ENTREGADO", "comentario": "Recibido conforme"}
   ```

**Vuelve María** (Authorize → Logout → pegar `TOKEN_MARIA`):

5. `GET /api/v1/envios/{id}/historial` → **mostrar la trazabilidad**
   `PENDIENTE → ENVIADO → ENTREGADO` con fecha y comentario de cada cambio.
6. Definición `notificaciones`: `GET /api/v1/notificaciones` con
   `destinatarioTipo` = `CLIENTE`, `destinatarioId` = `MARIA_ID` → **mostrar** los avisos
   `VENTA_CONFIRMADA` y `DESPACHO`.

**Decir:** "María sigue su pedido desde el perfil. Cuando el vendedor lo marca como enviado,
despacho avisa a notificaciones por WebClient y a María le llega el aviso. Cada cambio de
estado queda trazado con fecha."

---

## Escena 5 — "El producto llega defectuoso: abre un reclamo"

**Definición:** `tickets (reclamos)` — con `TOKEN_MARIA`.

1. `POST /api/v1/tickets` → body:

   ```json
   {
     "ventaId": VENTA_ID,
     "categoria": "PRODUCTO_DEFECTUOSO",
     "asunto": "Taladro no enciende",
     "descripcion": "El gatillo llegó trabado, no gira"
   }
   ```

2. **Mostrar:** el ticket nace `ABIERTO` y trae `clienteId` y `proveedorId` **que María no
   envió**: `tickets` validó contra `ventas` que la compra es real y los tomó de ahí
   → **anotar `TICKET_ID`**.

**Decir:** "María solo indica la venta y el problema. Tickets verifica contra ventas que la
compra existe y asocia solo al vendedor y cliente reales — nadie puede reclamar por compras
ajenas."

---

## Escena 6 — "El Administrador media la disputa y autoriza el reembolso"

**Cambio de actor → entra el admin:**

1. Definición `administrador`: `POST /api/v1/administradores/login` (público) → body:

   ```json
   {"username": "admin", "password": "admin123"}
   ```

   → **Authorize → Logout → pegar `TOKEN_ADMIN`**.

2. Definición `tickets (reclamos)`: `POST /api/v1/tickets/{id}/mensajes` con `TICKET_ID` →

   ```json
   {"autorRol": "ADMINISTRADOR", "mensaje": "Estamos revisando el caso con el vendedor"}
   ```

   **Mostrar:** el hilo pasa a `EN_MEDIACION` cuando interviene el admin.

3. *(Opcional, muestra el hilo de 3 actores)*: Authorize con `TOKEN_CONDOR` →

   ```json
   {"autorRol": "PROVEEDOR", "mensaje": "Aceptamos la devolución, lote con falla"}
   ```

   → y volver a `TOKEN_ADMIN`.

4. `PATCH /api/v1/tickets/{id}/resolver` con `TICKET_ID` → body:

   ```json
   {
     "estado": "RESUELTO",
     "resolucion": "Falla de fábrica confirmada por el vendedor; se reembolsa el total",
     "reembolsoAutorizado": true
   }
   ```

   **Mostrar:** `RESUELTO` y `reembolsoAutorizado: true`. **Decir:** "Solo el rol
   ADMINISTRADOR puede resolver; al autorizar el reembolso, tickets invoca a pagos."

**Evidencias del reembolso:**

5. Definición `pagos` (con `TOKEN_ADMIN`): `GET /api/v1/pagos/{id}/reembolsos` con `PAGO_ID`
   → reembolso `PROCESADO` por el monto completo, **con la referencia al `ticketId`**.
6. `GET /api/v1/pagos/{id}` → el pago quedó `REEMBOLSADO`.

**Cierre con María** (Authorize con `TOKEN_MARIA`):

7. Definición `notificaciones`: `GET /api/v1/notificaciones` con `destinatarioId` = `MARIA_ID`,
   `tipo` = `RESOLUCION_RECLAMO` → **mostrar el mensaje final** que recibió María.

---

## Escena 7 (opcional) — RBAC en vivo (~30 s)

Con `TOKEN_MARIA` autorizada, repetir `PATCH /api/v1/tickets/{id}/resolver` → **403**.

**Decir:** "La misma operación con el token de María es rechazada: cada endpoint valida el
rol del JWT. Cliente, proveedor y administrador solo pueden hacer lo suyo."

*(Alternativa: Authorize → Logout sin pegar nada y ejecutar cualquier endpoint protegido → 401/403 sin token.)*

## Escena 8 (opcional) — María reseña el taladro

Definición `feedback (reseñas)`, con `TOKEN_MARIA`: `POST /api/v1/resenas` → body:

```json
{"productoId": 1, "clienteId": MARIA_ID, "ventaId": VENTA_ID, "calificacion": 4, "comentario": "Buen taladro; la posventa respondió muy bien"}
```

**Decir:** "Solo puede reseñar quien compró: feedback verifica la compra contra ventas."

---

## Apéndice A — Reparar el escenario (oferta vencida o sin stock)

Con `TOKEN_CONDOR` (login del paso 4.2), definición `productos (catálogo)`:

- **Recrear la oferta del 20%** — `POST /api/v1/productos/1/ofertas` (ajustar fechas al día
  de la grabación):

  ```json
  {"tipoOferta": "PORCENTAJE", "valor": 20, "fechaInicio": "2026-07-04", "fechaFin": "2026-07-18"}
  ```

  > Si responde 409 hay una oferta activa todavía (una activa por producto): no hace falta nada.

- **Reponer stock** — `PUT /api/v1/productos/1`:

  ```json
  {"proveedorId": 1, "categoriaId": 1, "nombre": "Taladro percutor 750W", "descripcion": "Con maletín", "precio": 50000, "stock": 10}
  ```

## Apéndice B — Escenario desde cero (solo si la BD fue truncada)

Es la sección 3 de [guia-demo-maria.md](guia-demo-maria.md) (admin bootstrap, dos vendedores
postulados y aprobados, categoría Herramientas, un taladro por vendedor y la oferta del 20%).
Se puede correr por Swagger con los mismos payloads o por curl copy-paste desde esa guía.
Regla general: un **409 significa que ya existe** — continuar con el paso siguiente.

## Apéndice C — Si algo falla en cámara

| Síntoma | Causa probable | Salida |
|---|---|---|
| 503 o timeout largo | un servicio del free tier estaba dormido | re-ejecutar el mismo request (el primero lo despertó); pre-calentar de nuevo si hubo pausa |
| 401/403 en endpoint protegido | token no cargado, pegado con `Bearer `, o rol equivocado | Authorize → Logout → pegar el token del actor correcto |
| 403 "a propósito" | rol sin permiso | es el RBAC: úsalo como Escena 7 |
| 409 al crear algo | ya existía de una corrida anterior | seguir con el paso siguiente |
| login de María devuelve cuenta ya migrada | ese `cliente<N>` ya se usó | subir al siguiente: `cliente31`/`pass31`, `cliente32`/`pass32`… |
