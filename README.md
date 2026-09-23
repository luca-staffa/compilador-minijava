# Etapa 2 - Analizador Sintáctico de MiniJava

Compilador de MiniJava con Analizador Léxico (Etapa 1) y Analizador Sintáctico
Descendente Recursivo (Etapa 2), implementado según la estrategia recursiva
simple sobre la gramática MiniJava transformada a LL(1).

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
  src/main/java/analizadorsintactico/*.java \
  src/main/java/sourcemanager/*.java
jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .
```

## Ejecución

```
java -jar Compilador.jar resources/sinErrores/sintCorrecto01.java
```

Acepta como parámetro la ruta del archivo fuente MiniJava, con cualquier extensión.

Si el análisis finaliza sin errores se muestra:

```
Compilacion Exitosa
[SinErrores]
```

Ante un error sintáctico o léxico se muestra un mensaje descriptivo y el código
de error con el lexema y la línea del token con el que se detectó:

```
Error Sintactico en linea 1: se esperaba un id de clase se encontro "{"
[Error:{|1]
```

(El lexema del token EOF es `$`.)

## Estructura

- `moduloprincipal.ModuloPrincipal`: interfaz con el usuario. Abre el fuente con
  el `SourceManager`, crea el `AnalizadorLexico` y el `AnalizadorSintactico`,
  dispara el análisis y reporta el resultado (éxito o código de error).
- `analizadorsintactico.AnalizadorSintactico`: parser descendente recursivo.
  Un método por cada no terminal de la gramática LL(1); la producción a aplicar
  se elige mirando si el token actual está en los primeros de cada parte
  derecha (esquema simple). Los terminales se consumen con `match`, que a su
  vez pide el próximo token al léxico.
- `analizadorsintactico.ExcepcionSintactica`: error sintáctico con lexema,
  número de línea y mensaje. Se propaga hasta el módulo principal.
- `analizadorlexico.*` y `sourcemanager.*`: módulos de la Etapa 1, sin cambios.

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

- Los casos de `resources/sinErrores/` deben ser aceptados: el tester verifica que
  la salida contenga `[SinErrores]`.
- Los casos de `resources/conErrores/` deben ser rechazados: la primera línea del
  archivo lleva el código esperado con el formato `///[Error:lexema|nroLinea]` y el
  tester verifica que aparezca en la salida.

Para agregar un caso basta con crear el archivo en la carpeta correspondiente;
los testers lo incorporan automáticamente.
