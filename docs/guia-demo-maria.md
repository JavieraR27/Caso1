# Guía de demo — Historia de usuario: Cliente histórico (María)

> **Historia (Caso 1, main.pdf):** *María ingresa con su correo habitual; el sistema valida sus
> credenciales contra la API legacy. Busca un taladro, elige entre ofertas de distintos
> vendedores, paga y sigue el estado del despacho desde su perfil. Cuando el producto llega
> defectuoso, abre un reclamo; el Administrador media la disputa y autoriza el reembolso.*

La historia recorre 8 microservicios: `legacy`, `clientes`, `proveedores`, `productos`,
`ventas`, `pagos`, `despacho`, `tickets` (+ `notificaciones` y `administrador` de apoyo).
Todos los comandos son copy-paste desde `Caso1/` (requieren `curl` y `jq`).

---

## 1. Requisitos previos

Ya configurado en este equipo (si es una máquina nueva, ver README):

- Java 21 + Maven; los 13 módulos compilados (`mvn -DskipTests package` en cada uno).
- `application-secrets.properties` en los 11 servicios apuntando al proyecto Neon
  (gitignored; **si cambias credenciales hay que recompilar**: el archivo viaja dentro del JAR).
- Las 11 BDs creadas en Neon y el seed de los 5.000 clientes legacy cargado
  (`legacy/src/main/resources/seed-clientes-legacy.sql` — credenciales `cliente<N>@paris.cl` / `pass<N>`).

## 2. Levantar el sistema

```bash
./scripts/start-all.sh     # eureka + 11 servicios; espera a que respondan (~40 s)
```

Verificación rápida:
- Eureka con los 11 registrados: http://localhost:8761
- Swagger de cualquier servicio: http://localhost:8084/swagger-ui/index.html
- El catálogo es público: `curl -s localhost:8084/api/v1/productos | jq .`

Para detener al final: `./scripts/stop-all.sh`

> Alternativa contenedores: `cp .env.example .env` (completar Neon) y `docker compose up --build`.

## 3. Preparar el escenario (una sola vez)

María debe poder **elegir entre ofertas de distintos vendedores**, así que necesitamos un
administrador y **dos vendedores aprobados con un taladro cada uno**. Si un POST responde
**409 es que ya existe** (quedó de una demo anterior): continúa con el paso siguiente.

```bash
# 3.1 Administrador (bootstrap) y su token
curl -s -X POST localhost:8091/api/v1/administradores -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123","nombre":"Admin Paris","email":"admin@paris.cl"}' | jq .
TOKEN_ADMIN=$(curl -s -X POST localhost:8091/api/v1/administradores/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

# 3.2 Vendedor 1: Ferretería Cóndor postula → admin aprueba → login
curl -s -X POST localhost:8083/api/v1/proveedores -H 'Content-Type: application/json' \
  -d '{"rut":"76543210-K","razonSocial":"Ferretería Cóndor","email":"condor@ferreteria.cl","password":"condor123","telefono":"+56911111111"}' | jq '{id, estado}'
curl -s -X POST localhost:8091/api/v1/admin/proveedores/1/aprobacion -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' -d '{"adminId":1,"aprobado":true,"observaciones":"documentación en regla"}' | jq '{tipo, referenciaId}'
TOKEN_CONDOR=$(curl -s -X POST localhost:8083/api/v1/proveedores/login -H 'Content-Type: application/json' \
  -d '{"email":"condor@ferreteria.cl","password":"condor123"}' | jq -r .token)

# 3.3 Vendedor 2: Importadora Toolmax (mismo ciclo)
curl -s -X POST localhost:8083/api/v1/proveedores -H 'Content-Type: application/json' \
  -d '{"rut":"77888999-0","razonSocial":"Importadora Toolmax","email":"ventas@toolmax.cl","password":"toolmax123","telefono":"+56922222222"}' | jq '{id, estado}'
curl -s -X POST localhost:8091/api/v1/admin/proveedores/2/aprobacion -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' -d '{"adminId":1,"aprobado":true,"observaciones":"ok"}' | jq '{tipo, referenciaId}'
TOKEN_TOOLMAX=$(curl -s -X POST localhost:8083/api/v1/proveedores/login -H 'Content-Type: application/json' \
  -d '{"email":"ventas@toolmax.cl","password":"toolmax123"}' | jq -r .token)

# 3.4 Categoría (admin) y un taladro por vendedor
curl -s -X POST localhost:8084/api/v1/categorias -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' -d '{"nombre":"Herramientas"}' | jq .
curl -s -X POST localhost:8084/api/v1/productos -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' \
  -d '{"proveedorId":1,"categoriaId":1,"nombre":"Taladro percutor 750W","descripcion":"Con maletín","precio":50000,"stock":10}' | jq '{id, nombre, precio}'
curl -s -X POST localhost:8084/api/v1/productos -H "Authorization: Bearer $TOKEN_TOOLMAX" \
  -H 'Content-Type: application/json' \
  -d '{"proveedorId":2,"categoriaId":1,"nombre":"Taladro inalámbrico 20V","descripcion":"2 baterías","precio":45000,"stock":5}' | jq '{id, nombre, precio}'

# 3.5 Oferta del 20% en el taladro de Cóndor (precio vigente: 40.000)
curl -s -X POST localhost:8084/api/v1/productos/1/ofertas -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' \
  -d "{\"tipoOferta\":\"PORCENTAJE\",\"valor\":20,\"fechaInicio\":\"$(date +%F)\",\"fechaFin\":\"$(date -d '+7 days' +%F)\"}" | jq .
```

---

## 4. La historia de María, paso a paso

### Acto 1 — "Ingresa con su correo habitual" (validación contra la API legacy)

María nunca se registró en el marketplace: existe solo en el sistema legacy (fila 2 del seed).
Su primer login valida las credenciales contra `legacy` vía WebClient y la **migra**
automáticamente, sin re-registrarse.

> Si `cliente2` ya fue migrado en una corrida anterior (el login devolvería la cuenta local,
> sin pasar por el legacy), usa el siguiente histórico disponible: `cliente3@paris.cl`/`pass3`,
> `cliente4`/`pass4`, etc. — hay 5.000 en el seed.

```bash
LOGIN=$(curl -s -X POST localhost:8082/api/v1/clientes/login -H 'Content-Type: application/json' \
  -d '{"email":"cliente2@paris.cl","password":"pass2"}')
echo $LOGIN | jq .cliente
TOKEN_MARIA=$(echo $LOGIN | jq -r .token)
MARIA_ID=$(echo $LOGIN | jq -r .cliente.id)
```

**Qué mirar:** `"tipo": "MIGRADO"` y `"legacyId": 2` — el perfil se creó desde el legacy y
conserva la referencia. El `token` trae el rol CLIENTE. Un segundo login ya no llama al
legacy: usa la cuenta local (password re-almacenada con BCrypt).

### Acto 2 — "Busca un taladro, elige entre ofertas de distintos vendedores"

El catálogo es público. María compara los dos taladros y su reputación:

```bash
curl -s 'localhost:8084/api/v1/productos?categoria=Herramientas' \
  | jq '.[] | {id, nombre, proveedorId, precio, precioVigente, stock}'
curl -s localhost:8089/api/v1/resenas/producto/1/promedio | jq .
curl -s localhost:8089/api/v1/resenas/producto/2/promedio | jq .
```

**Qué mirar:** el taladro de Cóndor (id 1) cuesta 50.000 pero su **precio vigente es 40.000**
por la oferta del 20%; el de Toolmax (id 2) vale 45.000. María elige el de Cóndor.

### Acto 3 — "Paga"

La compra son dos pasos: crear la orden (valida cliente, producto y stock; calcula la
comisión del 10% en servidor) y pagarla (la **orquestación**: descuenta stock, registra el
pago con comprobante, crea el seguimiento y notifica).

```bash
VENTA=$(curl -s -X POST localhost:8085/api/v1/ventas -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d "{\"clienteId\":$MARIA_ID,\"detalles\":[{\"productoId\":1,\"cantidad\":1}]}")
echo $VENTA | jq '{id, estado, montoTotal, comisionTotal, detalles}'
VENTA_ID=$(echo $VENTA | jq -r .id)

curl -s -X PATCH localhost:8085/api/v1/ventas/$VENTA_ID/pagar -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d '{"medioPago":"TARJETA","direccionEntrega":"Av. Providencia 123, Santiago"}' | jq '{id, estado, montoTotal}'
```

**Qué mirar:** `montoTotal: 40000` (precio vigente, no el de lista), `comisionTotal: 4000`,
y en el detalle los **snapshots** (nombre, categoría, precio). Tras pagar:

```bash
curl -s localhost:8084/api/v1/productos/1 | jq '{stock}'                                    # bajó en 1
curl -s "localhost:8086/api/v1/pagos?ventaId=$VENTA_ID" -H "Authorization: Bearer $TOKEN_MARIA" | jq '.[0] | {id, monto, estado}'
PAGO_ID=$(curl -s "localhost:8086/api/v1/pagos?ventaId=$VENTA_ID" -H "Authorization: Bearer $TOKEN_MARIA" | jq -r '.[0].id')
curl -s localhost:8086/api/v1/pagos/$PAGO_ID/comprobante -H "Authorization: Bearer $TOKEN_MARIA" | jq .   # folio F-...
```

### Acto 4 — "Sigue el estado del despacho desde su perfil"

```bash
curl -s "localhost:8087/api/v1/envios?clienteId=$MARIA_ID" -H "Authorization: Bearer $TOKEN_MARIA" \
  | jq '.[0] | {id, estadoActual, numeroSeguimiento, direccionEntrega}'
ENVIO_ID=$(curl -s "localhost:8087/api/v1/envios?clienteId=$MARIA_ID" -H "Authorization: Bearer $TOKEN_MARIA" | jq -r '.[0].id')
```

El vendedor prepara y despacha (acciones exclusivas del rol PROVEEDOR):

```bash
curl -s -X PATCH localhost:8087/api/v1/envios/$ENVIO_ID/estado -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' -d '{"estado":"ENVIADO","comentario":"Retirado por courier"}' | jq '{estadoActual}'
curl -s -X PATCH localhost:8087/api/v1/envios/$ENVIO_ID/estado -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' -d '{"estado":"ENTREGADO","comentario":"Recibido conforme"}' | jq '{estadoActual}'
```

María revisa la trazabilidad y su bandeja de avisos:

```bash
curl -s localhost:8087/api/v1/envios/$ENVIO_ID/historial -H "Authorization: Bearer $TOKEN_MARIA" \
  | jq '.[] | {estado, comentario, fechaCambio}'
curl -s "localhost:8090/api/v1/notificaciones?destinatarioTipo=CLIENTE&destinatarioId=$MARIA_ID" \
  -H "Authorization: Bearer $TOKEN_MARIA" | jq '.[] | {tipo, asunto}'
```

**Qué mirar:** historial `PENDIENTE → ENVIADO → ENTREGADO` con fechas, y las notificaciones
`VENTA_CONFIRMADA` y `DESPACHO`. (Al pasar a ENVIADO, `despacho` avisó a `notificaciones`
por WebClient.)

### Acto 5 — "El producto llega defectuoso: abre un reclamo"

```bash
TICKET=$(curl -s -X POST localhost:8088/api/v1/tickets -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d "{\"ventaId\":$VENTA_ID,\"categoria\":\"PRODUCTO_DEFECTUOSO\",\"asunto\":\"Taladro no enciende\",\"descripcion\":\"El gatillo llegó trabado, no gira\"}")
echo $TICKET | jq '{id, estado, clienteId, proveedorId}'
TICKET_ID=$(echo $TICKET | jq -r .id)
```

**Qué mirar:** `tickets` validó contra `ventas` que la compra es real y tomó de ella el
cliente y el proveedor (María no los envió).

### Acto 6 — "El Administrador media la disputa y autoriza el reembolso"

Mediación (el hilo pasa a EN_MEDIACION cuando interviene el admin):

```bash
curl -s -X POST localhost:8088/api/v1/tickets/$TICKET_ID/mensajes -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' -d '{"autorRol":"ADMINISTRADOR","mensaje":"Estamos revisando el caso con el vendedor"}' | jq '{autorRol, mensaje}'
curl -s -X POST localhost:8088/api/v1/tickets/$TICKET_ID/mensajes -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' -d '{"autorRol":"PROVEEDOR","mensaje":"Aceptamos la devolución, lote con falla"}' | jq '{autorRol, mensaje}'
```

Resolución con reembolso (solo el ADMINISTRADOR puede):

```bash
curl -s -X PATCH localhost:8088/api/v1/tickets/$TICKET_ID/resolver -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"estado":"RESUELTO","resolucion":"Falla de fábrica confirmada por el vendedor; se reembolsa el total","reembolsoAutorizado":true}' \
  | jq '{estado, reembolsoAutorizado, resolucion}'
```

Constancia del dinero devuelto y aviso a María:

```bash
curl -s localhost:8086/api/v1/pagos/$PAGO_ID/reembolsos -H "Authorization: Bearer $TOKEN_ADMIN" | jq '.[0] | {monto, estado, ticketId}'
curl -s localhost:8086/api/v1/pagos/$PAGO_ID -H "Authorization: Bearer $TOKEN_ADMIN" | jq '{estado}'
curl -s "localhost:8090/api/v1/notificaciones?destinatarioId=$MARIA_ID&tipo=RESOLUCION_RECLAMO" -H "Authorization: Bearer $TOKEN_MARIA" | jq '.[0].mensaje'
```

**Qué mirar:** `tickets` invocó a `pagos` (reembolso `PROCESADO` por el monto completo, con
la referencia al ticket), el pago quedó `REEMBOLSADO` y María recibió la notificación.

---

## 5. Extensiones opcionales (mismos datos)

- María reseña el taladro (compra verificada): `POST /api/v1/resenas` con su token.
- El admin genera el reporte semanal por categoría: `POST /api/v1/admin/reportes/semanal`.
- RBAC en vivo: repetir cualquier paso sin token (401/403) o con el rol equivocado (403).

## 6. Cierre y reinicio de datos

```bash
./scripts/stop-all.sh
```

Para repetir la demo desde cero (borra TODO el negocio, conserva el seed legacy):
en el SQL Editor de Neon, por cada BD de negocio (`clientesdb`, `proveedoresdb`,
`productosdb`, `ventasdb`, `pagosdb`, `despachodb`, `ticketsdb`, `feedbackdb`,
`notificacionesdb`, `administradordb`):

```sql
DO $$ DECLARE t text;
BEGIN
  FOR t IN SELECT tablename FROM pg_tables WHERE schemaname='public' LOOP
    EXECUTE 'TRUNCATE TABLE ' || quote_ident(t) || ' RESTART IDENTITY CASCADE';
  END LOOP;
END $$;
```

(O usar otro cliente histórico — `cliente3@paris.cl`/`pass3`, etc. — y dejar que los ids
avancen.)
