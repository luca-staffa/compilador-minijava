package analizadorsintactico;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import analizadorlexico.AnalizadorLexico;
import analizadorlexico.Token;

/**
 * Analizador Sintactico Descendente Recursivo para MiniJava.
 *
 * Implementa la estrategia recursiva simple: un metodo por cada no terminal
 * de la gramatica LL(1), eligiendo la produccion a aplicar segun los
 * primeros de sus partes derechas. Los terminales se consumen con match()
 * y los errores se reportan lanzando ExcepcionSintactica.
 */
public class AnalizadorSintactico {

    private static final Map<String, String> DESCRIPCIONES = new HashMap<>();

    static {
        DESCRIPCIONES.put("EOF", "fin de archivo");
        DESCRIPCIONES.put("idClase", "un id de clase");
        DESCRIPCIONES.put("idMV", "un identificador de metodo o variable");
        DESCRIPCIONES.put("idGen", "un parametro de tipo generico");
        DESCRIPCIONES.put("litInt", "un literal entero");
        DESCRIPCIONES.put("litChar", "un literal caracter");
        DESCRIPCIONES.put("litString", "un literal string");

        DESCRIPCIONES.put("pr_class", "'class'");
        DESCRIPCIONES.put("pr_extends", "'extends'");
        DESCRIPCIONES.put("pr_interface", "'interface'");
        DESCRIPCIONES.put("pr_implements", "'implements'");
        DESCRIPCIONES.put("pr_static", "'static'");
        DESCRIPCIONES.put("pr_boolean", "'boolean'");
        DESCRIPCIONES.put("pr_char", "'char'");
        DESCRIPCIONES.put("pr_int", "'int'");
        DESCRIPCIONES.put("pr_void", "'void'");
        DESCRIPCIONES.put("pr_public", "'public'");
        DESCRIPCIONES.put("pr_private", "'private'");
        DESCRIPCIONES.put("pr_if", "'if'");
        DESCRIPCIONES.put("pr_else", "'else'");
        DESCRIPCIONES.put("pr_while", "'while'");
        DESCRIPCIONES.put("pr_return", "'return'");
        DESCRIPCIONES.put("pr_var", "'var'");
        DESCRIPCIONES.put("pr_this", "'this'");
        DESCRIPCIONES.put("pr_new", "'new'");
        DESCRIPCIONES.put("pr_null", "'null'");
        DESCRIPCIONES.put("pr_true", "'true'");
        DESCRIPCIONES.put("pr_false", "'false'");

        DESCRIPCIONES.put("parA", "'('");
        DESCRIPCIONES.put("parC", "')'");
        DESCRIPCIONES.put("llaveA", "'{'");
        DESCRIPCIONES.put("llaveC", "'}'");
        DESCRIPCIONES.put("corcheteA", "'['");
        DESCRIPCIONES.put("corcheteC", "']'");
        DESCRIPCIONES.put("puntoComa", "';'");
        DESCRIPCIONES.put("coma", "','");
        DESCRIPCIONES.put("punto", "'.'");
        DESCRIPCIONES.put("dosPuntos", "':'");

        DESCRIPCIONES.put("op=", "'='");
        DESCRIPCIONES.put("op==", "'=='");
        DESCRIPCIONES.put("op!=", "'!='");
        DESCRIPCIONES.put("op<", "'<'");
        DESCRIPCIONES.put("op>", "'>'");
        DESCRIPCIONES.put("op<=", "'<='");
        DESCRIPCIONES.put("op>=", "'>='");
        DESCRIPCIONES.put("op+", "'+'");
        DESCRIPCIONES.put("op-", "'-'");
        DESCRIPCIONES.put("op*", "'*'");
        DESCRIPCIONES.put("op/", "'/'");
        DESCRIPCIONES.put("op%", "'%'");
        DESCRIPCIONES.put("op&&", "'&&'");
        DESCRIPCIONES.put("op||", "'||'");
        DESCRIPCIONES.put("op!", "'!'");
        DESCRIPCIONES.put("op++", "'++'");
        DESCRIPCIONES.put("op--", "'--'");
    }

    // Conjuntos de primeros usados para elegir producciones.
    private static final Set<String> PRIMEROS_TIPO_PRIMITIVO = Set.of(
            "pr_boolean", "pr_char", "pr_int");

    private static final Set<String> PRIMEROS_TIPO = Set.of(
            "pr_boolean", "pr_char", "pr_int", "idClase", "idGen");

    private static final Set<String> PRIMEROS_MIEMBRO = Set.of(
            "pr_boolean", "pr_char", "pr_int", "idClase", "idGen",
            "pr_void", "pr_static", "pr_public", "pr_private");

    private static final Set<String> PRIMEROS_METODO_INTERFAZ = Set.of(
            "pr_boolean", "pr_char", "pr_int", "idClase", "idGen", "pr_void",
            "pr_public", "pr_private");

    private static final Set<String> PRIMEROS_PRIMITIVO = Set.of(
            "pr_true", "pr_false", "litInt", "litChar", "pr_null");

    private static final Set<String> PRIMEROS_PRIMARIO = Set.of(
            "pr_this", "litString", "idMV", "pr_new", "idClase", "parA");

    private static final Set<String> PRIMEROS_OPERADOR_UNARIO = Set.of(
            "op+", "op-", "op!");

    private static final Set<String> PRIMEROS_OPERANDO = Set.of(
            "pr_true", "pr_false", "litInt", "litChar", "pr_null",
            "pr_this", "litString", "idMV", "pr_new", "idClase", "parA");

    private static final Set<String> PRIMEROS_EXPRESION = Set.of(
            "op+", "op-", "op!",
            "pr_true", "pr_false", "litInt", "litChar", "pr_null",
            "pr_this", "litString", "idMV", "pr_new", "idClase", "parA");

    private static final Set<String> PRIMEROS_SENTENCIA = Set.of(
            "puntoComa", "pr_var", "pr_return", "pr_if", "pr_while", "llaveA",
            "op+", "op-", "op!",
            "pr_true", "pr_false", "litInt", "litChar", "pr_null",
            "pr_this", "litString", "idMV", "pr_new", "idClase", "parA");

    private static final Set<String> PRIMEROS_OPERADOR_BINARIO = Set.of(
            "op||", "op&&", "op==", "op!=", "op<", "op>", "op<=", "op>=",
            "op+", "op-", "op*", "op/", "op%");

    private final AnalizadorLexico analizadorLexico;
    private Token tokenActual;

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico) {
        this.analizadorLexico = analizadorLexico;
    }

    /** Comienza el analisis sintactico desde el simbolo inicial. */
    public void analizar() throws IOException {
        tokenActual = analizadorLexico.proximoToken();
        inicial();
    }

    private void match(String nombreToken) throws IOException {
        if (nombreToken.equals(tokenActual.getNombre())) {
            tokenActual = analizadorLexico.proximoToken();
        } else {
            throw new ExcepcionSintactica(
                    tokenActual.getLexema(),
                    tokenActual.getNroLinea(),
                    "se esperaba " + descripcion(nombreToken)
                            + " se encontro \"" + tokenActual.getLexema() + "\"");
        }
    }

    private ExcepcionSintactica error(String esperado) {
        return new ExcepcionSintactica(
                tokenActual.getLexema(),
                tokenActual.getNroLinea(),
                "se encontro \"" + tokenActual.getLexema() + "\" cuando se esperaba " + esperado);
    }

    private static String descripcion(String nombreToken) {
        String descripcion = DESCRIPCIONES.get(nombreToken);
        return descripcion != null ? descripcion : nombreToken;
    }

    private boolean actualEn(Set<String> primeros) {
        return primeros.contains(tokenActual.getNombre());
    }

    // <Inicial> ::= <ListaClases> eof
    private void inicial() throws IOException {
        listaClases();
        match("EOF");
    }

    // <ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | epsilon
    private void listaClases() throws IOException {
        if (actualEn(Set.of("pr_class"))) {
            clase();
            listaClases();
        } else if (actualEn(Set.of("pr_interface"))) {
            interfaz();
            listaClases();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <Clase> ::= class idClase <GenericidadOpcional> <HerenciaOpcional> { <ListaMiembros> }
    private void clase() throws IOException {
        match("pr_class");
        match("idClase");
        genericidadOpcional();
        herenciaOpcional();
        match("llaveA");
        listaMiembros();
        match("llaveC");
    }

    // <Interfaz> ::= interface idClase <GenericidadOpcional> <ExtensionOpcional> { <ListaMetodosInterfaz> }
    private void interfaz() throws IOException {
        match("pr_interface");
        match("idClase");
        genericidadOpcional();
        extensionOpcional();
        match("llaveA");
        listaMetodosInterfaz();
        match("llaveC");
    }

    // <GenericidadOpcional> ::= < idGen > | epsilon
    private void genericidadOpcional() throws IOException {
        if (actualEn(Set.of("op<"))) {
            match("op<");
            match("idGen");
            match("op>");
        } else {
            // epsilon: no se hace nada
        }
    }

    // <HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | epsilon
    private void herenciaOpcional() throws IOException {
        if (actualEn(Set.of("pr_extends"))) {
            match("pr_extends");
            tipoReferencia();
        } else if (actualEn(Set.of("pr_implements"))) {
            match("pr_implements");
            tipoReferencia();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ExtensionOpcional> ::= extends <TipoReferencia> | epsilon
    private void extensionOpcional() throws IOException {
        if (actualEn(Set.of("pr_extends"))) {
            match("pr_extends");
            tipoReferencia();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ListaMiembros> ::= <Miembro> <ListaMiembros> | epsilon
    private void listaMiembros() throws IOException {
        if (actualEn(PRIMEROS_MIEMBRO)) {
            miembro();
            listaMiembros();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ListaMetodosInterfaz> ::= <MetodoInterfaz> <ListaMetodosInterfaz> | epsilon
    private void listaMetodosInterfaz() throws IOException {
        if (actualEn(PRIMEROS_METODO_INTERFAZ)) {
            metodoInterfaz();
            listaMetodosInterfaz();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <Miembro> ::= <VisibilidadOpcional> <MiembroSinVisibilidad>
    // Logro Visibilidad Mejorada E2: public/private opcionales para todo miembro.
    private void miembro() throws IOException {
        visibilidadOpcional();
        miembroSinVisibilidad();
    }

    // <VisibilidadOpcional> ::= public | private | epsilon
    private void visibilidadOpcional() throws IOException {
        if (actualEn(Set.of("pr_public"))) {
            match("pr_public");
        } else if (actualEn(Set.of("pr_private"))) {
            match("pr_private");
        } else {
            // epsilon: visibilidad implicita
        }
    }

    // <MiembroSinVisibilidad> ::= <MetodoVoid>
    //                           | static <MetodoResto>
    //                           | <TipoPrimitivo> <DimensionesOpcionales> idMetVar <MetodoOAtributo>
    //                           | idGen <DimensionesOpcionales> idMetVar <MetodoOAtributo>
    //                           | idClase <RestoTipoOConstructor>
    private void miembroSinVisibilidad() throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            metodoVoid();
        } else if (actualEn(Set.of("pr_static"))) {
            match("pr_static");
            metodoResto();
        } else if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            tipoPrimitivo();
            dimensionesOpcionales();
            match("idMV");
            metodoOAtributo();
        } else if (actualEn(Set.of("idGen"))) {
            match("idGen");
            dimensionesOpcionales();
            match("idMV");
            metodoOAtributo();
        } else if (actualEn(Set.of("idClase"))) {
            match("idClase");
            restoTipoOConstructor();
        } else {
            throw error("un miembro de la clase (atributo, metodo o constructor)");
        }
    }

    // <RestoTipoOConstructor> ::= <TipoGenericoOpcional> <TipoFinalOConstructor>
    // Factorizacion profunda: tras idClase puede seguir un tipo (atributo/metodo)
    // o directamente los argumentos formales (constructor sin visibilidad).
    private void restoTipoOConstructor() throws IOException {
        tipoGenericoOpcional();
        tipoFinalOConstructor();
    }

    // <TipoFinalOConstructor> ::= <DimensionesOpcionales> idMetVar <MetodoOAtributo>
    //                           | <ArgsFormales> <Bloque>
    private void tipoFinalOConstructor() throws IOException {
        if (actualEn(Set.of("corcheteA", "idMV"))) {
            dimensionesOpcionales();
            match("idMV");
            metodoOAtributo();
        } else if (actualEn(Set.of("parA"))) {
            argsFormales();
            bloque();
        } else {
            throw error("un atributo, un metodo o un constructor");
        }
    }

    // <MetodoOAtributo> ::= ; | = <ExpresionCompuesta> ; | <ArgsFormales> <Bloque>
    // Logro Atributos Inicializados: el atributo puede inicializarse al declararse.
    private void metodoOAtributo() throws IOException {
        if (actualEn(Set.of("puntoComa"))) {
            match("puntoComa");
        } else if (actualEn(Set.of("op="))) {
            match("op=");
            expresionCompuesta();
            match("puntoComa");
        } else if (actualEn(Set.of("parA"))) {
            argsFormales();
            bloque();
        } else {
            throw error("';', una inicializacion o los argumentos formales de un metodo");
        }
    }

    // <MetodoVoid> ::= void idMetVar <ArgsFormales> <Bloque>
    private void metodoVoid() throws IOException {
        match("pr_void");
        match("idMV");
        argsFormales();
        bloque();
    }

    // <MetodoResto> ::= <MetodoVoid> | <Tipo> idMetVar <ArgsFormales> <Bloque>
    private void metodoResto() throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            metodoVoid();
        } else if (actualEn(PRIMEROS_TIPO)) {
            tipo();
            match("idMV");
            argsFormales();
            bloque();
        } else {
            throw error("un tipo de retorno o 'void'");
        }
    }

    // <MetodoInterfaz> ::= <VisibilidadOpcional> <TipoMetodo> idMetVar <ArgsFormales> ;
    private void metodoInterfaz() throws IOException {
        visibilidadOpcional();
        tipoMetodo();
        match("idMV");
        argsFormales();
        match("puntoComa");
    }

    // <Constructor> (se maneja via <MiembroSinVisibilidad> tras idClase)

    // <TipoMetodo> ::= <Tipo> | void
    private void tipoMetodo() throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            match("pr_void");
        } else if (actualEn(PRIMEROS_TIPO)) {
            tipo();
        } else {
            throw error("un tipo de retorno o 'void'");
        }
    }

    // <Tipo> ::= <TipoBase> <DimensionesOpcionales>
    private void tipo() throws IOException {
        tipoBase();
        dimensionesOpcionales();
    }

    // <TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen
    private void tipoBase() throws IOException {
        if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            tipoPrimitivo();
        } else if (actualEn(Set.of("idClase"))) {
            tipoReferencia();
        } else if (actualEn(Set.of("idGen"))) {
            match("idGen");
        } else {
            throw error("un tipo (primitivo, clase o parametro generico)");
        }
    }

    // <DimensionesOpcionales> ::= [] <DimensionesOpcionales> | epsilon
    private void dimensionesOpcionales() throws IOException {
        if (actualEn(Set.of("corcheteA"))) {
            match("corcheteA");
            match("corcheteC");
            dimensionesOpcionales();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <TipoReferencia> ::= idClase <TipoGenericoOpcional>
    private void tipoReferencia() throws IOException {
        match("idClase");
        tipoGenericoOpcional();
    }

    // <TipoPrimitivo> ::= boolean | char | int
    private void tipoPrimitivo() throws IOException {
        if (actualEn(Set.of("pr_boolean"))) {
            match("pr_boolean");
        } else if (actualEn(Set.of("pr_char"))) {
            match("pr_char");
        } else if (actualEn(Set.of("pr_int"))) {
            match("pr_int");
        } else {
            throw error("un tipo primitivo ('boolean', 'char' o 'int')");
        }
    }

    // <TipoGenericoOpcional> ::= < <InstanciadoOParametrico> > | epsilon
    private void tipoGenericoOpcional() throws IOException {
        if (actualEn(Set.of("op<"))) {
            match("op<");
            instanciadoOParametrico();
            match("op>");
        } else {
            // epsilon: no se hace nada
        }
    }

    // <InstanciadoOParametrico> ::= idGen | idClase
    private void instanciadoOParametrico() throws IOException {
        if (actualEn(Set.of("idGen"))) {
            match("idGen");
        } else if (actualEn(Set.of("idClase"))) {
            match("idClase");
        } else {
            throw error("un id de clase o un parametro de tipo generico");
        }
    }

    // <ArgsFormales> ::= ( <ListaArgsFormalesOpcional> )
    private void argsFormales() throws IOException {
        match("parA");
        listaArgsFormalesOpcional();
        match("parC");
    }

    // <ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | epsilon
    private void listaArgsFormalesOpcional() throws IOException {
        if (actualEn(PRIMEROS_TIPO)) {
            listaArgsFormales();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ListaArgsFormales> ::= <ArgFormal> <LAFResto>
    private void listaArgsFormales() throws IOException {
        argFormal();
        lAFResto();
    }

    // <LAFResto> ::= , <ArgFormal> <LAFResto> | epsilon
    private void lAFResto() throws IOException {
        if (actualEn(Set.of("coma"))) {
            match("coma");
            argFormal();
            lAFResto();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ArgFormal> ::= <Tipo> idMetVar
    private void argFormal() throws IOException {
        tipo();
        match("idMV");
    }

    // <Bloque> ::= { <ListaSentencias> }
    private void bloque() throws IOException {
        match("llaveA");
        listaSentencias();
        match("llaveC");
    }

    // <ListaSentencias> ::= <Sentencia> <ListaSentencias> | epsilon
    private void listaSentencias() throws IOException {
        if (actualEn(PRIMEROS_SENTENCIA)) {
            sentencia();
            listaSentencias();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <Sentencia> ::= ;
    //               | <Expresion> ;
    //               | <VarLocal> ;
    //               | <Return> ;
    //               | <If>
    //               | <While>
    //               | <Bloque>
    private void sentencia() throws IOException {
        if (actualEn(Set.of("puntoComa"))) {
            match("puntoComa");
        } else if (actualEn(Set.of("pr_var"))) {
            varLocal();
            match("puntoComa");
        } else if (actualEn(Set.of("pr_return"))) {
            retorno();
            match("puntoComa");
        } else if (actualEn(Set.of("pr_if"))) {
            sentenciaIf();
        } else if (actualEn(Set.of("pr_while"))) {
            sentenciaWhile();
        } else if (actualEn(Set.of("llaveA"))) {
            bloque();
        } else if (actualEn(PRIMEROS_EXPRESION)) {
            expresion();
            match("puntoComa");
        } else {
            throw error("una sentencia");
        }
    }

    // <VarLocal> ::= var idMetVar = <ExpresionCompuesta>
    private void varLocal() throws IOException {
        match("pr_var");
        match("idMV");
        match("op=");
        expresionCompuesta();
    }

    // <Return> ::= return <ExpresionOpcional>
    private void retorno() throws IOException {
        match("pr_return");
        expresionOpcional();
    }

    // <ExpresionOpcional> ::= <Expresion> | epsilon
    private void expresionOpcional() throws IOException {
        if (actualEn(PRIMEROS_EXPRESION)) {
            expresion();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <If> ::= if ( <Expresion> ) <Sentencia> <ElseOpcional>
    private void sentenciaIf() throws IOException {
        match("pr_if");
        match("parA");
        expresion();
        match("parC");
        sentencia();
        elseOpcional();
    }

    // <ElseOpcional> ::= else <Sentencia> | epsilon
    private void elseOpcional() throws IOException {
        if (actualEn(Set.of("pr_else"))) {
            match("pr_else");
            sentencia();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <While> ::= while ( <Expresion> ) <Sentencia>
    private void sentenciaWhile() throws IOException {
        match("pr_while");
        match("parA");
        expresion();
        match("parC");
        sentencia();
    }

    // <Expresion> ::= <ExpresionCompuesta> <ExpresionResto>
    private void expresion() throws IOException {
        expresionCompuesta();
        expresionResto();
    }

    // <ExpresionResto> ::= <OperadorAsignacion> <ExpresionCompuesta> | epsilon
    private void expresionResto() throws IOException {
        if (actualEn(Set.of("op="))) {
            operadorAsignacion();
            expresionCompuesta();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <OperadorAsignacion> ::= =
    private void operadorAsignacion() throws IOException {
        match("op=");
    }

    // <ExpresionCompuesta> ::= <ExpresionBasica> <ECResto>
    private void expresionCompuesta() throws IOException {
        expresionBasica();
        eCResto();
    }

    // <ECResto> ::= <OperadorBinario> <ExpresionBasica> <ECResto> | epsilon
    private void eCResto() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_BINARIO)) {
            operadorBinario();
            expresionBasica();
            eCResto();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <OperadorBinario> ::= || | && | == | != | < | > | <= | >= | + | - | * | / | %
    private void operadorBinario() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_BINARIO)) {
            match(tokenActual.getNombre());
        } else {
            throw error("un operador binario");
        }
    }

    // <ExpresionBasica> ::= <OperadorUnario> <Operando> <PosfijoOpcional>
    //                     | <Operando> <PosfijoOpcional>
    // Logro Operadores Posfijos E2: el posfijo va solo al final de la
    // expresion basica completa (referencia incluida), para mantener LL(1).
    private void expresionBasica() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_UNARIO)) {
            operadorUnario();
            operando();
        } else if (actualEn(PRIMEROS_OPERANDO)) {
            operando();
        } else {
            throw error("una expresion");
        }
        posfijoOpcional();
    }

    // <PosfijoOpcional> ::= ++ | -- | epsilon
    private void posfijoOpcional() throws IOException {
        if (actualEn(Set.of("op++", "op--"))) {
            match(tokenActual.getNombre());
        } else {
            // epsilon: no se hace nada
        }
    }

    // <OperadorUnario> ::= + | - | !
    private void operadorUnario() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_UNARIO)) {
            match(tokenActual.getNombre());
        } else {
            throw error("un operador unario ('+', '-' o '!')");
        }
    }

    // <Operando> ::= <Primitivo> | <Referencia>
    private void operando() throws IOException {
        if (actualEn(PRIMEROS_PRIMITIVO)) {
            primitivo();
        } else if (actualEn(PRIMEROS_PRIMARIO)) {
            referencia();
        } else {
            throw error("un operando (literal o referencia)");
        }
    }

    // <Primitivo> ::= true | false | intLiteral | charLiteral | null
    private void primitivo() throws IOException {
        if (actualEn(PRIMEROS_PRIMITIVO)) {
            match(tokenActual.getNombre());
        } else {
            throw error("un literal primitivo");
        }
    }

    // <Referencia> ::= <Primario> <RResto>
    private void referencia() throws IOException {
        primario();
        rResto();
    }

    // <RResto> ::= . idMetVar <MetodoOVar> <RResto>
    //            | <AccesoArreglo> <RResto>
    //            | epsilon
    private void rResto() throws IOException {
        if (actualEn(Set.of("punto"))) {
            match("punto");
            match("idMV");
            metodoOVar();
            rResto();
        } else if (actualEn(Set.of("corcheteA"))) {
            accesoArreglo();
            rResto();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <Primario> ::= this
    //              | stringLiteral
    //              | idMetVar <MetodoOVar>
    //              | new <ArregloOConstructor>
    //              | <LlamadaMetodoEstatico>
    //              | <ExpresionParentizada>
    private void primario() throws IOException {
        if (actualEn(Set.of("pr_this"))) {
            match("pr_this");
        } else if (actualEn(Set.of("litString"))) {
            match("litString");
        } else if (actualEn(Set.of("idMV"))) {
            match("idMV");
            metodoOVar();
        } else if (actualEn(Set.of("pr_new"))) {
            match("pr_new");
            arregloOConstructor();
        } else if (actualEn(Set.of("idClase"))) {
            llamadaMetodoEstatico();
        } else if (actualEn(Set.of("parA"))) {
            expresionParentizada();
        } else {
            throw error("una referencia ('this', string, variable, llamada, 'new' o expresion parentizada)");
        }
    }

    // <ArregloOConstructor> ::= <TipoReferencia> <AOC2>
    //                         | <TipoPrimitivo> <DimensionesConTamanio>
    //                         | idGen <DimensionesConTamanio>
    private void arregloOConstructor() throws IOException {
        if (actualEn(Set.of("idClase"))) {
            tipoReferencia();
            aOC2();
        } else if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            tipoPrimitivo();
            dimensionesConTamanio();
        } else if (actualEn(Set.of("idGen"))) {
            match("idGen");
            dimensionesConTamanio();
        } else {
            throw error("un tipo de arreglo o constructor");
        }
    }

    // <AOC2> ::= <ArgsActuales> | <DimensionesConTamanio>
    private void aOC2() throws IOException {
        if (actualEn(Set.of("parA"))) {
            argsActuales();
        } else if (actualEn(Set.of("corcheteA"))) {
            dimensionesConTamanio();
        } else {
            throw error("los argumentos de un constructor o las dimensiones de un arreglo");
        }
    }

    // <MetodoOVar> ::= <ArgsActuales> | epsilon
    private void metodoOVar() throws IOException {
        if (actualEn(Set.of("parA"))) {
            argsActuales();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ExpresionParentizada> ::= ( <Expresion> )
    private void expresionParentizada() throws IOException {
        match("parA");
        expresion();
        match("parC");
    }

    // <LlamadaMetodoEstatico> ::= idClase . idMetVar <ArgsActuales>
    private void llamadaMetodoEstatico() throws IOException {
        match("idClase");
        match("punto");
        match("idMV");
        argsActuales();
    }

    // <DimensionesConTamanio> ::= [ <Expresion> ] <DCTResto>
    private void dimensionesConTamanio() throws IOException {
        match("corcheteA");
        expresion();
        match("corcheteC");
        dCTResto();
    }

    // <DCTResto> ::= [ <Expresion> ] <DCTResto> | epsilon
    private void dCTResto() throws IOException {
        if (actualEn(Set.of("corcheteA"))) {
            match("corcheteA");
            expresion();
            match("corcheteC");
            dCTResto();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ArgsActuales> ::= ( <ListaExpsOpcional> )
    private void argsActuales() throws IOException {
        match("parA");
        listaExpsOpcional();
        match("parC");
    }

    // <ListaExpsOpcional> ::= <ListaExps> | epsilon
    private void listaExpsOpcional() throws IOException {
        if (actualEn(PRIMEROS_EXPRESION)) {
            listaExps();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <ListaExps> ::= <Expresion> <ListaExpsResto>
    private void listaExps() throws IOException {
        expresion();
        listaExpsResto();
    }

    // <ListaExpsResto> ::= , <ListaExps> | epsilon
    private void listaExpsResto() throws IOException {
        if (actualEn(Set.of("coma"))) {
            match("coma");
            listaExps();
        } else {
            // epsilon: no se hace nada
        }
    }

    // <AccesoArreglo> ::= [ <Expresion> ]
    private void accesoArreglo() throws IOException {
        match("corcheteA");
        expresion();
        match("corcheteC");
    }
}
