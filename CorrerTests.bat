@echo off
rem Compila y ejecuta los testers JUnit (equivalente Unix: CorrerTests.sh).
setlocal
cd /d "%~dp0"

if not exist lib\junit-4.13.2.jar (
    echo Descargando dependencias de test en lib\ ...
    mkdir lib 2>nul
    curl -fLo lib\junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar || goto :error
)
if not exist lib\hamcrest-core-1.3.jar (
    mkdir lib 2>nul
    curl -fLo lib\hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar || goto :error
)

call Compilar.bat || exit /b 1

if exist build\test-classes rmdir /s /q build\test-classes
mkdir build\test-classes

javac -cp "build\classes;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" -d build\test-classes src\test\java\test\*.java || goto :error

java -cp "build\classes;build\test-classes;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore test.TesterDeCasosSinErrores test.TesterDeCasosConErrores
exit /b 0

:error
echo Error en la ejecucion de los tests.
exit /b 1
