#!/bin/bash
# Detiene todos los microservicios levantados con start-all.sh
if pkill -f 'SNAPSHOT.jar'; then
  echo "✔ Servicios detenidos."
else
  echo "No había servicios corriendo."
fi
