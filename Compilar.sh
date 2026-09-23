#!/bin/sh
# Compila el proyecto y genera Compilador.jar (equivalente Windows: Compilar.bat).
set -e
cd "$(dirname "$0")"

rm -rf build
mkdir -p build/classes

javac -d build/classes src/main/java/moduloprincipal/*.java src/main/java/analizadorlexico/*.java src/main/java/analizadorsintactico/*.java src/main/java/sourcemanager/*.java

jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .

echo "Compilador.jar generado."
