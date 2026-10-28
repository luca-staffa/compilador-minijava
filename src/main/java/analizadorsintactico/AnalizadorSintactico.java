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
import analizadorsemantico.Entidad;
import analizadorsemantico.ExcepcionSemantica;
import analizadorsemantico.Interfaz;
import analizadorsemantico.Metodo;
import analizadorsemantico.Parametro;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;
import analizadorsemantico.TipoParametro;
import analizadorsemantico.TipoPrimitivo;
import analizadorsemantico.TipoReferencia;
import analizadorsemantico.TipoNull;
import analizadorsemantico.TipoVoid;
import analizadorsemantico.ast.NodoBloque;
import analizadorsemantico.ast.NodoEncadenado;
import analizadorsemantico.ast.NodoEncadenadoArreglo;
import analizadorsemantico.ast.NodoEncadenadoAtributo;
import analizadorsemantico.ast.NodoEncadenadoLlamada;
import analizadorsemantico.ast.NodoExpresion;
import analizadorsemantico.ast.NodoExpresionAsignacion;
import analizadorsemantico.ast.NodoExpresionBinaria;
import analizadorsemantico.ast.NodoExpresionParentizada;
import analizadorsemantico.ast.NodoExpresionPosfijo;
import analizadorsemantico.ast.NodoExpresionUnaria;
import analizadorsemantico.ast.NodoIf;
import analizadorsemantico.ast.NodoLlamada;
import analizadorsemantico.ast.NodoLlamadaEstatica;
import analizadorsemantico.ast.NodoLiteral;
import analizadorsemantico.ast.NodoNewArreglo;
import analizadorsemantico.ast.NodoNewObjeto;
import analizadorsemantico.ast.NodoReferencia;
import analizadorsemantico.ast.NodoReturn;
import analizadorsemantico.ast.NodoSentencia;
import analizadorsemantico.ast.NodoSentenciaExpresion;
import analizadorsemantico.ast.NodoSentenciaVacia;
import analizadorsemantico.ast.NodoThis;
import analizadorsemantico.ast.NodoVar;
import analizadorsemantico.ast.NodoVarLocal;
import analizadorsemantico.ast.NodoWhile;

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
                NodoBloque cuerpo = bloque();
                Constructor constructor =
                        new Constructor(tokenIdClase, parametros, tablaDeSimbolos);
                constructor.setCuerpo(cuerpo);
                tablaDeSimbolos.getClaseActual().agregarConstructor(constructor);
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
            NodoBloque cuerpo = bloque();
            Metodo metodo = new Metodo(tokenNombre, tipo, parametros, false, tablaDeSimbolos);
            metodo.setCuerpo(cuerpo);
            agregarMetodo(metodo);
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
        NodoBloque cuerpo = bloque();
        Metodo metodo = new Metodo(tokenNombre, tipoRetorno, parametros, esEstatico, tablaDeSimbolos);
        metodo.setCuerpo(cuerpo);
        agregarMetodo(metodo);
    }

    // <MetodoResto> ::= <MetodoVoid> | <Tipo> idMetVar <ArgsFormales> <Bloque>
    private void metodoResto(boolean esEstatico) throws IOException {
        if (actualEn(Set.of("pr_void"))) {
            metodoVoid(esEstatico);
        } else if (actualEn(PRIMEROS_TIPO)) {
            Tipo tipoRetorno = tipo();
            Token tokenNombre = consumir("idMV");
            List<Parametro> parametros = argsFormales();
            NodoBloque cuerpo = bloque();
            Metodo metodo = new Metodo(tokenNombre, tipoRetorno, parametros, esEstatico, tablaDeSimbolos);
            metodo.setCuerpo(cuerpo);
            agregarMetodo(metodo);
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

    // <TipoReferenciaInstanciacion> ::= idClase <InstanciacionOpcional>
    // La notacion diamante (<>) solo se admite al instanciar una clase generica.
    private TipoReferencia tipoReferenciaNueva(Token tokenIdClase) throws IOException {
        if (!actualEn(Set.of("op<"))) {
            return new TipoReferencia(tokenIdClase.getLexema(), null, tokenIdClase);
        }
        match("op<");
        if (actualEn(Set.of("op>"))) {
            match("op>");
            Entidad entidad = tablaDeSimbolos.getTipo(tokenIdClase.getLexema());
            if (entidad == null || entidad.getParametroGenerico() == null) {
                throw new ExcepcionSemantica(
                        tokenIdClase.getLexema(),
                        tokenIdClase.getNroLinea(),
                        "el operador diamante <> solo puede usarse al instanciar una clase generica, y "
                                + tokenIdClase.getLexema() + " no es generica");
            }
            Tipo argumento = new TipoParametro(entidad.getParametroGenerico(), tokenIdClase);
            TipoReferencia referencia =
                    new TipoReferencia(tokenIdClase.getLexema(), argumento, tokenIdClase);
            referencia.marcarDiamante();
            return referencia;
        }
        Tipo argumento = instanciadoOParametrico();
        match("op>");
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

    // <InstanciadoOParametrico> ::= idGen | idClase <TipoGenericoOpcional>
    private Tipo instanciadoOParametrico() throws IOException {
        if (actualEn(Set.of("idGen"))) {
            Token tokenParametro = consumir("idGen");
            return new TipoParametro(tokenParametro.getLexema(), tokenParametro);
        } else if (actualEn(Set.of("idClase"))) {
            Token tokenIdClase = consumir("idClase");
            Tipo argumento = tipoGenericoOpcional();
            return new TipoReferencia(tokenIdClase.getLexema(), argumento, tokenIdClase);
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
    private NodoBloque bloque() throws IOException {
        Token tokenLlave = consumir("llaveA");
        List<NodoSentencia> sentencias = listaSentencias();
        match("llaveC");
        return new NodoBloque(tokenLlave, sentencias);
    }

    // <ListaSentencias> ::= <Sentencia> <ListaSentencias> | epsilon
    private List<NodoSentencia> listaSentencias() throws IOException {
        List<NodoSentencia> sentencias = new ArrayList<>();
        while (actualEn(PRIMEROS_SENTENCIA)) {
            sentencias.add(sentencia());
        }
        return sentencias;
    }

    // <Sentencia> ::= ;
    //               | <Expresion> ;
    //               | <VarLocal> ;
    //               | <Return> ;
    //               | <If>
    //               | <While>
    //               | <Bloque>
    private NodoSentencia sentencia() throws IOException {
        if (actualEn(Set.of("puntoComa"))) {
            Token tokenPuntoComa = consumir("puntoComa");
            return new NodoSentenciaVacia(tokenPuntoComa);
        } else if (actualEn(Set.of("pr_var"))) {
            NodoSentencia sentencia = varLocal();
            match("puntoComa");
            return sentencia;
        } else if (actualEn(Set.of("pr_return"))) {
            NodoSentencia sentencia = retorno();
            match("puntoComa");
            return sentencia;
        } else if (actualEn(Set.of("pr_if"))) {
            return sentenciaIf();
        } else if (actualEn(Set.of("pr_while"))) {
            return sentenciaWhile();
        } else if (actualEn(Set.of("llaveA"))) {
            return bloque();
        } else if (actualEn(PRIMEROS_EXPRESION)) {
            NodoExpresion expresion = expresion();
            match("puntoComa");
            return new NodoSentenciaExpresion(expresion);
        } else {
            throw error("una sentencia");
        }
    }

    // <VarLocal> ::= var idMetVar = <ExpresionCompuesta>
    private NodoVarLocal varLocal() throws IOException {
        match("pr_var");
        Token tokenNombre = consumir("idMV");
        match("op=");
        NodoExpresion inicializador = expresionCompuesta();
        return new NodoVarLocal(tokenNombre, inicializador);
    }

    // <Return> ::= return <ExpresionOpcional>
    private NodoReturn retorno() throws IOException {
        Token tokenReturn = consumir("pr_return");
        NodoExpresion expresion = expresionOpcional();
        return new NodoReturn(tokenReturn, expresion);
    }

    // <ExpresionOpcional> ::= <Expresion> | epsilon
    private NodoExpresion expresionOpcional() throws IOException {
        if (actualEn(PRIMEROS_EXPRESION)) {
            return expresion();
        }
        return null;
    }

    // <If> ::= if ( <Expresion> ) <Sentencia> <ElseOpcional>
    private NodoIf sentenciaIf() throws IOException {
        Token tokenIf = consumir("pr_if");
        match("parA");
        NodoExpresion condicion = expresion();
        match("parC");
        NodoSentencia entonces = sentencia();
        NodoSentencia sino = elseOpcional();
        return new NodoIf(tokenIf, condicion, entonces, sino);
    }

    // <ElseOpcional> ::= else <Sentencia> | epsilon
    private NodoSentencia elseOpcional() throws IOException {
        if (actualEn(Set.of("pr_else"))) {
            match("pr_else");
            return sentencia();
        }
        return null;
    }

    // <While> ::= while ( <Expresion> ) <Sentencia>
    private NodoWhile sentenciaWhile() throws IOException {
        Token tokenWhile = consumir("pr_while");
        match("parA");
        NodoExpresion condicion = expresion();
        match("parC");
        NodoSentencia cuerpo = sentencia();
        return new NodoWhile(tokenWhile, condicion, cuerpo);
    }

    // <Expresion> ::= <ExpresionCompuesta> <ExpresionResto>
    private NodoExpresion expresion() throws IOException {
        NodoExpresion izquierda = expresionCompuesta();
        if (actualEn(Set.of("op="))) {
            Token operador = operadorAsignacion();
            NodoExpresion derecha = expresionCompuesta();
            return new NodoExpresionAsignacion(operador, izquierda, derecha);
        }
        return izquierda;
    }

    // <OperadorAsignacion> ::= =
    private Token operadorAsignacion() throws IOException {
        return consumir("op=");
    }

    // <ExpresionCompuesta> ::= <ExpresionBasica> <ECResto>
    private NodoExpresion expresionCompuesta() throws IOException {
        NodoExpresion izquierda = expresionBasica();
        return eCResto(izquierda);
    }

    // <ECResto> ::= <OperadorBinario> <ExpresionBasica> <ECResto> | epsilon
    private NodoExpresion eCResto(NodoExpresion izquierda) throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_BINARIO)) {
            Token operador = operadorBinario();
            NodoExpresion derecha = expresionBasica();
            return eCResto(new NodoExpresionBinaria(operador, izquierda, derecha));
        }
        return izquierda;
    }

    // <OperadorBinario> ::= || | && | == | != | < | > | <= | >= | + | - | * | / | %
    private Token operadorBinario() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_BINARIO)) {
            return consumir(tokenActual.getNombre());
        }
        throw error("un operador binario");
    }

    // <ExpresionBasica> ::= <OperadorUnario> <Operando> <PosfijoOpcional>
    //                     | <Operando> <PosfijoOpcional>
    private NodoExpresion expresionBasica() throws IOException {
        Token operador = null;
        if (actualEn(PRIMEROS_OPERADOR_UNARIO)) {
            operador = operadorUnario();
        } else if (!actualEn(PRIMEROS_OPERANDO)) {
            throw error("una expresion");
        }
        NodoExpresion operando = operando();
        operando = posfijoOpcional(operando);
        if (operador != null) {
            return new NodoExpresionUnaria(operador, operando);
        }
        return operando;
    }

    // <PosfijoOpcional> ::= ++ | -- | epsilon
    private NodoExpresion posfijoOpcional(NodoExpresion operando) throws IOException {
        if (actualEn(Set.of("op++", "op--"))) {
            Token operador = consumir(tokenActual.getNombre());
            return new NodoExpresionPosfijo(operador, operando);
        }
        return operando;
    }

    // <OperadorUnario> ::= + | - | !
    private Token operadorUnario() throws IOException {
        if (actualEn(PRIMEROS_OPERADOR_UNARIO)) {
            return consumir(tokenActual.getNombre());
        }
        throw error("un operador unario ('+', '-' o '!')");
    }

    // <Operando> ::= <Primitivo> | <Referencia>
    private NodoExpresion operando() throws IOException {
        if (actualEn(PRIMEROS_PRIMITIVO)) {
            return primitivo();
        } else if (actualEn(PRIMEROS_PRIMARIO)) {
            return referencia();
        } else {
            throw error("un operando (literal o referencia)");
        }
    }

    // <Primitivo> ::= true | false | intLiteral | charLiteral | null
    private NodoLiteral primitivo() throws IOException {
        if (!actualEn(PRIMEROS_PRIMITIVO)) {
            throw error("un literal primitivo");
        }
        Token tokenLiteral = consumir(tokenActual.getNombre());
        Tipo tipo;
        switch (tokenLiteral.getNombre()) {
            case "litInt":
                tipo = new TipoPrimitivo(TipoPrimitivo.Base.INT, tokenLiteral);
                break;
            case "litChar":
                tipo = new TipoPrimitivo(TipoPrimitivo.Base.CHAR, tokenLiteral);
                break;
            case "pr_true":
            case "pr_false":
                tipo = new TipoPrimitivo(TipoPrimitivo.Base.BOOLEAN, tokenLiteral);
                break;
            case "pr_null":
                tipo = new TipoNull(tokenLiteral);
                break;
            default:
                throw error("un literal primitivo");
        }
        return new NodoLiteral(tokenLiteral, tipo);
    }

    // <Referencia> ::= <Primario> <RResto>
    private NodoExpresion referencia() throws IOException {
        NodoExpresion primario = primario();
        NodoEncadenado encadenado = rResto();
        if (encadenado == null) {
            return primario;
        }
        return new NodoReferencia(primario.getToken(), primario, encadenado);
    }

    // <RResto> ::= . idMetVar <MetodoOVar> <RResto>
    //            | <AccesoArreglo> <RResto>
    //            | epsilon
    private NodoEncadenado rResto() throws IOException {
        if (actualEn(Set.of("punto"))) {
            match("punto");
            Token tokenNombre = consumir("idMV");
            List<NodoExpresion> argumentos = metodoOVar();
            NodoEncadenado siguiente = rResto();
            if (argumentos == null) {
                return new NodoEncadenadoAtributo(tokenNombre, siguiente);
            }
            return new NodoEncadenadoLlamada(tokenNombre, argumentos, siguiente);
        } else if (actualEn(Set.of("corcheteA"))) {
            NodoExpresion indice = accesoArreglo();
            NodoEncadenado siguiente = rResto();
            return new NodoEncadenadoArreglo(indice, siguiente);
        }
        return null;
    }

    // <Primario> ::= this
    //              | stringLiteral
    //              | idMetVar <MetodoOVar>
    //              | new <ArregloOConstructor>
    //              | <LlamadaMetodoEstatico>
    //              | <ExpresionParentizada>
    private NodoExpresion primario() throws IOException {
        if (actualEn(Set.of("pr_this"))) {
            Token tokenThis = consumir("pr_this");
            return new NodoThis(tokenThis);
        } else if (actualEn(Set.of("litString"))) {
            Token tokenString = consumir("litString");
            return new NodoLiteral(tokenString, new TipoReferencia("String", null, tokenString));
        } else if (actualEn(Set.of("idMV"))) {
            Token tokenNombre = consumir("idMV");
            List<NodoExpresion> argumentos = metodoOVar();
            if (argumentos == null) {
                return new NodoVar(tokenNombre);
            }
            return new NodoLlamada(tokenNombre, argumentos);
        } else if (actualEn(Set.of("pr_new"))) {
            match("pr_new");
            return arregloOConstructor();
        } else if (actualEn(Set.of("idClase"))) {
            return llamadaMetodoEstatico();
        } else if (actualEn(Set.of("parA"))) {
            return expresionParentizada();
        } else {
            throw error("una referencia ('this', string, variable, llamada, 'new' o expresion parentizada)");
        }
    }

    // <ArregloOConstructor> ::= <TipoReferenciaInstanciacion> <AOC2>
    //                         | <TipoPrimitivo> <DimensionesConTamanio>
    //                         | idGen <DimensionesConTamanio>
    private NodoExpresion arregloOConstructor() throws IOException {
        if (actualEn(Set.of("idClase"))) {
            Token tokenIdClase = consumir("idClase");
            TipoReferencia tipo = tipoReferenciaNueva(tokenIdClase);
            if (actualEn(Set.of("parA"))) {
                List<NodoExpresion> argumentos = argsActuales();
                return new NodoNewObjeto(tokenIdClase, tipo, argumentos);
            }
            List<NodoExpresion> tamanios = dimensionesConTamanio();
            return new NodoNewArreglo(tokenIdClase, tipo, tamanios);
        } else if (actualEn(PRIMEROS_TIPO_PRIMITIVO)) {
            Token tokenTipo = tokenActual;
            TipoPrimitivo tipo = tipoPrimitivo();
            List<NodoExpresion> tamanios = dimensionesConTamanio();
            return new NodoNewArreglo(tokenTipo, tipo, tamanios);
        } else if (actualEn(Set.of("idGen"))) {
            Token tokenTipo = consumir("idGen");
            TipoParametro tipo = new TipoParametro(tokenTipo.getLexema(), tokenTipo);
            List<NodoExpresion> tamanios = dimensionesConTamanio();
            return new NodoNewArreglo(tokenTipo, tipo, tamanios);
        } else {
            throw error("un tipo de arreglo o constructor");
        }
    }

    // <MetodoOVar> ::= <ArgsActuales> | epsilon
    private List<NodoExpresion> metodoOVar() throws IOException {
        if (actualEn(Set.of("parA"))) {
            return argsActuales();
        }
        return null;
    }

    // <ExpresionParentizada> ::= ( <Expresion> )
    private NodoExpresion expresionParentizada() throws IOException {
        Token tokenParentesis = consumir("parA");
        NodoExpresion expresion = expresion();
        match("parC");
        return new NodoExpresionParentizada(tokenParentesis, expresion);
    }

    // <LlamadaMetodoEstatico> ::= idClase . idMetVar <ArgsActuales>
    private NodoExpresion llamadaMetodoEstatico() throws IOException {
        Token tokenClase = consumir("idClase");
        match("punto");
        Token tokenMetodo = consumir("idMV");
        List<NodoExpresion> argumentos = argsActuales();
        return new NodoLlamadaEstatica(tokenClase, tokenMetodo, argumentos);
    }

    // <DimensionesConTamanio> ::= [ <Expresion> ] <DCTResto>
    private List<NodoExpresion> dimensionesConTamanio() throws IOException {
        List<NodoExpresion> tamanios = new ArrayList<>();
        match("corcheteA");
        tamanios.add(expresion());
        match("corcheteC");
        dCTResto(tamanios);
        return tamanios;
    }

    // <DCTResto> ::= [ <Expresion> ] <DCTResto> | epsilon
    private void dCTResto(List<NodoExpresion> tamanios) throws IOException {
        while (actualEn(Set.of("corcheteA"))) {
            match("corcheteA");
            tamanios.add(expresion());
            match("corcheteC");
        }
    }

    // <ArgsActuales> ::= ( <ListaExpsOpcional> )
    private List<NodoExpresion> argsActuales() throws IOException {
        match("parA");
        List<NodoExpresion> argumentos = listaExpsOpcional();
        match("parC");
        return argumentos;
    }

    // <ListaExpsOpcional> ::= <ListaExps> | epsilon
    private List<NodoExpresion> listaExpsOpcional() throws IOException {
        if (actualEn(PRIMEROS_EXPRESION)) {
            return listaExps();
        }
        return new ArrayList<>();
    }

    // <ListaExps> ::= <Expresion> <ListaExpsResto>
    private List<NodoExpresion> listaExps() throws IOException {
        List<NodoExpresion> expresiones = new ArrayList<>();
        expresiones.add(expresion());
        while (actualEn(Set.of("coma"))) {
            match("coma");
            expresiones.add(expresion());
        }
        return expresiones;
    }

    // <AccesoArreglo> ::= [ <Expresion> ]
    private NodoExpresion accesoArreglo() throws IOException {
        match("corcheteA");
        NodoExpresion expresion = expresion();
        match("corcheteC");
        return expresion;
    }
}
