-- Seed de los 5.000 clientes históricos del sistema legacy.
-- Ejecutar UNA VEZ en la BD legacydb (SQL Editor de Neon), después de que
-- Hibernate haya creado la tabla (primer arranque del servicio).
-- Idempotente: los conflictos por email/rut únicos se ignoran.

INSERT INTO clientes_legacy (rut, email, password, nombre, fecha_registro)
SELECT
    (10000000 + g)::text || '-' || (g % 10)::text,
    'cliente' || g || '@paris.cl',
    'pass' || g,
    'Cliente Histórico ' || g,
    DATE '2015-01-01' + (g % 3650)
FROM generate_series(1, 5000) AS g
ON CONFLICT DO NOTHING;

-- Verificación:
-- SELECT count(*) FROM clientes_legacy;  -- debe dar 5000
