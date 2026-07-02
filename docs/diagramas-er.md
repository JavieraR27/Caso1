# Diagramas ER por servicio — Caso 1 · Marketplace Almacenes Paris

Un diagrama por microservicio (una base de datos Neon por servicio, sin tablas compartidas).
Convenciones: tablas en `snake_case` plural, PK `Integer` IDENTITY, montos CLP en `int`.
Las **referencias blandas** (ids de entidades que viven en otro servicio) se marcan en el comentario
del atributo: son columnas simples sin FK física; su existencia se valida por WebClient.

---

## 1. legacy — `legacydb`

Simula el sistema antiguo. Tabla única con seed de 5.000 clientes históricos.

```mermaid
erDiagram
    CLIENTES_LEGACY {
        int id PK
        string rut UK
        string email UK
        string password "texto plano (simulacion legacy)"
        string nombre
        date fecha_registro
    }
```

## 2. clientes — `clientesdb`

```mermaid
erDiagram
    CLIENTES ||--o{ DIRECCIONES : tiene
    CLIENTES {
        int id PK
        string email UK
        string password
        string nombre
        string telefono
        string tipo "NUEVO | MIGRADO"
        int legacy_id "ref blanda -> legacy (nullable)"
        timestamp fecha_creacion
    }
    DIRECCIONES {
        int id PK
        int cliente_id FK
        string alias
        string calle
        string numero
        string comuna
        string region
    }
```

## 3. proveedores — `proveedoresdb`

```mermaid
erDiagram
    PROVEEDORES ||--o{ DOCUMENTOS_PROVEEDOR : adjunta
    PROVEEDORES {
        int id PK
        string rut UK
        string razon_social
        string email
        string telefono
        string estado "POSTULADO | APROBADO | RECHAZADO"
        string observaciones "exigidas al rechazar"
        timestamp fecha_postulacion
        timestamp fecha_resolucion
    }
    DOCUMENTOS_PROVEEDOR {
        int id PK
        int proveedor_id FK
        string tipo
        string nombre_archivo
        string url
        timestamp fecha_carga
    }
```

## 4. productos — `productosdb`

```mermaid
erDiagram
    CATEGORIAS ||--o{ PRODUCTOS : clasifica
    PRODUCTOS ||--o{ OFERTAS : tiene
    CATEGORIAS {
        int id PK
        string nombre UK
    }
    PRODUCTOS {
        int id PK
        int proveedor_id "ref blanda -> proveedores"
        int categoria_id FK
        string nombre
        string descripcion
        int precio "CLP"
        int stock "nunca negativo"
        string estado "ACTIVO | INACTIVO"
        timestamp fecha_creacion
    }
    OFERTAS {
        int id PK
        int producto_id FK
        string tipo_oferta "PORCENTAJE | MONTO_FIJO"
        int valor
        date fecha_inicio
        date fecha_fin
        boolean activa "una sola activa por producto"
    }
```

## 5. ventas — `ventasdb`

Los snapshots (nombre, categoría, precio) congelan el dato histórico de la transacción
y son el insumo del reporte semanal por categoría.

```mermaid
erDiagram
    VENTAS ||--|{ DETALLES_VENTA : contiene
    VENTAS {
        int id PK
        int cliente_id "ref blanda -> clientes"
        timestamp fecha
        string estado "CREADA | PAGADA | ANULADA"
        int monto_total "CLP, calculado en servidor"
        int comision_total "CLP, 10% configurable"
    }
    DETALLES_VENTA {
        int id PK
        int venta_id FK
        int producto_id "ref blanda -> productos"
        int proveedor_id "ref blanda -> proveedores"
        string nombre_producto "snapshot"
        string categoria "snapshot p/reporte"
        int cantidad
        int precio_unitario "snapshot, precio vigente"
        int subtotal
        int comision "por linea"
    }
```

## 6. pagos — `pagosdb`

```mermaid
erDiagram
    PAGOS ||--|| COMPROBANTES : emite
    PAGOS ||--o{ REEMBOLSOS : registra
    PAGOS {
        int id PK
        int venta_id UK "ref blanda -> ventas (un pago por venta)"
        int cliente_id "ref blanda -> clientes"
        int monto "debe coincidir con la venta"
        string medio_pago "TARJETA | DEBITO | TRANSFERENCIA"
        string estado "PAGADO | REEMBOLSADO | ANULADO"
        timestamp fecha_pago
    }
    COMPROBANTES {
        int id PK
        int pago_id FK
        string folio UK
        timestamp fecha_emision
    }
    REEMBOLSOS {
        int id PK
        int pago_id FK
        int ticket_id "ref blanda -> tickets"
        int monto "menor o igual al pago"
        string motivo
        string estado "SOLICITADO | PROCESADO"
        timestamp fecha
    }
```

## 7. despacho — `despachodb`

```mermaid
erDiagram
    SEGUIMIENTOS ||--|{ HISTORIAL_ESTADOS : registra
    SEGUIMIENTOS {
        int id PK
        int venta_id UK "ref blanda -> ventas (uno por venta)"
        int cliente_id "ref blanda -> clientes"
        int proveedor_id "ref blanda -> proveedores"
        string estado_actual "PENDIENTE|PREPARACION|ENVIADO|EN_REPARTO|ENTREGADO|CANCELADO"
        string numero_seguimiento UK
        string direccion_entrega "snapshot"
        timestamp fecha_creacion
        timestamp fecha_actualizacion
    }
    HISTORIAL_ESTADOS {
        int id PK
        int seguimiento_id FK
        string estado
        string comentario
        timestamp fecha_cambio
    }
```

## 8. tickets — `ticketsdb`

```mermaid
erDiagram
    TICKETS ||--o{ TICKET_MENSAJES : conversa
    TICKETS {
        int id PK
        int venta_id "ref blanda -> ventas"
        int cliente_id "ref blanda -> clientes (tomado de la venta)"
        int proveedor_id "ref blanda -> proveedores (tomado de la venta)"
        string categoria "PRODUCTO_DEFECTUOSO | NO_RECIBIDO | OTRO"
        string asunto
        string descripcion
        string estado "ABIERTO | EN_MEDIACION | RESUELTO | RECHAZADO"
        string resolucion
        boolean reembolso_autorizado
        timestamp fecha_apertura
        timestamp fecha_resolucion
    }
    TICKET_MENSAJES {
        int id PK
        int ticket_id FK
        string autor_rol "CLIENTE | PROVEEDOR | ADMINISTRADOR"
        string mensaje
        timestamp fecha
    }
```

## 9. feedback — `feedbackdb`

```mermaid
erDiagram
    RESENAS {
        int id PK
        int producto_id "ref blanda -> productos"
        int cliente_id "ref blanda -> clientes"
        int venta_id "ref blanda -> ventas (compra verificada)"
        int calificacion "1 a 5"
        string comentario
        timestamp fecha_creacion
    }
```

Restricción: `UNIQUE (cliente_id, producto_id, venta_id)` — una reseña por compra.

## 10. notificaciones — `notificacionesdb`

```mermaid
erDiagram
    NOTIFICACIONES {
        int id PK
        string destinatario_tipo "CLIENTE | PROVEEDOR"
        int destinatario_id "ref blanda -> clientes o proveedores"
        string tipo "DESPACHO | APROBACION_PROVEEDOR | RESOLUCION_RECLAMO | VENTA_CONFIRMADA"
        string asunto
        string mensaje
        string estado "PENDIENTE | ENVIADA"
        int referencia_id "ref blanda: venta, ticket o proveedor segun tipo"
        timestamp fecha_creacion
        timestamp fecha_envio
    }
```

## 11. administrador — `administradordb`

```mermaid
erDiagram
    ADMINISTRADORES ||--o{ ACCIONES_ADMIN : audita
    ADMINISTRADORES ||--o{ REPORTES_SEMANALES : genera
    REPORTES_SEMANALES ||--|{ REPORTE_CATEGORIAS : desglosa
    ADMINISTRADORES {
        int id PK
        string username UK
        string password
        string nombre
        string email
    }
    ACCIONES_ADMIN {
        int id PK
        int administrador_id FK
        string tipo "APROBAR_PROVEEDOR | RECHAZAR_PROVEEDOR | RESOLVER_TICKET | GENERAR_REPORTE"
        int referencia_id "ref blanda: proveedor, ticket o reporte"
        string observaciones
        timestamp fecha
    }
    REPORTES_SEMANALES {
        int id PK
        int administrador_id FK
        date semana_inicio
        date semana_fin
        int total_ventas "CLP"
        int total_comision "CLP"
        timestamp fecha_generacion
    }
    REPORTE_CATEGORIAS {
        int id PK
        int reporte_id FK
        string categoria
        int unidades
        int monto_total "CLP"
        int comision_total "CLP"
    }
```
