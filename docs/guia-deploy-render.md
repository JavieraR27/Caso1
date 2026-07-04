# Guía de despliegue en Render (EP3)

Despliega los **11 microservicios de negocio + el gateway** como web services Docker en el
plan free de Render, usando el blueprint [render.yaml](../render.yaml) de la raíz del repo.
Eureka queda como infra local: en la nube cada servicio tiene su propia URL, el discovery se
desactiva con `EUREKA_CLIENT_ENABLED=false` y el gateway enruta con las `*_SERVICE_URL`.
**Swagger unificado en la nube:** https://paris-gateway.onrender.com/swagger-ui.html

## 1. Subir el repo a GitHub (manual)

```bash
cd Caso1
git remote add origin https://github.com/<tu-usuario>/<repo>.git
git push -u origin main
```

Verifica en GitHub que **NO** se subieron `application-secrets.properties` ni `.env`
(están gitignored). El `render.yaml` incluye el host de Neon (no es secreto); el usuario,
la password y el secreto JWT se piden aparte.

## 2. Crear el blueprint en Render

1. [render.com](https://render.com) → **New → Blueprint** → conectar la cuenta de GitHub y
   elegir el repo.
2. Render lee `render.yaml` y lista los 12 servicios `paris-*` (11 de negocio + gateway).
3. Al confirmar, pide los 3 valores secretos del grupo `paris-comun`:
   - `SPRING_DATASOURCE_USERNAME` → `neondb_owner`
   - `SPRING_DATASOURCE_PASSWORD` → la password de Neon
   - `PARIS_JWT_SECRET` → el mismo secreto usado localmente (≥32 caracteres)
4. Crear. `autoDeploy` está **apagado** para no quemar minutos de build del plan free:
   el primer deploy parte solo; los siguientes se lanzan a mano con **Manual Deploy** en
   cada servicio.

> Si algún nombre `paris-*` ya está tomado, Render le agrega un sufijo a la URL
> (`paris-ventas-abcd.onrender.com`). En ese caso corrige las variables `*_SERVICE_URL`
> de los servicios que lo consumen (dashboard → Environment) — el mapa de quién llama a
> quién está en PLAN.md §2.

## 3. Particularidades del plan free (importante para la defensa)

- **Spin-down:** cada servicio se duerme tras ~15 min sin tráfico y despierta en ~1 min.
  Por eso el blueprint sube los timeouts entre servicios
  (`PARIS_WEBCLIENT_RESPONSETIMEOUT=75` s) — la orquestación tolera que un servicio
  destino esté despertando.
- **Pre-calentar SIEMPRE antes de la demo** (despierta los 12 y los deja listos):

```bash
for s in legacy clientes proveedores productos ventas pagos despacho tickets feedback notificaciones administrador; do
  curl -s -o /dev/null -w "paris-$s: %{http_code}\n" --max-time 90 https://paris-$s.onrender.com/v3/api-docs &
done
curl -s -o /dev/null -w "paris-gateway: %{http_code}\n" --max-time 90 https://paris-gateway.onrender.com/v3/api-docs/swagger-config &
wait
```

  (200 = despierto; repetir hasta que todos den 200.)
- **512 MB por instancia:** ya acotado con `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`.
- La BD es la misma de siempre (Neon): los datos locales y los de la nube son los mismos.

## 4. Smoke test en la nube

```bash
# Catálogo público
curl -s https://paris-productos.onrender.com/api/v1/productos | jq .

# Login del admin y una llamada protegida
TOKEN_ADMIN=$(curl -s -X POST https://paris-administrador.onrender.com/api/v1/administradores/login \
  -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | jq -r .token)
curl -s https://paris-administrador.onrender.com/api/v1/admin/acciones \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq length

# Validación cruzada entre servicios EN LA NUBE (clientes → legacy)
curl -s -X POST https://paris-clientes.onrender.com/api/v1/clientes/login \
  -H 'Content-Type: application/json' -d '{"email":"cliente10@paris.cl","password":"pass10"}' | jq .cliente
```

- **Swagger unificado (gateway):** `https://paris-gateway.onrender.com/swagger-ui.html` —
  desplegable con los 11 servicios; los "Try it out" salen por el gateway.
- Swagger de cada servicio: `https://paris-<servicio>.onrender.com/swagger-ui/index.html`
  (con las anotaciones @Operation/@ApiResponse/@RequestBody/@ExampleObject de la rúbrica).
- Endpoints protegidos desde Swagger: botón **Authorize** → pegar el `token` del login
  correspondiente, sin el prefijo `Bearer ` (la tabla de qué token usar por acto está en la
  [guía de María](guia-demo-maria.md) §2.1).

## 5. Demo en la nube (historia de María)

La [guía de María](guia-demo-maria.md) funciona igual en la nube reemplazando
`localhost:<puerto>` por `https://paris-<servicio>.onrender.com`. Checklist previa:

1. Pre-calentar los 11 (paso 3) y esperar que todos den 200.
2. Datos base ya en Neon (admin, vendedores, taladros); si se quiere desde cero, TRUNCATE
   según la guía §6 y recrear el escenario.
3. Tener el token de cada rol a mano (login de admin, Cóndor y María).
