package analizadorsintactico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analizadorlexico.AnalizadorLexico;
import analizadorlexico.Token;
import analizadorsemantico.Atributo;
import analizadorsemantico.Clase;
import analizadorsemantico.Constructor;
import analizadorsemantico.Interfaz;
import analizadorsemantico.Metodo;
import analizadorsemantico.Parametro;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;
import analizadorsemantico.TipoParametro;
import analizadorsemantico.TipoPrimitivo;
import analizadorsemantico.TipoReferencia;
import analizadorsemantico.TipoVoid;

/**
 * Analizador Sintactico Descendente Recursivo para MiniJava.
 *
 * Implementa la estrategia recursiva simple: un metodo por cada no terminal
 * de la gramatica LL(1), eligiendo la produccion a aplicar segun los
 * primeros de sus partes derechas. Los terminales se consumen con match()
 * y los errores se reportan lanzando ExcepcionSintactica.
 *
 * Ademas, construye la Tabla de Simbolos a medida que reconoce las
 * declaraciones (clases, interfaces, atributos, metodos, constructores y
 * parametros), realizando los controles de nombres repetidos de la primera
 * pasada.
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
    private final TablaDeSimbolos tablaDeSimbolos;
    private Token tokenActual;

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico, TablaDeSimbolos tablaDeSimbolos) {
        this.analizadorLexico = analizadorLexico;
        this.tablaDeSimbolos = tablaDeSimbolos;
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

    private Token consumir(String nombreToken) throws IOException {
        Token consumido = tokenActual;
        match(nombreToken);
        return consumido;
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
        Token tokenNombre = consumir("idClase");
        Clase clase = new Clase(tokenNombre, tablaDeSimbolos);
        tablaDeSimbolos.insertar(clase);
        tablaDeSimbolos.setClaseActual(clase);

        String parametroGenerico = genericidadOpcional();
        if (parametroGenerico != null) {
            clase.setParametroGenerico(parametroGenerico);
        }
        herenciaOpcional(clase);

        match("llaveA");
        listaMiembros();
        match("llaveC");

        clase.agregarConstructorPorDefecto();
        tablaDeSimbolos.setMetodoActual(null);
        tablaDeSimbolos.setClaseActual(null);
    }

    // <Interfaz> ::= interface idClase <GenericidadOpcional> <ExtensionOpcional> { <ListaMetodosInterfaz> }
    private void interfaz() throws IOException {
        match("pr_interface");
        Token tokenNombre = consumir("idClase");
        Interfaz interfaz = new Interfaz(tokenNombre, tablaDeSimbolos);
        tablaDeSimbolos.insertar(interfaz);
        tablaDeSimbolos.setInterfazActual(interfaz);

        String parametroGenerico = genericidadOpcional();
        if (parametroGenerico != null) {
            interfaz.setParametroGenerico(parametroGenerico);
        }
        extensionOpcional(interfaz);

        match("llaveA");
        listaMetodosInterfaz();
        match("llaveC");

        tablaDeSimbolos.setMetodoActual(null);
        tablaDeSimbolos.setInterfazActual(null);
    }

    // <GenericidadOpcional> ::= < idGen > | epsilon
    private String genericidadOpcional() throws IOException {
        if (actualEn(Set.of("op<"))) {
            match("op<");
            Token tokenParametro = consumir("idGen");
            match("op>");
            return tokenParametro.getLexema();
        }
        return null;
    }

    // <HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | epsilon
    private void herenciaOpcional(Clase clase) throws IOException {
        if (actualEn(Set.of("pr_extends"))) {
            match("pr_extends");
            clase.setAncestro(tipoReferencia(), false);
        } else if (actualEn(Set.of("pr_implements"))) {
            match("pr_implements");
            clase.setAncestro(tipoReferencia(), true);
        } else {
            // epsilon: extiende Object implicitamente
        }
    }

    // <ExtensionOpcional> ::= extends <TipoReferencia> | epsilon
    private void extensionOpcional(Interfaz interfaz) throws IOException {
        if (actualEn(Set.of("pr_extends"))) {
            match("pr_extends");
            interfaz.setAncestro(tipoReferencia());
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
            metodoVoid(false);
        } else if (actualEn(Set.of("pr_static"))) {
            match("pr_static");
            metodoResto(true);
        } else if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            Token tokenTipo = tokenActual;
            TipoPrimitivo tipoBase = tipoPrimitivo();
            int dimensiones = dimensionesOpcionales();
            Token tokenNombre = consumir("idMV");
            metodoOAtributo(conDimensiones(tipoBase, dimensiones, tokenTipo), tokenNombre);
        } else if (actualEn(Set.of("idGen"))) {
            Token tokenTipo = consumir("idGen");
            TipoParametro tipoBase = new TipoParametro(tokenTipo.getLexema(), tokenTipo);
            int dimensiones = dimensionesOpcionales();
            Token tokenNombre = consumir("idMV");
            metodoOAtributo(conDimensiones(tipoBase, dimensiones, tokenTipo), tokenNombre);
        } else if (actualEn(Set.of("idClase"))) {
            Token tokenIdClase = consumir("idClase");
            Tipo argumento = tipoGenericoOpcional();
            if (actualEn(Set.of("parA"))) {
                List<Parametro> parametros = argsFormales();
                bloque();
                tablaDeSimbolos.getClaseActual().agregarConstructor(
                        new Constructor(tokenIdClase, parametros, tablaDeSimbolos));
            } else {
                TipoReferencia tipoBase = new TipoReferencia(tokenIdClase.getLexema(), argumento, tokenIdClase);
                int dimensiones = dimensionesOpcionales();
                Token tokenNombre = consumir("idMV");
                metodoOAtributo(conDimensiones(tipoBase, dimensiones, tokenIdClase), tokenNombre);
            }
        } else {
            throw error("un miembro de la clase (atributo, metodo o constructor)");
        }
    }

    // <MetodoOAtributo> ::= ; | = <ExpresionCompuesta> ; | <ArgsFormales> <Bloque>
    private void metodoOAtributo(Tipo tipo, Token tokenNombre) throws IOException {
        if (actualEn(Set.of("puntoComa"))) {
            match("puntoComa");
            tablaDeSimbolos.getClaseActual().agregarAtributo(
                    new Atributo(tokenNombre, tipo, tablaDeSimbolos));
        } else if (actualEn(Set.of("op="))) {
            match("op=");
            expresionCompuesta();
            match("puntoComa");
            tablaDeSimbolos.getClaseActual().agregarAtributo(
                    new Atributo(tokenNombre, tipo, tablaDeSimbolos));
        } else if (actualEn(Set.of("parA"))) {
            List<Parametro> parametros = argsFormales();
            bloque();
            agregarMetodo(new Metodo(tokenNombre, tipo, parametros, false, tablaDeSimbolos));
        } else {
            throw error("';', una inicializacion o los argumentos formales de un metodo");
        }
    }

    // <MetodoVoid> ::= void idMetVar <ArgsFormales> <Bloque>
    private void metodoVoid(boolean esEstatico) throws IOException {
        Token tokenVoid = consumir("pr_void");
        TipoVoid tipoRetorno = new TipoVoid(tokenVoid);
        Token tokenNombre = consumir("idMV");
        List<Parametro> parametros = argsFormales();
        bloque();
        agregarMetodo(new Metodo(tokenNombre, tipoRetorno, parametros, esEstatico, tablaDeSimbolos));
    }

    // <MetodoResto> ::= <MetodoVoid> | <Tipo> idMetVar <ArgsFormales> <Bloque>
    private void metodoResto(boolean esEstatico) throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            metodoVoid(esEstatico);
        } else if (actualEn(PRIMEROS_TIPO)) {
            Tipo tipoRetorno = tipo();
            Token tokenNombre = consumir("idMV");
            List<Parametro> parametros = argsFormales();
            bloque();
            agregarMetodo(new Metodo(tokenNombre, tipoRetorno, parametros, esEstatico, tablaDeSimbolos));
        } else {
            throw error("un tipo de retorno o 'void'");
        }
    }

    // <MetodoInterfaz> ::= <VisibilidadOpcional> <TipoMetodo> idMetVar <ArgsFormales> ;
    private void metodoInterfaz() throws IOException {
        visibilidadOpcional();
        Tipo tipoRetorno = tipoMetodo();
        Token tokenNombre = consumir("idMV");
        List<Parametro> parametros = argsFormales();
        match("puntoComa");
        tablaDeSimbolos.getInterfazActual().agregarMetodo(
                new Metodo(tokenNombre, tipoRetorno, parametros, false, tablaDeSimbolos));
    }

    private void agregarMetodo(Metodo metodo) {
        if (tablaDeSimbolos.getClaseActual() != null) {
            tablaDeSimbolos.getClaseActual().agregarMetodo(metodo);
        } else {
            tablaDeSimbolos.getInterfazActual().agregarMetodo(metodo);
        }
        tablaDeSimbolos.setMetodoActual(metodo);
    }

    // <TipoMetodo> ::= <Tipo> | void
    private Tipo tipoMetodo() throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            Token tokenVoid = consumir("pr_void");
            return new TipoVoid(tokenVoid);
        } else if (actualEn(PRIMEROS_TIPO)) {
            return tipo();
        } else {
            throw error("un tipo de retorno o 'void'");
        }
    }

    // <Tipo> ::= <TipoBase> <DimensionesOpcionales>
    private Tipo tipo() throws IOException {
        Token tokenBase = tokenActual;
        Tipo tipoBase = tipoBase();
        int dimensiones = dimensionesOpcionales();
        return conDimensiones(tipoBase, dimensiones, tokenBase);
    }

    private static Tipo conDimensiones(Tipo tipoBase, int dimensiones, Token token) {
        return dimensiones == 0 ? tipoBase : new TipoArreglo(tipoBase, dimensiones, token);
    }

    // <TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen
    private Tipo tipoBase() throws IOException {
        if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            return tipoPrimitivo();
        } else if (actualEn(Set.of("idClase"))) {
            return tipoReferencia();
        } else if (actualEn(Set.of("idGen"))) {
            Token tokenParametro = consumir("idGen");
            return new TipoParametro(tokenParametro.getLexema(), tokenParametro);
        } else {
            throw error("un tipo (primitivo, clase o parametro generico)");
        }
    }

    // <DimensionesOpcionales> ::= [] <DimensionesOpcionales> | epsilon
    private int dimensionesOpcionales() throws IOException {
        if (actualEn(Set.of("corcheteA"))) {
            match("corcheteA");
            match("corcheteC");
            return 1 + dimensionesOpcionales();
        }
        return 0;
    }

    // <TipoReferencia> ::= idClase <TipoGenericoOpcional>
    private TipoReferencia tipoReferencia() throws IOException {
        Token tokenIdClase = consumir("idClase");
        Tipo argumento = tipoGenericoOpcional();
        return new TipoReferencia(tokenIdClase.getLexema(), argumento, tokenIdClase);
    }

    // <TipoPrimitivo> ::= boolean | char | int
    private TipoPrimitivo tipoPrimitivo() throws IOException {
        Token tokenTipo = tokenActual;
        if (actualEn(Set.of("pr_boolean"))) {
            match("pr_boolean");
            return new TipoPrimitivo(TipoPrimitivo.Base.BOOLEAN, tokenTipo);
        } else if (actualEn(Set.of("pr_char"))) {
            match("pr_char");
            return new TipoPrimitivo(TipoPrimitivo.Base.CHAR, tokenTipo);
        } else if (actualEn(Set.of("pr_int"))) {
            match("pr_int");
            return new TipoPrimitivo(TipoPrimitivo.Base.INT, tokenTipo);
        } else {
            throw error("un tipo primitivo ('boolean', 'char' o 'int')");
        }
    }

    // <TipoGenericoOpcional> ::= < <InstanciadoOParametrico> > | epsilon
    private Tipo tipoGenericoOpcional() throws IOException {
        if (actualEn(Set.of("op<"))) {
            match("op<");
            Tipo argumento = instanciadoOParametrico();
            match("op>");
            return argumento;
        }
        return null;
    }

    // <InstanciadoOParametrico> ::= idGen | idClase
    private Tipo instanciadoOParametrico() throws IOException {
        if (actualEn(Set.of("idGen"))) {
            Token tokenParametro = consumir("idGen");
            return new TipoParametro(tokenParametro.getLexema(), tokenParametro);
        } else if (actualEn(Set.of("idClase"))) {
            Token tokenIdClase = consumir("idClase");
            return new TipoReferencia(tokenIdClase.getLexema(), null, tokenIdClase);
        } else {
            throw error("un id de clase o un parametro de tipo generico");
        }
    }

    // <ArgsFormales> ::= ( <ListaArgsFormalesOpcional> )
    private List<Parametro> argsFormales() throws IOException {
        match("parA");
        List<Parametro> parametros = listaArgsFormalesOpcional();
        match("parC");
        return parametros;
    }

    // <ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | epsilon
    private List<Parametro> listaArgsFormalesOpcional() throws IOException {
        List<Parametro> parametros = new ArrayList<>();
        if (!actualEn(PRIMEROS_TIPO)) {
            return parametros;
        }
        int posicion = 0;
        Tipo tipo = tipo();
        Token tokenNombre = consumir("idMV");
        parametros.add(new Parametro(tokenNombre, tipo, posicion++, tablaDeSimbolos));
        while (actualEn(Set.of("coma"))) {
            match("coma");
            tipo = tipo();
            tokenNombre = consumir("idMV");
            parametros.add(new Parametro(tokenNombre, tipo, posicion++, tablaDeSimbolos));
        }
        return parametros;
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
