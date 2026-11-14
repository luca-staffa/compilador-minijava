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
- `./CorrerTests.sh` reports `OK (179 tests)`.
- Etapa 5 tester contract: `resources/sinErrores/` holds generation cases; each
  compiles to `[caso].out` and is executed with `CeIVM-cei2011.jar`, comparing
  runtime output against line 1 (`///v1&v2&...&exitosamente`). The Etapa 4
  semantic fixtures (no `main`) live in `resources/sinErroresSemantico/` and are
  checked by `TesterDeCasosSinErroresSemantico`. `resources/conErrores/` is
  unchanged.
- `CeIVM-cei2011.jar` must sit in `compilador-minijava/` for the tester (it is
  committed).

## Commands

- `./Compilar.sh` — deletes `build/`, compiles every package, rebuilds
  `Compilador.jar` (Windows: `Compilar.bat`).
- `java -jar Compilador.jar <fuente> [<salida>]` — accepts any file extension;
  without `<salida>` it only analyzes.
- `java -jar CeIVM-cei2011.jar <salida>` — runs the generated code.
- `./CorrerTests.sh` — builds, compiles testers, runs the three JUnit classes.
  First run needs `curl` to download JUnit/Hamcrest into `lib/`.
- Single case: compare the compiler stdout (`[SinErrores]` or `[Error:...]`) and,
  for generation cases, run the CeIVM on `[caso].out`. JUnitCore cannot select
  one parameterized case.
- No lint/typecheck/formatter; the `javac` call in the build scripts is the only
  static check.

## Architecture notes

- Entrypoint/manifest main: `moduloprincipal.ModuloPrincipal`. Flow: open file
  -> lexer -> parser (also fills the TS and builds ASTs) -> `estaBienDeclarada()`
  -> `consolidar()` -> `chequearCuerpos()` -> `generarCodigo()` -> write output.
- AST nodes live in `analizadorsemantico/ast/`; expressions expose
  `InfoCheck chequear(ts)` and `void generar(g)`, sentences `void chequear(ts)`
  and `void generar(g)`. Chained accesses add `generar(g, destino)`. The check
  pass also records resolved entities (offsets, methods, constructors) that the
  generation pass uses.
- `analizadorsemantico.Generador` centralizes line/label buffers, the unit
  context (arity, static, return offset, live locals), `calcularOffsets()` (CIR,
  VT, RA) and the full program emission (entry point, heap routines, VTs, units,
  predefinidas, string literals).
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
- `main` is not required (Etapas 3-4); Etapa 5 generation calls it when present.
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
