# AGENTS.md

MiniJava compiler (UNS Compiladores, Etapa 4). Work happens in
`compilador-minijava/`; use it as cwd for all commands.

## Current state (verified)

- Branch `etapa-4`. `src/` implements Etapa 3 (parse-time symbol table plus
  declaration checking) and the **base** of Etapa 4: AST construction for
  method/constructor bodies (`analizadorsemantico.ast`) and sentence checking
  (name resolution, type checking, structural info, local scopes, static
  context). Logros are not implemented.
- Spec: `../md/Pautas Etapa 4/Pautas Etapa 4.md` and the full semantics in
  `../md/MiniJava-Semantica-Completa-2026/MiniJava-Semantica-Completa-2026.md`
  (both Spanish). `Etapa4_Base.md` documents the Etapa 4 base phase by phase;
  the README lists compile/run and the module structure.
- `./CorrerTests.sh` reports `OK (146 tests)`: the 10 catedra fixtures
  (`semIIError01-05`, `semIICorrecto01-05`, intact) plus the `testersSala`
  battery renumbered from `06` (`semIIError06..77`, `semIICorrecto06..70`).
  The discarded originals live in `../tests_descartados/`; the sala case
  `semIICorrecto56` (renumbered `61`) was discarded because `crear()` without a
  receiver does not resolve per §10.2.
- Error-token convention (Pautas): the lexeme is the token of the node that has
  the error: the operator/keyword (`+`, `=`, `[`, `new`, `return`, `var`) or the
  name of the variable/attribute/method (`m2`, `hacer`, `nombre`, `length`).
  Do not use a child subexpression token or a bare `.`.
- Etapa 4 deliverable also needs a report (compile/run instructions, AST class
  diagram, logros attempted); `Informe_etapa1-3.*` cover earlier stages only.

## Commands

- `./Compilar.sh` — deletes `build/`, compiles every package, rebuilds
  `Compilador.jar` (Windows: `Compilar.bat`).
- `java -jar Compilador.jar <fuente>` — accepts any file extension.
- `./CorrerTests.sh` — builds, compiles testers, runs both JUnit classes. First
  run needs `curl` to download JUnit/Hamcrest into `lib/`.
- Single case: `java -jar Compilador.jar resources/conErrores/<caso>.java` and
  compare stdout with line 1 minus its 3 leading chars. JUnitCore cannot select
  one parameterized case.
- No lint/typecheck/formatter; the `javac` call in the build scripts is the only
  static check.

## Architecture notes

- Entrypoint/manifest main: `moduloprincipal.ModuloPrincipal`. Flow: open file
  -> lexer -> parser (also fills the TS) -> `estaBienDeclarada()` ->
  `consolidar()` -> print result.
- Parser is hand-written recursive descent, one method per nonterminal plus
  `PRIMEROS_*` sets; semantic actions build the symbol table inline. There is no
  separate AST layer yet.
- AST nodes live in `analizadorsemantico/ast/`; every expression exposes
  `InfoCheck chequear(ts)` and every sentence `void chequear(ts)`. Bodies are
  attached to `Unidad` and checked by `TablaDeSimbolos.chequearCuerpos()` after
  `estaBienDeclarada()` and `consolidar()`.
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
- `main` is not required (Etapas 3 and 4). `null` and `void` have special rules.
- Assignment and "usable as a call statement" are independent of type: `a.length`
  and `a[0]` can both be `int`, but only the latter is assignable. A parenthesized
  expression keeps its type but not the structural traits of its contents.
- When in doubt, consult the full semantics doc; do not infer from Java.

## Output and test contract

- Success must print `[SinErrores]`; an error must print a message plus
  `[Error:<lexema>|<linea>]` and stop after the first error.
- Error fixtures put the expected code on line 1, e.g. `//[Error:v1|9]` or
  `///[Error:v1|9]`; testers strip the first 3 chars and assert stdout contains
  the rest. Files added to `resources/sinErrores/` or `resources/conErrores/`
  are auto-discovered and sorted by filename.
- Testers invoke `ModuloPrincipal.main` statically through a null field; keep
  `main` static with signature `main(String[])`.
- Tester paths are relative, so manual `java -cp ...` runs must start from
  `compilador-minijava/`.
- `build/`, `Compilador.jar`, and `lib/` are generated/ignored; never edit or
  commit them.
