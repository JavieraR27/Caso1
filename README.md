# Caso 1 — Almacenes Paris: Marketplace de Mejoramiento del Hogar

Proyecto semestral DSY1103 Full Stack I (EP2). Arquitectura de microservicios Spring Boot:
**11 servicios de negocio** + gateway (infra, backlog), monorepo con una carpeta por servicio.

- Plan completo: [PLAN.md](PLAN.md)
- Diagramas ER por servicio: [docs/diagramas-er.md](docs/diagramas-er.md)

## Servicios

| Servicio | Puerto | BD (Neon) | Estado |
|---|---|---|---|
| [legacy](legacy/) | 8081 | legacydb | ✅ construido |
| [clientes](clientes/) | 8082 | clientesdb | ✅ construido |
| [proveedores](proveedores/) | 8083 | proveedoresdb | ✅ construido |
| [productos](productos/) | 8084 | productosdb | ✅ construido |
| [ventas](ventas/) | 8085 | ventasdb | ✅ construido |
| [pagos](pagos/) | 8086 | pagosdb | ✅ construido |
| [despacho](despacho/) | 8087 | despachodb | ✅ construido |
| [tickets](tickets/) | 8088 | ticketsdb | ✅ construido |
| [feedback](feedback/) | 8089 | feedbackdb | ✅ construido |
| [notificaciones](notificaciones/) | 8090 | notificacionesdb | ✅ construido |
| [administrador](administrador/) | 8091 | administradordb | ✅ construido |
| [gateway](gateway/) | 8080 | — | ✅ construido (infra, no puntúa EP2) |
| [eureka](eureka/) | 8761 | — | ✅ construido (infra, no puntúa EP2) |

## Stack

Java 21 · Spring Boot 3.3.6 · Spring Data JPA/Hibernate (`ddl-auto=update`) · PostgreSQL en Neon
(un proyecto, una BD lógica por servicio) · WebClient para comunicación entre servicios ·
Spring Cloud 2023.0.5 (Eureka + Gateway) · Spring Security + JJWT (BCrypt/JWT/RBAC) ·
springdoc OpenAPI 3 · JUnit 5/Mockito · Docker Compose · Maven.

## Seguridad (roles)

JWT HS256 con secreto compartido (`paris.jwt.secret`). Cada login emite un token con su rol:

| Rol | Login | Acciones exclusivas |
|---|---|---|
| CLIENTE | `POST /api/v1/clientes/login` | comprar, pagar, reclamar, reseñar |
| PROVEEDOR | `POST /api/v1/proveedores/login` (exige APROBADO) | publicar productos/ofertas, marcar enviado |
| ADMINISTRADOR | `POST /api/v1/administradores/login` | aprobar proveedores, resolver tickets, reportes, moderar |
| INTERNO | (lo emiten los propios servicios) | llamadas WebClient entre microservicios |

Catálogo (`GET /productos`, `/categorias`) y reseñas son de lectura pública; registro y logins
no exigen token. El resto de endpoints requiere `Authorization: Bearer <token>` según la matriz
de PLAN.md §3.

## Documentación y tests

- Swagger UI por servicio: `http://localhost:<puerto>/swagger-ui/index.html` (JSON en `/v3/api-docs`).
- Tests unitarios de reglas de negocio (sin red ni BD): `mvn test -Dtest='*ServiceTest'`.

## Docker Compose

```bash
cp .env.example .env   # completar credenciales de Neon
docker compose up --build
```

Levanta los 13 contenedores (11 servicios + eureka + gateway); dentro de la red se resuelven
por hostname y el gateway queda en `http://localhost:8080`.

## Cómo ejecutar un servicio

```bash
cd <servicio>

# 1. Configurar credenciales (una vez): copiar la plantilla y completar con el
#    connection string POOLED de Neon apuntando a la BD del servicio
cp src/main/resources/application-secrets.properties.example \
   src/main/resources/application-secrets.properties

# 2. Arrancar (si JAVA_HOME está vacío, usar mvn del sistema)
mvn spring-boot:run
# o bien: export JAVA_HOME=/usr/lib/jvm/java-21-openjdk && ./mvnw spring-boot:run
```

Hibernate crea las tablas al primer arranque. Para `legacy`, ejecutar después el seed
[`legacy/src/main/resources/seed-clientes-legacy.sql`](legacy/src/main/resources/seed-clientes-legacy.sql)
en el SQL Editor de Neon (5.000 clientes históricos).

## Orden de arranque para la demo

Los servicios se validan entre sí por WebClient: levantar primero los que no llaman a nadie.

```
legacy → proveedores → notificaciones → clientes → productos → ventas → pagos → despacho → tickets → feedback → administrador
```
