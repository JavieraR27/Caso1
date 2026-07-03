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
| administrador | 8091 | administradordb | pendiente (F5) |
| gateway | 8080 | — | backlog (fuera de EP2) |

## Stack

Java 21 · Spring Boot 3.3.6 · Spring Data JPA/Hibernate (`ddl-auto=update`) · PostgreSQL en Neon
(un proyecto, una BD lógica por servicio) · WebClient para comunicación entre servicios · Maven.

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
