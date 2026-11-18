# Etapas 1 a 5 - Compilador de MiniJava

Compilador de MiniJava con Analizador Léxico (Etapa 1), Analizador Sintáctico
Descendente Recursivo (Etapa 2), Análisis Semántico de Declaraciones (Etapa 3),
Análisis Semántico de Sentencias con ASTs (Etapa 4) y Generación de Código
CeIVM (Etapa 5), implementado sin herramientas de build.

La Etapa 3 extiende al analizador sintáctico para que **construya la Tabla de
Símbolos** a medida que reconoce las declaraciones, y agrega una segunda pasada
de **chequeo de declaraciones** y la **consolidación** de clases e interfaces.

La Etapa 4 extiende al analizador sintáctico para que **construya los AST** de
los cuerpos de métodos y constructores y los asocie a la Tabla de Símbolos, y
agrega el **chequeo de sentencias**: resolución de nombres, chequeo de tipos,
información estructural, ámbitos locales y contexto estático.

La Etapa 5 agrega el **Generador de Código**: calcula los offsets en el RA, el
CIR y la VT y recorre la TS y los AST para emitir **código CeIASM** ejecutable
en la CeIVM, que el módulo principal escribe en el archivo de salida. Incluye el
control del punto de entrada (`static void main()`), el control en ejecución de
índices y tamaños de arreglos, la construcción de Strings en el `.heap` y el
despacho virtual genérico.

Cuando se pide un archivo de salida, el compilador exige exactamente un método
`static void main()` sin parámetros: si falta (o no tiene esa forma) reporta
`[Error:$|línea]` y si hay más de uno, `[Error:main|línea]` sobre el segundo. Si
solo se analiza (sin archivo de salida), `main` no es obligatorio.

## Requisitos

Solo se necesitan `javac`, `jar` y `java` de cualquier JDK 21 o superior
(probado con JDK 25). Para ejecutar los tests, la máquina virtual
`CeIVM-cei2011.jar` debe estar en la raíz del proyecto (ya incluida).

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
  src/main/java/analizadorsemantico/ast/*.java \
  src/main/java/sourcemanager/*.java
jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .
```

## Ejecución

```
java -jar Compilador.jar resources/sinErrores/gen-01.java salida.txt
```

El primer parámetro es la ruta del archivo fuente MiniJava (con cualquier
extensión) y el segundo, opcional, el nombre del archivo de salida para la
CeIVM (también con cualquier extensión). Si se omite el segundo parámetro solo se
realiza el análisis, sin generar código.

Si el análisis (léxico, sintáctico y semántico) finaliza sin errores se muestra:

```
Compilacion Exitosa
[SinErrores]
```

y se escribe el código CeIASM generado en el archivo de salida. Ese archivo puede
ejecutarse con la máquina virtual:

```
java -jar CeIVM-cei2011.jar salida.txt
```

Ante un error léxico, sintáctico o semántico se muestra un mensaje descriptivo y
el código de error con el lexema y la línea del token asociado:

```
Error Semantico en linea 7: el tipo A1 ya fue declarado
[Error:A1|7]
```

La comparación de tokens en los casos con error es sensible al lexema y al
número de línea (formato `//[Error:lexema|nroLinea]`, igual que en la Etapa 2).

## Estructura

- `moduloprincipal.ModuloPrincipal`: interfaz con el usuario. Crea la
  `TablaDeSimbolos` (con las entidades predefinidas), el `AnalizadorLexico` y el
  `AnalizadorSintactico`; dispara el análisis, ejecuta el chequeo de
  declaraciones, la consolidación y el chequeo de cuerpos, genera el código y lo
  escribe en el archivo de salida.
- `analizadorsintactico.AnalizadorSintactico`: parser descendente recursivo que,
  además de validar la sintaxis, ejecuta las acciones semánticas que construyen
  la tabla de símbolos (controles de nombres repetidos de la primera pasada) y los
  AST de los cuerpos de métodos y constructores.
- `analizadorsemantico`: modelo de la Tabla de Símbolos.
  - `Generador`: calcula los offsets (RA, CIR y VT) y recorre la TS y los AST
    para emitir el programa CeIASM (entry point, rutinas de heap, tablas
    virtuales, unidades y entidades predefinidas).
  - `TablaDeSimbolos`: tabla global de tipos, entidades predefinidas, contexto
    actual y punto de entrada de `estaBienDeclarada()`, `consolidar()`,
    `chequearCuerpos()` y `generarCodigo()`.
  - `Entidad` (abstracta): `Clase`, `Interfaz`, `Unidad`, `Atributo`,
    `Parametro`; `Unidad` (abstracta) agrupa `Metodo` y `Constructor`. Todas
    conservan el token de su declaración; las entidades con ubicación en memoria
    (atributos, parámetros, variables locales, métodos, unidades) conservan su
    offset y su etiqueta CeIASM.
  - `Tipo` (abstracta): `TipoPrimitivo`, `TipoVoid`, `TipoParametro`,
    `TipoArreglo`, `TipoReferencia`.
  - `ExcepcionSemantica`: error semántico con lexema, línea y mensaje.
  - `Unidad`, `VariableLocal`, `Bloque`, `InfoCheck`, `SemanticaUtil`,
    `TipoNull`: soporte del chequeo de sentencias (Etapa 4).
- `analizadorsemantico.ast`: nodos del AST (expresiones, encadenados y
  sentencias) con sus métodos de chequeo.
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
  org.junit.runner.JUnitCore test.TesterDeCasosSinErrores \
  test.TesterDeCasosConErrores
```

Los testers de generación recorren `resources/sinErrores/`: para cada caso
generan el archivo `[caso].out` con el compilador y lo ejecutan con la CeIVM,
comparando la salida de ejecución con la que indica la primera línea del archivo
(con el formato `///valor1&valor2&...&exitosamente`, igual que el tester de la
cátedra). Algunos casos esperan en cambio un error de ejecución (índice fuera de
rango, tamaño negativo, referencia nula, división por cero). Los casos de
`resources/conErrores/` deben rechazarse; la primera línea del archivo lleva el
código esperado con el formato `//[Error:lexema|nroLinea]`. El tester los invoca
con un archivo de salida temporal, porque los casos del punto de entrada de la
Etapa 5 (`main`) solo se controlan cuando se genera código.

Los casos de `resources/` prueban sólo la generación de código: todos se
compilan y se ejecutan en la CeIVM (o son programas que deben rechazarse,
incluidos los del punto de entrada). No hay casos de sólo análisis, ni fixtures
que usen construcciones de logro. El corpus externo se importó ya filtrado (ver
`../TESTERS EXTERNOS FILTRADOS/`).

Para agregar un caso basta con crear el archivo en la carpeta correspondiente;
los testers lo incorporan automáticamente.
