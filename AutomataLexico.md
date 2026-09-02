# Autómata Finito Determinista del Analizador Léxico — MiniJava, Etapa 1

Especificación en notación clara del autómata implementado en
`AnalizadorLexico.java` (un estado = un método).

## Convenciones

- **Clases de caracteres** usadas en las transiciones:
  - `dígito` = `[0-9]`
  - `minúscula` = `[a-z]`
  - `mayúscula` = `[A-Z]`
  - `charId` = `[A-Za-z0-9_]`
  - `blanco` = espacio | `\t` | `\n`
  - `EOF` = fin de archivo (lexema `$`)
- **Regla de cierre (estados finales):** ante cualquier carácter
  *sin transición propia*, se emite el token asociado al estado y se vuelve a `e0`
  **sin consumir** ese carácter (queda como adelanto para el próximo token).
- **EOF** actúa como "otro carácter" en los estados finales (cierra el token) y
  como **error** en `eString`, `eStringEscape`, `eCaracter`, `eCaracterEscape`,
  `eCaracterFin` y `eComentarioBloque`.
- Ante un error léxico, el estado lanza `ExcepcionLexica` (sin transición interna a
  `e0`). El módulo principal la captura, reporta el error y vuelve a pedir tokens,
  retomando en `e0` desde la posición de lectura actual (multi-detección de errores;
  en `e0` el carácter ofensor se consume para garantizar el progreso).
- El `SourceManagerEficiente` normaliza `\r\n` y `\r` a `\n` **antes** del autómata,
  por lo que el autómata solo ve `\n`.

## Estados

| Estado              | Tipo        | Token(s) emitidos                                  | Método correspondiente     |
|---------------------|-------------|----------------------------------------------------|----------------------------|
| `e0`                | inicial     | —                                                  | `e0()`                     |
| `eEntero`           | final       | `litInt` (máx. 9 dígitos, con contador)             | `eEntero(int)`             |
| `eIdMetVar`         | final       | `idMV` o `pr_<palabra>` (tabla de reservadas)       | `eIdMetVar()`              |
| `eIdMayuscula`      | final       | `idGen`                                             | `eIdMayuscula()`           |
| `eIdClase`          | final       | `idClase`                                           | `eIdClase()`               |
| `eString`           | intermedio  | —                                                   | `eString()`                |
| `eStringEscape`     | intermedio  | —                                                   | `eStringEscape()`          |
| `litString`         | final       | `litString`                                         | (cierre en `eString`)      |
| `eCaracter`         | intermedio  | —                                                   | `eCaracter()`              |
| `eCaracterEscape`   | intermedio  | —                                                   | `eCaracterEscape()`        |
| `eCaracterFin`      | intermedio  | —                                                   | `eCaracterFin()`           |
| `litChar`           | final       | `litChar`                                           | (cierre en `eCaracterFin`) |
| `eOpMas`            | final       | `op+`                                               | `eOpMas()`                 |
| `eOpMenos`          | final       | `op-`                                               | `eOpMenos()`               |
| `eOpIgual`          | final       | `op=`                                               | `eOpIgual()`               |
| `eOpNot`            | final       | `op!`                                               | `eOpNot()`                 |
| `eOpMayor`          | final       | `op>`                                               | `eOpMayor()`               |
| `eOpMenor`          | final       | `op<`                                               | `eOpMenor()`               |
| `eOpAnd`            | intermedio  | —                                                   | `eOpAnd()`                 |
| `eOpOr`             | intermedio  | —                                                   | `eOpOr()`                  |
| `eOpDiv`            | final       | `op/`                                               | `eOpDiv()`                 |
| `eComentarioLinea`  | intermedio  | — (descarta el comentario)                          | `eComentarioLinea()`       |
| `eComentarioBloque` | intermedio  | — (descarta el comentario)                          | `eComentarioBloque()`      |
| `EOF`               | final       | `EOF` (lexema `$`)                                  | (cierre en `e0`)           |
| símbolos simples    | finales     | `op*`, `op%`, `parA`, `parC`, `llaveA`, `llaveC`, `corcheteA`, `corcheteC`, `puntoComa`, `coma`, `punto`, `dosPuntos` | (cierre en `e0`) |
| `ERROR`             | de error    | lanza `ExcepcionLexica`                             | `error(String)`            |

## Transiciones

### e0 (inicial)

| Entrada                                   | Destino                                      |
|-------------------------------------------|----------------------------------------------|
| `dígito`                                   | `eEntero`                                    |
| `minúscula`                                | `eIdMetVar`                                  |
| `mayúscula`                                | `eIdMayuscula`                               |
| `"`                                        | `eString`                                    |
| `'`                                        | `eCaracter`                                  |
| `blanco`                                   | `e0` (descarta)                              |
| `EOF`                                      | `EOF` (final)                                |
| `+` `-` `=` `!` `>` `<` `/`                 | `eOpMas` `eOpMenos` `eOpIgual` `eOpNot` `eOpMayor` `eOpMenor` `eOpDiv` |
| `&` `|`                                    | `eOpAnd` `eOpOr`                             |
| `*` `%`                                    | finales `op*` `op%`                          |
| `(` `)` `{` `}` `[` `]` `;` `,` `.` `:`    | finales `parA` `parC` `llaveA` `llaveC` `corcheteA` `corcheteC` `puntoComa` `coma` `punto` `dosPuntos` |
| otro                                       | **ERROR** ("no es un símbolo valido")        |

### Identificadores

| Estado         | Entrada    | Destino                          |
|----------------|------------|----------------------------------|
| `eIdMetVar`    | `charId`   | `eIdMetVar` (loop)               |
| `eIdMetVar`    | otro       | **acepta**: `idMV` (o `pr_*` si el lexema está en la tabla de reservadas) |
| `eIdMayuscula` | `charId`   | `eIdClase`                       |
| `eIdMayuscula` | otro       | **acepta**: `idGen`              |
| `eIdClase`     | `charId`   | `eIdClase` (loop)                |
| `eIdClase`     | otro       | **acepta**: `idClase`            |

### Literal entero

| Estado    | Entrada  | Destino                                        |
|-----------|----------|------------------------------------------------|
| `eEntero` | `dígito` | `eEntero` (loop; incrementa contador)          |
| `eEntero` | otro     | **acepta**: `litInt` — si el contador es > 9 → **ERROR** ("literal entero demasiado largo") |

### Literal string

| Estado           | Entrada                          | Destino                     |
|------------------|----------------------------------|-----------------------------|
| `eString`        | `"`                              | **acepta**: `litString`     |
| `eString`        | `\`                              | `eStringEscape`             |
| `eString`        | `\n` o `EOF`                     | **ERROR** ("literal string mal formado") |
| `eString`        | otro                             | `eString` (loop)            |
| `eStringEscape`  | `\n` o `EOF`                     | **ERROR** ("literal string mal formado") |
| `eStringEscape`  | otro                             | `eString`                   |

### Literal caracter

| Estado            | Entrada                  | Destino                                    |
|-------------------|--------------------------|--------------------------------------------|
| `eCaracter`       | `'`                      | **ERROR** ("literal caracter mal formado") |
| `eCaracter`       | `\n` o `EOF`             | **ERROR** ("literal caracter mal formado") |
| `eCaracter`       | `\`                      | `eCaracterEscape`                          |
| `eCaracter`       | otro                     | `eCaracterFin`                             |
| `eCaracterEscape` | `\n` o `EOF`             | **ERROR** ("literal caracter mal formado") |
| `eCaracterEscape` | otro                     | `eCaracterFin`                             |
| `eCaracterFin`    | `'`                      | **acepta**: `litChar`                      |
| `eCaracterFin`    | otro                     | **ERROR** ("literal caracter mal formado") |

### Operadores (criterio de lexema más largo)

| Estado      | Entrada | Destino                                            |
|-------------|---------|----------------------------------------------------|
| `eOpMas`    | `+`     | **acepta**: `op++`; otro → **acepta**: `op+`       |
| `eOpMenos`  | `-`     | **acepta**: `op--`; otro → **acepta**: `op-`       |
| `eOpIgual`  | `=`     | **acepta**: `op==`; otro → **acepta**: `op=`       |
| `eOpNot`    | `=`     | **acepta**: `op!=`; otro → **acepta**: `op!`       |
| `eOpMayor`  | `=`     | **acepta**: `op>=`; otro → **acepta**: `op>`       |
| `eOpMenor`  | `=`     | **acepta**: `op<=`; otro → **acepta**: `op<`       |
| `eOpAnd`    | `&`     | **acepta**: `op&&`; otro → **ERROR** (`&` no es símbolo válido) |
| `eOpOr`     | `\|`    | **acepta**: `op\|\|`; otro → **ERROR** (`\|` no es símbolo válido) |
| `eOpDiv`    | `/`     | `eComentarioLinea`; otro → **acepta**: `op/`       |
| `eOpDiv`    | `*`     | `eComentarioBloque`                                |

### Comentarios

| Estado                | Entrada          | Destino                          |
|-----------------------|------------------|----------------------------------|
| `eComentarioLinea`    | `\n` o `EOF`     | `e0` (comentario descartado)     |
| `eComentarioLinea`    | otro             | `eComentarioLinea` (loop)        |
| `eComentarioBloque`   | `*`              | `eComentarioBloque (*)`          |
| `eComentarioBloque`   | `EOF`            | **ERROR** ("comentario sin cerrar") |
| `eComentarioBloque`   | otro             | `eComentarioBloque` (loop)       |
| `eComentarioBloque (*)` | `/`            | `e0` (cierra `*/`)               |
| `eComentarioBloque (*)` | otro (incl. `EOF`) | `eComentarioBloque`          |

## Notas

- **`eEntero` con contador:** un AFD "puro" para el máximo de 9 dígitos exigiría
  encadenar 9 estados (`e1..e9`). La implementación usa un contador entero, que es
  semánticamente equivalente y evita inflar el dibujo. Si se prefiere la versión
  pura, basta desenrollar ese estado.
- **Palabras reservadas:** se resuelven en el cierre de `eIdMetVar` consultando una
  tabla (`class`, `extends`, ..., `false`), según lo visto en clase; no ocupan
  estados propios del autómata.
- **"Símbolo simple" en el dibujo:** los 12 tokens de un solo carácter (`op*`, `op%`,
  `parA`, `parC`, `llaveA`, `llaveC`, `corcheteA`, `corcheteC`, `puntoComa`, `coma`,
  `punto`, `dosPuntos`) se dibujan como un único estado final porque comparten el
  mismo comportamiento (emitir y volver a `e0`); el token concreto lo indica la
  etiqueta de cada flecha. En la implementación son cierres directos en `e0`.
