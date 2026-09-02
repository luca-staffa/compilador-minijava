#!/bin/sh
# Compila y ejecuta los testers JUnit (equivalente Windows: CorrerTests.bat).
# Requiere que el working directory sea codigo/ (el script lo hace solo).
set -e
cd "$(dirname "$0")"

LIB=lib
JUNIT_JAR=$LIB/junit-4.13.2.jar
HAMCREST_JAR=$LIB/hamcrest-core-1.3.jar

if [ ! -f "$JUNIT_JAR" ] || [ ! -f "$HAMCREST_JAR" ]; then
    echo "Descargando dependencias de test en $LIB/ ..."
    mkdir -p "$LIB"
    curl -fLo "$JUNIT_JAR" https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar \
        || { echo "No se pudo descargar JUnit. Descargalo manualmente en $JUNIT_JAR."; exit 1; }
    curl -fLo "$HAMCREST_JAR" https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar \
        || { echo "No se pudo descargar Hamcrest. Descargalo manualmente en $HAMCREST_JAR."; exit 1; }
fi

./Compilar.sh

rm -rf build/test-classes
mkdir -p build/test-classes

javac -cp "build/classes:$JUNIT_JAR:$HAMCREST_JAR" -d build/test-classes src/test/java/test/*.java

java -cp "build/classes:build/test-classes:$JUNIT_JAR:$HAMCREST_JAR" org.junit.runner.JUnitCore \
    test.TesterDeCasosSinErrores test.TesterDeCasosConErrores
