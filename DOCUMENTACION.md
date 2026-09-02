# Etapa 1 - Analizador Léxico de MiniJava

Proyecto de Compiladores e Intérpretes UNS, 2026

Implementación de un analizador lexico para MiniJava junto con un módulo principal de
consola. El analizador se construye a partir de un autómata finito determinista implementado manualmente (un método por estado).

## Estructura del proyecto

```
codigo/
├── Compilar.sh / Compilar.bat          Compila y genera Compilador.jar
├── CorrerTests.sh / CorrerTests.bat    Compila y ejecuta los testers JUnit
├── lib/                                Jars de JUnit 4 y Hamcrest (se descargan solos la primera vez)
├── src/main/java/
│   ├── moduloprincipal/
│   │   └── ModuloPrincipal.java        Módulo principal (interfaz con el usuario)
│   ├── analizadorlexico/
│   │   ├── AnalizadorLexico.java       Autómata finito determinista
│   │   ├── Token.java                  Representación de un token (nombre, lexema, linea)
│   │   └── ExcepcionLexica.java        Excepción propagada ante un error lexico
│   └── sourcemanager/
│       ├── SourceManager.java          Interfaz provista por la cátedra
│       ├── SourceManagerImpl.java      Implementación provista (por lineas, referencia)
│       └── SourceManagerEficiente.java Implementación propia por caracteres (se usa)
├── src/test/java/test/                 Testers JUnit provistos por la cátedra
└── resources/
    ├── sinErrores/                     Casos de prueba sin errores
    └── conErrores/                     Casos de prueba con errores
```

## Componentes

### Módulo principal (ModuloPrincipal)

Recibe por parámetro la ruta del archivo fuente MiniJava (acepta cualquier extensión):

```
java -jar Compilador.jar programa1.java
```

Asocia el archivo a un `SourceManagerEficiente`, crea el analizador léxico y le solicita
tokens hasta recibir el token de fin de archivo. Cada token se imprime por pantalla con el
formato `(NombreToken,Lexema,NroLinea)`. Al finalizar sin errores se imprime `[SinErrores]`.

Ante un error léxico, la excepcion `ExcepcionLexica` es capturada por el modulo principal,
que reporta el mensaje de error y el codigo de error, finalizando la ejecucion.

### Manejador de archivos eficiente (SourceManagerEficiente)

Implementa la interfaz `SourceManager` leyendo el fuente de a caracteres (en lugar de
lineas como la implementacion provista). Características:

- Lectura por caracteres con `InputStreamReader` (UTF-8) sobre `PushbackReader`.
- Reconoce las tres convenciones de salto de línea (LF, CR y CRLF), normalizandolas a un
  unico `\n`. La secuencia `\r\n` no se contabiliza como dos saltos.
- `getLineNumber()` devuelve la linea del ultimo caracter devuelto. Un caracter de salto
  de linea pertenece a la linea que termina, y el numero de linea se incrementa al leer
  el siguiente caracter. De esta forma el token EOF reporta la linea del final fisico
  del archivo (por ejemplo, un archivo de 21 lineas que termina con salto de linea
  reporta el EOF en la linea 22).

La implementacion provista por la catedra (`SourceManagerImpl`) se incluye como
referencia, pero no se utiliza en la ejecucion porque su conteo de lineas difiere en el
token EOF cuando el archivo termina con un salto de linea.

### Analizador lexico (AnalizadorLexico)

Automata finito determinista implementado con un metodo por estado. El estado inicial
`e0` descarta espacios en blanco y comentarios, y despacha al estado correspondiente
segun el primer caracter del lexema. Cada metodo consulta `caracterActual` (un caracter
de adelanto que nunca se consume al finalizar un token), lo agrega al lexema y avanza
leyendo el proximo caracter.

Las palabras reservadas se resuelven consultando una tabla: al construir un identificador
de metodo o variable se verifica si su lexema corresponde a una palabra reservada y, en
ese caso, se devuelve el token de palabra reservada correspondiente.

## Tokens del lenguaje

Se adoptaron los siguientes nombres para los tokens.

### Palabras reservadas

Se reconocen con un identificador de metodo o variable y se devuelven con nombre
`pr_<palabra>`:

`class` `extends` `interface` `implements` `static` `boolean` `char` `int` `void`
`public` `if` `else` `while` `return` `var` `this` `new` `null` `true` `false`

Los nombres de tokens siguen el ejemplo de las pautas de la Etapa 1: `litString`
para los literales string, `idMV` para identificadores de metodo o variable,
`pr_<palabra>` para palabras reservadas, `op<simbolo>` para operadores y
`llaveC` para puntuacion.

### Identificadores

| Token      | Expresion regular         | Descripcion                                    |
|------------|---------------------------|------------------------------------------------|
| `idClase`  | `[A-Z][A-Za-z0-9_]+`      | Clase o interfaz                               |
| `idGen`    | `[A-Z]`                   | Parametro de tipo generico (una sola mayuscula)|
| `idMV`     | `[a-z][A-Za-z0-9_]*`      | Metodo o variable                              |

### Literales

| Token          | Expresion regular                        | Restricciones                                            |
|----------------|------------------------------------------|----------------------------------------------------------|
| `litInt`       | `[0-9]{1,9}`                             | Maximo 9 digitos; mas de 9 es error                       |
| `litChar`      | `'[^\\'\n]'` o `'\\[^\n]'`               | Un caracter normal, o barra invertida seguida de cualquier caracter |
| `litString`    | `"([^"\\\n]|\\[^\n])*"`                 | Sin saltos de linea; `\"` no cierra el literal            |

Los literales `true`, `false` y `null` se representan con las palabras reservadas
`pr_true`, `pr_false` y `pr_null`.

### Operadores

`op+` `op-` `op*` `op/` `op%` `op<` `op>` `op!` `op=` `op==` `op>=` `op<=` `op!=`
`op&&` `op||` `op++` `op--`

Los operadores se reconocen con el criterio del lexema mas largo (por ejemplo, `>=`
antes que `>`). Los caracteres `&` y `|` no forman operadores por si solos: un `&` o `|`
no seguido de otro igual es un error lexico.

### Puntuacion

`parA` `(` | `parC` `)` | `llaveA` `{` | `llaveC` `}` | `corcheteA` `[` | `corcheteC` `]`
| `puntoComa` `;` | `coma` `,` | `punto` `.` | `dosPuntos` `:`

### Fin de archivo

Token `EOF` con lexema `$`.

## Elementos ignorados

- Espacios en blanco: espacio, tabulador y salto de linea (LF, CR o CRLF).
- Comentarios de una linea: desde `//` hasta el final de la linea.
- Comentarios multilinea: desde `/*` hasta `*/`.

## Errores lexicos

Al detectar un error se lanza `ExcepcionLexica` con el lexema armado hasta el momento y
el numero de linea, y el modulo principal finaliza la ejecucion reportando:

```
Error Lexico en linea <N>: <mensaje>
[Error:<lexema>|<N>]
```

Para que el codigo de error quede en una sola linea, en el caso de un comentario
multilinea sin cerrar el lexema reportado es el que abre el comentario, `/*`.

Mensajes segun la naturaleza del error:

| Caso                                     | Mensaje                                 |
|------------------------------------------|-----------------------------------------|
| Caracter invalido (no es simbolo valido) | `<lexema> no es un simbolo valido`      |
| String sin comilla de cierre             | `literal string mal formado`            |
| Caracter mal formado                     | `literal caracter mal formado`          |
| Entero con mas de 9 digitos              | `literal entero demasiado largo`        |
| Comentario multilinea sin cerrar         | `comentario sin cerrar`                 |

## Formato de salida

### Analisis exitoso

Un token por linea con el formato `(NombreToken,Lexema,NroLinea)`. El lexema incluye los
delimitadores para strings y caracteres (por ejemplo, `(litString,"hola",2)` o
`(litChar,'\n',3)`). Al final se imprime el token de fin de archivo y:

```
[SinErrores]
```

### Analisis con errores

```
Error Lexico en linea 2: # no es un simbolo valido
[Error:#|2]
```

Toda la salida se realiza con `System.out`.

## Compilacion y ejecucion

No se usa ninguna herramienta de build: solo se necesitan `javac`, `jar` y `java` de
cualquier JDK 21 o superior (probado con JDK 25).

```
./Compilar.sh             # compila y genera Compilador.jar  (Windows: Compilar.bat)
java -jar Compilador.jar resources/sinErrores/lexSinErrores01.java
./CorrerTests.sh          # compila y ejecuta los testers JUnit  (Windows: CorrerTests.bat)
```

`CorrerTests.sh` descarga automaticamente `junit-4.13.2.jar` y `hamcrest-core-1.3.jar`
en `lib/` si no estan (requiere `curl`); si no hay red, se pueden descargar a mano de
Maven Central y dejarlos en esa carpeta.

### Comandos manuales equivalentes

Compilar (desde `codigo/`):

```
javac -d build/classes src/main/java/moduloprincipal/*.java src/main/java/analizadorlexico/*.java src/main/java/sourcemanager/*.java
jar cfe Compilador.jar moduloprincipal.ModuloPrincipal -C build/classes .
```

Correr los tests (desde `codigo/`, con JUnit 4.13.2 y Hamcrest 1.3 en `lib/`):

```
javac -cp "build/classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d build/test-classes src/test/java/test/*.java
java -cp "build/classes:build/test-classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore test.TesterDeCasosSinErrores test.TesterDeCasosConErrores
```

En Windows el separador del classpath es `;` en lugar de `:`.

Los testers (`TesterDeCasosSinErrores` y `TesterDeCasosConErrores`) usan el working
directory `codigo/` para acceder a `resources/sinErrores/` y `resources/conErrores/`
(los scripts ya se posicionan ahi; los comandos manuales deben correrse desde `codigo/`).

## Tests provistos por la catedra

Casos de prueba incluidos (copiados del material de la catedra):

- `resources/sinErrores/lexSinErrores01.java` - identificadores, strings, chars y EOF.
- `resources/sinErrores/lexSinErrores02.java` - ejemplo de la convencion de salida.
- `resources/sinErrores/lexSinErrores03.java` - archivo solo con comentarios.
- `resources/conErrores/lexConErrores01.java` - caracter invalido `#`.
- `resources/conErrores/lexConErrores02.java` - string sin comilla de cierre.

## Logros

- **Manejador de Archivos Eficiente**: `SourceManagerEficiente` lee de a caracteres y
  conforma con la interfaz `SourceManager`, sin modificar la interfaz provista.
