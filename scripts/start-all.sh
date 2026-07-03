#!/bin/bash
# Levanta eureka + los 11 microservicios desde sus JARs.
# Requiere haber compilado antes: mvn -DskipTests package en cada módulo
# (los secrets van dentro del JAR: recompilar si cambian).
cd "$(dirname "$0")/.."

SERVICIOS="eureka legacy clientes proveedores productos ventas pagos despacho tickets feedback notificaciones administrador"

for s in $SERVICIOS; do
  jar=$s/target/$s-0.0.1-SNAPSHOT.jar
  if [ ! -f "$jar" ]; then
    echo "✘ Falta $jar — compílalo: (cd $s && mvn -DskipTests package)"
    exit 1
  fi
  (cd $s && nohup java -jar target/$s-0.0.1-SNAPSHOT.jar > /tmp/paris-$s.log 2>&1 &)
  echo "→ $s (log: /tmp/paris-$s.log)"
done

echo "Esperando a que respondan los 12 puertos (~40 s, +1 s de cold start de Neon)..."
for i in $(seq 1 40); do
  up=0
  for p in 8761 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091; do
    curl -s -o /dev/null --max-time 1 http://localhost:$p && up=$((up+1))
  done
  if [ $up -eq 12 ]; then echo "✔ Los 12 procesos responden."; exit 0; fi
  echo "  $up/12..."
  sleep 5
done
echo "✘ Tiempo agotado; revisa /tmp/paris-*.log"
exit 1
