@echo off
rem Compila el proyecto y genera Compilador.jar (equivalente Unix: Compilar.sh).
setlocal
cd /d "%~dp0"

if exist build rmdir /s /q build
mkdir build\classes

javac -d build\classes src\main\java\moduloprincipal\*.java src\main\java\analizadorlexico\*.java src\main\java\analizadorsintactico\*.java src\main\java\sourcemanager\*.java || goto :error

jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build\classes .

echo Compilador.jar generado.
exit /b 0

:error
echo Error de compilacion.
exit /b 1
