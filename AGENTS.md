# AGENTS.md

MiniJava compiler (UNS Compiladores, Etapa 5). Work happens in
`compilador-minijava/`; use it as cwd for all commands.

## Current state (verified)

- Branch `etapa-5`. `src/` implements Etapas 3-4 (parse-time symbol table plus
  declaration checking, ASTs for method/constructor bodies, sentence checking)
  and the **base** of Etapa 5: CeIVM code generation. Logros are not
  implemented.
- Etapa 5 adds `analizadorsemantico.Generador` (offset/label calculation and
  CeIASM emission) and `void generar(Generador)` on AST nodes and entities.
  `ModuloPrincipal` now takes `<fuente> [<salida>]` and writes the CeIASM file.
- Spec: `../md/Pautas Etapa 5/Pautas Etapa 5.md`, the CeIVM notes in
  `../md/Clase 12..16 ...` and `../md/Apunte-CeIVM-2026/Apunte-CeIVM-2026.md`
  (all Spanish), plus `../CeiVM2026/readme CeIVM.txt` and
  `../CeiVM2026/rutinasHeap.txt`. `Informe_etapa5.tex` documents the stage.
- `./CorrerTests.sh` reports `OK (191 tests)`.
- Etapa 5 tester contract: `resources/sinErrores/` holds generation cases; each
  compiles to `[caso].out` and is executed with `CeIVM-cei2011.jar`, comparing
  runtime output against line 1 (`///v1&v2&...&exitosamente`). Some cases expect
  a runtime error instead (out of bounds, negative size, null, division by zero).
  `resources/conErrores/` holds the Etapa 5 entry-point cases plus the Etapa 4
  semantic errors; `TesterDeCasosConErrores` invokes the compiler with a
  temporary output file so the entry-point control runs (the Etapa 4 cases ignore
  it and keep the same code).
- `resources/` is only about code generation: everything compiles and runs (or
  is a rejected program). There are no logro fixtures and no analyze-only
  fixtures; the Etapa 4 semantic corpus was removed with its tester.
- `CeIVM-cei2011.jar` must sit in `compilador-minijava/` for the tester (it is
  committed).
- The external cross-check `../TESTERS EXTERNOS FILTRADOS/correr.py` (85 cases,
  Etapa 5 base only) passes with 0 fallos; `chars` and `strings_con_escapes` are
  excluded as logro (unicode escapes).
- Etapa 5 also covers the Apunte's base requirements beyond raw generation:
  entry-point control (`static void main()`), array bounds/size checks, String
  literals as heap objects, `Object.toString` at VT offset 0, generic virtual
  dispatch and arrays of any number of dimensions.

## Commands

- `./Compilar.sh` — deletes `build/`, compiles every package, rebuilds
  `Compilador.jar` (Windows: `Compilar.bat`).
- `java -jar Compilador.jar <fuente> [<salida>]` — accepts any file extension;
  without `<salida>` it only analyzes.
- `java -jar CeIVM-cei2011.jar <salida>` — runs the generated code.
- `./CorrerTests.sh` — builds, compiles testers, runs the two JUnit classes.
  First run needs `curl` to download JUnit/Hamcrest into `lib/`.
- Single case: compare the compiler stdout (`[SinErrores]` or `[Error:...]`) and,
  for generation cases, run the CeIVM on `[caso].out`. JUnitCore cannot select
  one parameterized case.
- No lint/typecheck/formatter; the `javac` call in the build scripts is the only
  static check.

## Architecture notes

- Entrypoint/manifest main: `moduloprincipal.ModuloPrincipal`. Flow: open file
  -> lexer -> parser (also fills the TS and builds ASTs) -> `estaBienDeclarada()`
  -> `consolidar()` -> `chequearCuerpos()` -> (if an output file was given)
  `chequearPuntoDeEntrada()` -> `generarCodigo()` -> write output.
- AST nodes live in `analizadorsemantico/ast/`; expressions expose
  `InfoCheck chequear(ts)` and `void generar(g)`, sentences `void chequear(ts)`
  and `void generar(g)`. Chained accesses add `generar(g, destino)`. The check
  pass also records resolved entities (offsets, methods, constructors) that the
  generation pass uses. For generic receivers the node keeps the *declared*
  method (not the instantiated view) so generation sees the VT offset/etiqueta.
- `analizadorsemantico.Generador` centralizes line/label buffers, the unit
  context (arity, static, return offset, live locals, scratch pool),
  `calcularOffsets()` (CIR, VT, RA) and the full program emission (entry point,
  heap routines, error routines, VTs, units, predefinidas, String literals).
- `reservarTemporal()`/`liberarTemporal()` hand out cells from a fixed pool
  reserved at unit entry (`RMEM TAM_POOL`), so temporaries are valid even when
  the expression stack is not empty (array dimensions, indices).
- Array access emits inline bounds checks (negative and `>= length`) that jump
  to `lErrorIndice`; `new` checks every dimension for a negative size and jumps
  to `lErrorTamanio`. Both routines print a message with `SPRINT` and `HALT`.
- String literals are built in the `.heap` at each evaluation (`generarString`),
  as `[VT, length, chars..., 0]`; `System.printS*` adds the offset 2 before
  `SPRINT`. `Object.toString` builds the empty String the same way.
- New source packages must be added to the `javac` line in both `Compilar.sh`
  and `Compilar.bat` (they glob `src/main/java/<pkg>/*.java`).
- Keep identifiers, comments, and error messages in Spanish (e.g.
  `estaBienDeclarada`) to match the existing code and tests.

## MiniJava vs Java (do not assume Java semantics)

- Generics and arrays are invariant; there are no raw types, no erased members,
  and no unchecked conversions. Generic inheritance without an argument is a
  MiniJava-only exception that keeps the original parameter.
- Constructors do not implicitly call a super constructor; no method is required
  to return on every path and unreachable code is not rejected.
- `main` is not required when only analyzing (no output file, Etapas 3-4). When
  an output file is given, exactly one `static void main()` must exist: missing
  or malformed gives `[Error:$|<lineaEOF>]`, duplicated gives `[Error:main|<n>]`
  on the second one.
- `null` and `void` have special rules. Assignment and "usable as a call
  statement" are independent of type. A parenthesized expression keeps its type
  but not the structural traits of its contents.
- When in doubt, consult the full semantics doc; do not infer from Java.

## Output and test contract

- Success must print `[SinErrores]`; an error must print a message plus
  `[Error:<lexema>|<linea>]` and stop after the first error.
- Error fixtures put the expected code on line 1, e.g. `//[Error:v1|9]`;
  testers strip the first 3 chars and assert stdout contains the rest.
- Generation fixtures put the expected CeIVM output on line 1 as `///a&b`;
  the tester runs `java -jar CeIVM-cei2011.jar [caso].out` and asserts the
  output contains each `a`, `b`, ... (the last one is `exitosamente`).
- Testers invoke `ModuloPrincipal.main` statically through a null field; keep
  `main` static with signature `main(String[])`.
- Tester paths are relative, so manual runs must start from
  `compilador-minijava/`.
- `build/`, `Compilador.jar`, and `lib/` are generated/ignored; never edit or
  commit them.
