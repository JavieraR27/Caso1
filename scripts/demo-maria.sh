#!/bin/bash
# ============================================================================
# DEMO EN VIVO — Historia de usuario: Cliente histórico (María)
# Caso 1 · Marketplace Almacenes Paris · DSY1103 EP3
#
# Uso:
#   ./scripts/demo-maria.sh              → contra Render (nube), con pausas
#   ./scripts/demo-maria.sh local        → contra localhost (start-all.sh antes)
#   ./scripts/demo-maria.sh nube 45      → usar cliente45@paris.cl como María
#
# Pausa entre actos con Enter (si se ejecuta sin terminal, corre de un tirón).
# Cada corrida usa un cliente legacy NUEVO (contador en scripts/.demo-cliente-n)
# para mostrar la migración en vivo, y repone stock si se está agotando.
# Requiere: curl y jq.
# ============================================================================
set -u
cd "$(dirname "$0")"

MODO=${1:-nube}
if [ "$MODO" = "local" ]; then
  U_LEGACY=http://localhost:8081;  U_CLIENTES=http://localhost:8082
  U_PROVEEDORES=http://localhost:8083; U_PRODUCTOS=http://localhost:8084
  U_VENTAS=http://localhost:8085;  U_PAGOS=http://localhost:8086
  U_DESPACHO=http://localhost:8087; U_TICKETS=http://localhost:8088
  U_FEEDBACK=http://localhost:8089; U_NOTIFICACIONES=http://localhost:8090
  U_ADMIN=http://localhost:8091
else
  U_LEGACY=https://paris-legacy.onrender.com
  U_CLIENTES=https://paris-clientes.onrender.com
  U_PROVEEDORES=https://paris-proveedores.onrender.com
  U_PRODUCTOS=https://paris-productos.onrender.com
  U_VENTAS=https://paris-ventas.onrender.com
  U_PAGOS=https://paris-pagos.onrender.com
  U_DESPACHO=https://paris-despacho.onrender.com
  U_TICKETS=https://paris-tickets.onrender.com
  U_FEEDBACK=https://paris-feedback.onrender.com
  U_NOTIFICACIONES=https://paris-notificaciones.onrender.com
  U_ADMIN=https://paris-administrador.onrender.com
fi

# Cliente legacy de esta corrida (uno nuevo por demo, para migrar en vivo)
N=${2:-$(cat .demo-cliente-n 2>/dev/null || echo 30)}
MARIA_EMAIL="cliente${N}@paris.cl"; MARIA_PASS="pass${N}"

C() { curl -s --max-time 90 "$@"; }

pausa() { if [ -t 0 ]; then echo; read -rp "───────── Enter para continuar ─────────"; echo; fi; }

titulo() { echo; echo "════════════════════════════════════════════════════════════"; echo "  $1"; echo "════════════════════════════════════════════════════════════"; }

peticion() { echo "  → $1"; }   # narración de la llamada que se está haciendo

fallo() { echo; echo "✘ ABORTADO: $1"; exit 1; }

# ============================================================================
titulo "0. PRE-CALENTADO — despierta los 11 servicios (free tier duerme a los 15 min)"
SERVICIOS_URL="$U_LEGACY $U_CLIENTES $U_PROVEEDORES $U_PRODUCTOS $U_VENTAS $U_PAGOS $U_DESPACHO $U_TICKETS $U_FEEDBACK $U_NOTIFICACIONES $U_ADMIN"
for ronda in 1 2 3 4 5 6; do
  up=0
  for u in $SERVICIOS_URL; do
    code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 120 "$u/v3/api-docs" 2>/dev/null)
    [ "$code" = "200" ] && up=$((up+1))
  done
  echo "  ronda $ronda: $up/11 despiertos"
  [ $up -eq 11 ] && break
  sleep 10
done
[ $up -eq 11 ] || fallo "no despertaron los 11 servicios; revisa el dashboard de Render"
echo "✔ Los 11 servicios responden."

# ============================================================================
titulo "0.b VERIFICACIÓN DEL ESCENARIO — admin, vendedores, taladros y oferta"

peticion "POST $U_ADMIN/api/v1/administradores/login  (admin/admin123)"
TOKEN_ADMIN=$(C -X POST $U_ADMIN/api/v1/administradores/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token // empty')
[ -n "$TOKEN_ADMIN" ] || fallo "no hay admin; crea el escenario con docs/guia-demo-maria.md §3"

peticion "POST $U_PROVEEDORES/api/v1/proveedores/login  (Ferretería Cóndor)"
TOKEN_CONDOR=$(C -X POST $U_PROVEEDORES/api/v1/proveedores/login -H 'Content-Type: application/json' \
  -d '{"email":"condor@ferreteria.cl","password":"condor123"}' | jq -r '.token // empty')
[ -n "$TOKEN_CONDOR" ] || fallo "no hay vendedor Cóndor aprobado; crea el escenario con docs/guia-demo-maria.md §3"

PROD=$(C $U_PRODUCTOS/api/v1/productos/1)
PRECIO=$(echo "$PROD" | jq -r '.precio // empty')
[ -n "$PRECIO" ] || fallo "no existe el producto 1; crea el escenario con docs/guia-demo-maria.md §3"
VIGENTE=$(echo "$PROD" | jq -r .precioVigente)
STOCK=$(echo "$PROD" | jq -r .stock)

if [ "$STOCK" -lt 2 ]; then
  echo "  (stock bajo: $STOCK — Cóndor repone a 10 unidades)"
  C -X PUT $U_PRODUCTOS/api/v1/productos/1 -H "Authorization: Bearer $TOKEN_CONDOR" \
    -H 'Content-Type: application/json' \
    -d "$(echo "$PROD" | jq -c '{nombre, descripcion, precio, stock: 10, estado}')" > /dev/null
  STOCK=10
fi
if [ "$VIGENTE" = "$PRECIO" ]; then
  echo "  ⚠ El producto 1 no tiene oferta vigente (precio de lista $PRECIO)."
  echo "    Si la oferta anterior expiró: en Neon → productosdb:"
  echo "    UPDATE ofertas SET activa=false;  y volver a correr la demo (creará una nueva)."
  C -X POST $U_PRODUCTOS/api/v1/productos/1/ofertas -H "Authorization: Bearer $TOKEN_CONDOR" \
    -H 'Content-Type: application/json' \
    -d "{\"tipoOferta\":\"PORCENTAJE\",\"valor\":20,\"fechaInicio\":\"$(date +%F)\",\"fechaFin\":\"$(date -d '+7 days' +%F)\"}" > /dev/null
  VIGENTE=$(C $U_PRODUCTOS/api/v1/productos/1 | jq -r .precioVigente)
fi
echo "✔ Escenario listo: producto 1 a \$$VIGENTE vigente (lista \$$PRECIO), stock $STOCK."
echo "✔ María de esta corrida: $MARIA_EMAIL / $MARIA_PASS (legacy #$N, aún no migrada)"
pausa

# ============================================================================
titulo "ACTO 1 — María entra con su correo habitual (validación contra la API legacy)"
echo "  María existe SOLO en el sistema legacy (5.000 históricos). Su primer login"
echo "  valida contra legacy vía WebClient y la MIGRA sin re-registrarse."
echo
peticion "POST $U_CLIENTES/api/v1/clientes/login  {\"email\":\"$MARIA_EMAIL\",\"password\":\"$MARIA_PASS\"}"
LOGIN=$(C -X POST $U_CLIENTES/api/v1/clientes/login -H 'Content-Type: application/json' \
  -d "{\"email\":\"$MARIA_EMAIL\",\"password\":\"$MARIA_PASS\"}")
echo "$LOGIN" | jq '.cliente'
TOKEN_MARIA=$(echo "$LOGIN" | jq -r '.token // empty')
MARIA_ID=$(echo "$LOGIN" | jq -r '.cliente.id // empty')
[ -n "$TOKEN_MARIA" ] || fallo "login de María falló (¿cliente$N ya migrado? corre con: $0 $MODO $((N+1)))"
echo
echo "  ★ DESTACAR: tipo MIGRADO + legacyId $N → validó contra la API legacy."
echo "  ★ El token JWT trae el rol CLIENTE (se usa en todo lo que sigue)."
pausa

# ============================================================================
titulo "ACTO 2 — Busca un taladro y elige entre ofertas de distintos vendedores"
peticion "GET $U_PRODUCTOS/api/v1/productos?categoria=Herramientas  (público, sin token)"
C "$U_PRODUCTOS/api/v1/productos?categoria=Herramientas" \
  | jq '.[] | {id, nombre, proveedorId, precio, precioVigente, stock}'
echo
peticion "GET $U_FEEDBACK/api/v1/resenas/producto/1/promedio  (reputación pública)"
C $U_FEEDBACK/api/v1/resenas/producto/1/promedio | jq .
echo
echo "  ★ DESTACAR: dos vendedores distintos; el taladro de Cóndor cuesta menos"
echo "    por la oferta del 20% (precioVigente) y tiene reputación. María lo elige."
pausa

# ============================================================================
titulo "ACTO 3 — Paga (validaciones cruzadas + ORQUESTACIÓN de 4 servicios)"
peticion "POST $U_VENTAS/api/v1/ventas  {clienteId: $MARIA_ID, detalles: [{productoId: 1, cantidad: 1}]}"
VENTA=$(C -X POST $U_VENTAS/api/v1/ventas -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d "{\"clienteId\":$MARIA_ID,\"detalles\":[{\"productoId\":1,\"cantidad\":1}]}")
echo "$VENTA" | jq '{id, estado, montoTotal, comisionTotal, detalles}'
VENTA_ID=$(echo "$VENTA" | jq -r '.id // empty')
[ -n "$VENTA_ID" ] || fallo "no se pudo crear la venta"
echo
echo "  ★ DESTACAR: validó cliente (→clientes) y producto/stock (→productos);"
echo "    el precio es el VIGENTE y la comisión del 10% se calculó en el servidor."
pausa

peticion "PATCH $U_VENTAS/api/v1/ventas/$VENTA_ID/pagar  {medioPago: TARJETA, direccionEntrega: ...}"
C -X PATCH $U_VENTAS/api/v1/ventas/$VENTA_ID/pagar -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d '{"medioPago":"TARJETA","direccionEntrega":"Av. Providencia 123, Santiago"}' \
  | jq '{id, estado, montoTotal}'
echo
echo "  ★ DESTACAR: un solo PATCH orquestó → productos (stock), pagos (pago +"
echo "    comprobante), despacho (seguimiento) y notificaciones (aviso)."
echo
peticion "GET $U_PRODUCTOS/api/v1/productos/1  (evidencia: stock descontado)"
C $U_PRODUCTOS/api/v1/productos/1 | jq '{id, stock}'
PAGO_ID=$(C "$U_PAGOS/api/v1/pagos?ventaId=$VENTA_ID" -H "Authorization: Bearer $TOKEN_MARIA" | jq -r '.[0].id')
peticion "GET $U_PAGOS/api/v1/pagos/$PAGO_ID/comprobante  (evidencia: folio emitido)"
C $U_PAGOS/api/v1/pagos/$PAGO_ID/comprobante -H "Authorization: Bearer $TOKEN_MARIA" | jq .
pausa

# ============================================================================
titulo "ACTO 4 — Sigue el estado del despacho desde su perfil"
peticion "GET $U_DESPACHO/api/v1/envios?clienteId=$MARIA_ID  (el perfil de María)"
ENVIO_ID=$(C "$U_DESPACHO/api/v1/envios?clienteId=$MARIA_ID" -H "Authorization: Bearer $TOKEN_MARIA" \
  | jq -r "[.[] | select(.ventaId == $VENTA_ID)][0].id")
C $U_DESPACHO/api/v1/envios/$ENVIO_ID -H "Authorization: Bearer $TOKEN_MARIA" \
  | jq '{id, ventaId, estadoActual, numeroSeguimiento, direccionEntrega}'
echo
echo "  Ahora el VENDEDOR (token PROVEEDOR de Cóndor) prepara y despacha:"
peticion "PATCH $U_DESPACHO/api/v1/envios/$ENVIO_ID/estado  {estado: ENVIADO}"
C -X PATCH $U_DESPACHO/api/v1/envios/$ENVIO_ID/estado -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' -d '{"estado":"ENVIADO","comentario":"Retirado por courier"}' \
  | jq '{estadoActual}'
peticion "PATCH $U_DESPACHO/api/v1/envios/$ENVIO_ID/estado  {estado: ENTREGADO}"
C -X PATCH $U_DESPACHO/api/v1/envios/$ENVIO_ID/estado -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' -d '{"estado":"ENTREGADO","comentario":"Recibido conforme"}' \
  | jq '{estadoActual}'
echo
peticion "GET $U_DESPACHO/api/v1/envios/$ENVIO_ID/historial  (trazabilidad completa)"
C $U_DESPACHO/api/v1/envios/$ENVIO_ID/historial -H "Authorization: Bearer $TOKEN_MARIA" \
  | jq '.[] | {estado, comentario, fechaCambio}'
echo
peticion "GET $U_NOTIFICACIONES/api/v1/notificaciones?destinatarioTipo=CLIENTE&destinatarioId=$MARIA_ID"
C "$U_NOTIFICACIONES/api/v1/notificaciones?destinatarioTipo=CLIENTE&destinatarioId=$MARIA_ID" \
  -H "Authorization: Bearer $TOKEN_MARIA" | jq '.[] | {tipo, asunto}'
echo
echo "  ★ DESTACAR: historial PENDIENTE→ENVIADO→ENTREGADO con fechas, y el aviso"
echo "    de DESPACHO que disparó el propio servicio despacho vía WebClient."
pausa

# ============================================================================
titulo "ACTO 5 — El producto llega defectuoso: María abre un reclamo"
peticion "POST $U_TICKETS/api/v1/tickets  {ventaId: $VENTA_ID, categoria: PRODUCTO_DEFECTUOSO, ...}"
TICKET=$(C -X POST $U_TICKETS/api/v1/tickets -H "Authorization: Bearer $TOKEN_MARIA" \
  -H 'Content-Type: application/json' \
  -d "{\"ventaId\":$VENTA_ID,\"categoria\":\"PRODUCTO_DEFECTUOSO\",\"asunto\":\"Taladro no enciende\",\"descripcion\":\"El gatillo llegó trabado, no gira\"}")
echo "$TICKET" | jq '{id, estado, ventaId, clienteId, proveedorId, asunto}'
TICKET_ID=$(echo "$TICKET" | jq -r .id)
echo
echo "  ★ DESTACAR: tickets validó contra ventas que la compra es REAL y tomó"
echo "    de ella el cliente y el proveedor (María no los envió)."
pausa

# ============================================================================
titulo "ACTO 6 — El Administrador media la disputa y autoriza el reembolso"
peticion "POST $U_TICKETS/api/v1/tickets/$TICKET_ID/mensajes  (admin media)"
C -X POST $U_TICKETS/api/v1/tickets/$TICKET_ID/mensajes -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"autorRol":"ADMINISTRADOR","mensaje":"Estamos revisando el caso con el vendedor"}' | jq '{autorRol, mensaje}'
peticion "POST $U_TICKETS/api/v1/tickets/$TICKET_ID/mensajes  (vendedor responde)"
C -X POST $U_TICKETS/api/v1/tickets/$TICKET_ID/mensajes -H "Authorization: Bearer $TOKEN_CONDOR" \
  -H 'Content-Type: application/json' \
  -d '{"autorRol":"PROVEEDOR","mensaje":"Aceptamos la devolución, lote con falla"}' | jq '{autorRol, mensaje}'
echo "  (la intervención del admin dejó el ticket EN_MEDIACION)"
echo
peticion "PATCH $U_TICKETS/api/v1/tickets/$TICKET_ID/resolver  {RESUELTO, reembolsoAutorizado: true}"
C -X PATCH $U_TICKETS/api/v1/tickets/$TICKET_ID/resolver -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"estado":"RESUELTO","resolucion":"Falla de fábrica confirmada por el vendedor; se reembolsa el total","reembolsoAutorizado":true}' \
  | jq '{estado, reembolsoAutorizado, resolucion}'
echo
peticion "GET $U_PAGOS/api/v1/pagos/$PAGO_ID/reembolsos  (evidencia del dinero devuelto)"
C $U_PAGOS/api/v1/pagos/$PAGO_ID/reembolsos -H "Authorization: Bearer $TOKEN_ADMIN" \
  | jq '.[0] | {monto, estado, ticketId, motivo}'
peticion "GET $U_PAGOS/api/v1/pagos/$PAGO_ID  (el pago quedó REEMBOLSADO)"
C $U_PAGOS/api/v1/pagos/$PAGO_ID -H "Authorization: Bearer $TOKEN_ADMIN" | jq '{id, monto, estado}'
peticion "GET notificaciones de María (aviso de la resolución)"
C "$U_NOTIFICACIONES/api/v1/notificaciones?destinatarioTipo=CLIENTE&destinatarioId=$MARIA_ID&tipo=RESOLUCION_RECLAMO" \
  -H "Authorization: Bearer $TOKEN_MARIA" | jq '.[0] | {asunto, mensaje}'
echo
echo "  ★ DESTACAR: tickets invocó a pagos (reembolso PROCESADO con referencia al"
echo "    ticket) y a notificaciones. La historia completa quedó cerrada."

# ============================================================================
echo $((N+1)) > .demo-cliente-n
titulo "FIN — Historia de María completa ($MODO)"
echo "  Cliente migrado: $MARIA_EMAIL (id $MARIA_ID) · venta $VENTA_ID · pago $PAGO_ID · envío $ENVIO_ID · ticket $TICKET_ID"
echo "  Próxima corrida usará cliente$((N+1))@paris.cl automáticamente."
