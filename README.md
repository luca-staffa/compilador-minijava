# Etapa 1 - Analizador Léxico de MiniJava

## Requisitos

No se usa ninguna herramienta de build: solo se necesitan `javac`, `jar` y `java` de
cualquier JDK 21 o superior (probado con JDK 25).

## Compilación

Desde `codigo/`:

```
./Compilar.sh             # compila y genera Compilador.jar  (Windows: Compilar.bat)
```

Comando manual equivalente:

```
javac -d build/classes \
  src/main/java/moduloprincipal/*.java \
  src/main/java/analizadorlexico/*.java \
  src/main/java/sourcemanager/*.java
jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .
```

## Ejecución

```
java -jar Compilador.jar resources/sinErrores/lexSinErrores01.java
```

Acepta como parámetro la ruta del archivo fuente MiniJava, con cualquier extensión.

## Tests

Desde `codigo/`:

```
./CorrerTests.sh          # compila y ejecuta los testers JUnit  (Windows: CorrerTests.bat)
```

`CorrerTests.sh` descarga automáticamente `junit-4.13.2.jar` y `hamcrest-core-1.3.jar`
en `lib/` si no están (requiere `curl`); si no hay red, se pueden descargar a mano de
Maven Central y dejarlos en esa carpeta.

Comandos manuales equivalentes (desde `codigo/`, con JUnit 4.13.2 y Hamcrest 1.3 en `lib/`):

```
javac -cp "build/classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  -d build/test-classes src/test/java/test/*.java

java -cp "build/classes:build/test-classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore test.TesterDeCasosSinErrores test.TesterDeCasosConErrores
```

En Windows el separador del classpath es `;` en lugar de `:`.

Los testers (`TesterDeCasosSinErrores` y `TesterDeCasosConErrores`) usan como working
directory `codigo/` para acceder a `resources/sinErrores/` y `resources/conErrores/`
(los scripts ya se posicionan ahí; los comandos manuales deben correrse desde `codigo/`).
