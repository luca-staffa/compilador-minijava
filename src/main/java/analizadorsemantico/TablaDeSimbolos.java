package analizadorsemantico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import analizadorlexico.Token;

/**
 * Tabla de simbolos global del programa. Contiene las clases e interfaces
 * declaradas por el usuario y las entidades predefinidas (Object, String y
 * System).
 */
public class TablaDeSimbolos {

    private final Map<String, Clase> clases = new LinkedHashMap<>();
    private final Map<String, Interfaz> interfaces = new LinkedHashMap<>();

    private Clase claseActual;
    private Interfaz interfazActual;
    private Metodo metodoActual;
    private Entidad unidadActual;
    private Bloque bloqueActual;

    public TablaDeSimbolos() {
        inicializarPredefinidas();
    }

    // ------------------------------------------------------------------
    // Administracion de la tabla
    // ------------------------------------------------------------------

    /** Inserta una entidad declarada por el usuario, controlando nombres repetidos. */
    public void insertar(Entidad entidad) {
        if (esTipo(entidad.getNombre())) {
            throw new ExcepcionSemantica(
                    entidad.getToken().getLexema(),
                    entidad.getToken().getNroLinea(),
                    "el tipo " + entidad.getNombre() + " ya fue declarado");
        }
        if (entidad instanceof Clase) {
            clases.put(entidad.getNombre(), (Clase) entidad);
        } else if (entidad instanceof Interfaz) {
            interfaces.put(entidad.getNombre(), (Interfaz) entidad);
        }
    }

    private void insertarPredefinida(Entidad entidad) {
        if (entidad instanceof Clase) {
            clases.put(entidad.getNombre(), (Clase) entidad);
        } else if (entidad instanceof Interfaz) {
            interfaces.put(entidad.getNombre(), (Interfaz) entidad);
        }
    }

    public boolean esTipo(String nombre) {
        return clases.containsKey(nombre) || interfaces.containsKey(nombre);
    }

    public Entidad getTipo(String nombre) {
        Entidad entidad = clases.get(nombre);
        return entidad != null ? entidad : interfaces.get(nombre);
    }

    public Map<String, Clase> getClases() {
        return clases;
    }

    public Map<String, Interfaz> getInterfaces() {
        return interfaces;
    }

    public Clase getClaseActual() {
        return claseActual;
    }

    public void setClaseActual(Clase claseActual) {
        this.claseActual = claseActual;
    }

    public Interfaz getInterfazActual() {
        return interfazActual;
    }

    public void setInterfazActual(Interfaz interfazActual) {
        this.interfazActual = interfazActual;
    }

    public Metodo getMetodoActual() {
        return metodoActual;
    }

    public void setMetodoActual(Metodo metodoActual) {
        this.metodoActual = metodoActual;
    }

    public Entidad getUnidadActual() {
        return unidadActual;
    }

    public void setUnidadActual(Entidad unidadActual) {
        this.unidadActual = unidadActual;
    }

    public Bloque getBloqueActual() {
        return bloqueActual;
    }

    public void setBloqueActual(Bloque bloqueActual) {
        this.bloqueActual = bloqueActual;
    }

    /** Abre un nuevo ambito lexico hijo del actual. */
    public void entrarBloque() {
        bloqueActual = new Bloque(bloqueActual);
    }

    /** Cierra el ambito lexico actual y restaura el contenedor. */
    public void salirBloque() {
        if (bloqueActual != null) {
            bloqueActual = bloqueActual.getPadre();
        }
    }

    /** Busca una variable local visible desde el bloque actual hacia afuera. */
    public VariableLocal buscarVariableLocal(String nombre) {
        Bloque bloque = bloqueActual;
        while (bloque != null) {
            VariableLocal variable = bloque.getVariable(nombre);
            if (variable != null) {
                return variable;
            }
            bloque = bloque.getPadre();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Chequeo de declaraciones y consolidacion
    // ------------------------------------------------------------------

    public void estaBienDeclarada() {
        for (Interfaz interfaz : interfaces.values()) {
            interfaz.estaBienDeclarada();
        }
        for (Clase clase : clases.values()) {
            clase.estaBienDeclarada();
        }
    }

    public void consolidar() {
        for (Interfaz interfaz : interfaces.values()) {
            interfaz.consolidar();
        }
        for (Clase clase : clases.values()) {
            clase.consolidar();
        }
    }

    /**
     * Chequeo de sentencias: recorre el cuerpo de cada unidad declarada por el
     * usuario en la clase donde fue declarada. Los cuerpos heredados por
     * consolidacion no se vuelven a chequear.
     */
    public void chequearCuerpos() {
        Clase claseAnterior = claseActual;
        Interfaz interfazAnterior = interfazActual;
        Metodo metodoAnterior = metodoActual;
        Entidad unidadAnterior = unidadActual;
        Bloque bloqueAnterior = bloqueActual;
        try {
            for (Clase clase : clases.values()) {
                claseActual = clase;
                interfazActual = null;
                metodoActual = null;
                for (Unidad unidad : clase.getUnidadesPropias()) {
                    unidadActual = unidad;
                    bloqueActual = null;
                    unidad.chequearCuerpo(this);
                }
            }
        } finally {
            claseActual = claseAnterior;
            interfazActual = interfazAnterior;
            metodoActual = metodoAnterior;
            unidadActual = unidadAnterior;
            bloqueActual = bloqueAnterior;
        }
    }

    // ------------------------------------------------------------------
    // Entidades predefinidas
    // ------------------------------------------------------------------

    private void inicializarPredefinidas() {
        Clase object = new Clase(tokenClase("Object"), this);
        object.agregarMetodo(new Metodo(
                tokenId("toString"),
                new TipoReferencia("String", null, null),
                new ArrayList<>(),
                false,
                this));
        insertarPredefinida(object);

        insertarPredefinida(new Clase(tokenClase("String"), this));

        Clase system = new Clase(tokenClase("System"), this);
        system.agregarMetodo(metodoEstatico("debugPrint", tipoVoid(), tipoInt()));
        system.agregarMetodo(metodoEstatico("read", tipoInt()));
        system.agregarMetodo(metodoEstatico("printB", tipoVoid(), tipoBoolean()));
        system.agregarMetodo(metodoEstatico("printC", tipoVoid(), tipoChar()));
        system.agregarMetodo(metodoEstatico("printI", tipoVoid(), tipoInt()));
        system.agregarMetodo(metodoEstatico("printS", tipoVoid(), tipoString()));
        system.agregarMetodo(metodoEstatico("println", tipoVoid()));
        system.agregarMetodo(metodoEstatico("printBln", tipoVoid(), tipoBoolean()));
        system.agregarMetodo(metodoEstatico("printCln", tipoVoid(), tipoChar()));
        system.agregarMetodo(metodoEstatico("printIln", tipoVoid(), tipoInt()));
        system.agregarMetodo(metodoEstatico("printSln", tipoVoid(), tipoString()));
        insertarPredefinida(system);
    }

    private static Token tokenClase(String lexema) {
        return new Token("idClase", lexema, 0);
    }

    private static Token tokenId(String lexema) {
        return new Token("idMV", lexema, 0);
    }

    private static Tipo tipoVoid() {
        return new TipoVoid(null);
    }

    private static Tipo tipoInt() {
        return new TipoPrimitivo(TipoPrimitivo.Base.INT, null);
    }

    private static Tipo tipoBoolean() {
        return new TipoPrimitivo(TipoPrimitivo.Base.BOOLEAN, null);
    }

    private static Tipo tipoChar() {
        return new TipoPrimitivo(TipoPrimitivo.Base.CHAR, null);
    }

    private static Tipo tipoString() {
        return new TipoReferencia("String", null, null);
    }

    private Metodo metodoEstatico(String nombre, Tipo retorno, Tipo... tipos) {
        List<Parametro> parametros = new ArrayList<>();
        int posicion = 0;
        for (Tipo tipo : tipos) {
            parametros.add(new Parametro(tokenId("p" + posicion), tipo, posicion, this));
            posicion++;
        }
        return new Metodo(tokenId(nombre), retorno, parametros, true, this);
    }
}
