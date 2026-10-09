# Etapa 3 - Analizador Semántico (Chequeo de Declaraciones) de MiniJava

Compilador de MiniJava con Analizador Léxico (Etapa 1), Analizador Sintáctico
Descendente Recursivo (Etapa 2) y Análisis Semántico de Declaraciones (Etapa 3),
implementado sin herramientas de build.

La Etapa 3 extiende al analizador sintáctico para que **construya la Tabla de
Símbolos** a medida que reconoce las declaraciones, y agrega una segunda pasada
de **chequeo de declaraciones** y la **consolidación** de clases e interfaces.

## Requisitos

Solo se necesitan `javac`, `jar` y `java` de cualquier JDK 21 o superior
(probado con JDK 25).

## Compilación

Desde `compilador-minijava/`:

```
./Compilar.sh             # compila y genera Compilador.jar  (Windows: Compilar.bat)
```

Comando manual equivalente:

```
javac -d build/classes \
  src/main/java/moduloprincipal/*.java \
  src/main/java/analizadorlexico/*.java \
  src/main/java/analizadorsintactico/*.java \
  src/main/java/analizadorsemantico/*.java \
  src/main/java/sourcemanager/*.java
jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .
```

## Ejecución

```
java -jar Compilador.jar resources/sinErrores/semCorrecto01.java
```

Acepta como parámetro la ruta del archivo fuente MiniJava, con cualquier extensión.

Si el análisis (léxico, sintáctico y semántico) finaliza sin errores se muestra:

```
Compilacion Exitosa
[SinErrores]
```

Ante un error léxico, sintáctico o semántico se muestra un mensaje descriptivo y
el código de error con el lexema y la línea del token asociado:

```
Error Semantico en linea 7: el tipo A1 ya fue declarado
[Error:A1|7]
```

La comparación de tokens en los casos con error es sensible al lexema y al
número de línea (formato `///[Error:lexema|nroLinea]`, igual que en la Etapa 2).

## Estructura

- `moduloprincipal.ModuloPrincipal`: interfaz con el usuario. Crea la
  `TablaDeSimbolos` (con las entidades predefinidas), el `AnalizadorLexico` y el
  `AnalizadorSintactico`; dispara el análisis, ejecuta el chequeo de
  declaraciones y la consolidación, y reporta el resultado.
- `analizadorsintactico.AnalizadorSintactico`: parser descendente recursivo que,
  además de validar la sintaxis, ejecuta las acciones semánticas que construyen
  la tabla de símbolos (controles de nombres repetidos de la primera pasada).
- `analizadorsemantico`: modelo de la Tabla de Símbolos.
  - `TablaDeSimbolos`: tabla global de tipos, entidades predefinidas, contexto
    actual y punto de entrada de `estaBienDeclarada()` y `consolidar()`.
  - `Entidad` (abstracta): `Clase`, `Interfaz`, `Metodo`, `Constructor`,
    `Atributo`, `Parametro`; todas conservan el token de su declaración.
  - `Tipo` (abstracta): `TipoPrimitivo`, `TipoVoid`, `TipoParametro`,
    `TipoArreglo`, `TipoReferencia`.
  - `ExcepcionSemantica`: error semántico con lexema, línea y mensaje.
- `analizadorlexico.*` y `sourcemanager.*`: módulos de las etapas anteriores.

## Chequeo de declaraciones

**Primera pasada (durante el parseo).** Se controlan nombres repetidos:
tipos (clases/interfaces), atributos de una clase, métodos con la misma clave
`(nombre, aridad)` en una clase o interfaz, constructores con la misma aridad y
parámetros repetidos en una misma unidad. Además, al parsear una instanciación
`new`, se admite la notación diamante (`new Caja<>()`) solo si la clase instanciada
declara un parámetro de tipo.

**Segunda pasada (`estaBienDeclarada`).** Se controlan tipos válidos y
parámetros genéricos (incluidos los tipos genéricos anidados, por ejemplo
`Caja<Par<String>>`), relaciones de herencia (existe y es del tipo correcto),
circularidad de clases y de interfaces, redefiniciones de métodos (incluidos los
conflictos con métodos estáticos heredados) y el contrato de interfaces.

**Consolidación (`consolidar`).** Incorpora los miembros heredados de clases e
interfaces con la sustitución del parámetro genérico correspondiente
(`extends A<X>` instancia, `extends A` conserva el nombre) y controla el choque
de un atributo propio con un atributo heredado. Toda clase extiende `Object`
(explícita o implícitamente, también cuando solo implementa una interfaz), por lo
que la consolidación incorpora los miembros heredados de `Object`.

## Tests

Desde `compilador-minijava/`:

```
./CorrerTests.sh          # compila y ejecuta los testers JUnit  (Windows: CorrerTests.bat)
```

`CorrerTests.sh` descarga automáticamente `junit-4.13.2.jar` y
`hamcrest-core-1.3.jar` en `lib/` si no están (requiere `curl`); si no hay red,
se pueden descargar a mano de Maven Central y dejarlos en esa carpeta.

Comandos manuales equivalentes (desde `compilador-minijava/`):

```
javac -cp "build/classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  -d build/test-classes src/test/java/test/*.java

java -cp "build/classes:build/test-classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore test.TesterDeCasosSinErrores test.TesterDeCasosConErrores
```

Los testers recorren `resources/sinErrores/` (deben aceptarse) y
`resources/conErrores/` (deben rechazarse; la primera línea del archivo lleva el
código esperado con el formato `///[Error:lexema|nroLinea]`).

Para agregar un caso basta con crear el archivo en la carpeta correspondiente;
los testers lo incorporan automáticamente.
