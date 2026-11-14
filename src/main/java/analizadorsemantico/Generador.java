package analizadorsemantico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import analizadorsemantico.ast.NodoSentencia;

/**
 * Generador de codigo CeIASM para la CeIVM.
 *
 * Calcula los offsets (atributos en el CIR, metodos en la VT, parametros y
 * variables locales en el RA) y arma el programa intermedio recorriendo los AST
 * de las unidades ya resueltos por el analisis semantico.
 */
public class Generador {

    private final TablaDeSimbolos ts;
    private final List<String> lineas = new ArrayList<>();
    private final Map<String, String> literales = new LinkedHashMap<>();
    private final Map<String, List<Metodo>> vts = new LinkedHashMap<>();
    private int contadorEtiquetas = 0;
    private int contadorStrings = 0;

    // Contexto de la unidad en generacion.
    private Unidad unidadActual;
    private Clase claseActual;
    private int aridad;
    private boolean estatico;
    private int offsetRetorno;
    private int localesVivas;

    public Generador(TablaDeSimbolos ts) {
        this.ts = ts;
    }

    public TablaDeSimbolos getTabla() {
        return ts;
    }

    // ------------------------------------------------------------------
    // Emision
    // ------------------------------------------------------------------

    public void emit(String instruccion) {
        lineas.add("\t" + instruccion);
    }

    public void emit(String instruccion, String comentario) {
        lineas.add("\t" + instruccion + "  ; " + comentario);
    }

    public void etiqueta(String etiqueta) {
        lineas.add(etiqueta + ":");
    }

    public void etiqueta(String etiqueta, String instruccion) {
        lineas.add(etiqueta + ": " + instruccion);
    }

    public void cruda(String texto) {
        lineas.add(texto);
    }

    public void vacia() {
        lineas.add("");
    }

    public String nuevaEtiqueta(String prefijo) {
        return prefijo + (++contadorEtiquetas);
    }

    // ------------------------------------------------------------------
    // Contexto de unidad
    // ------------------------------------------------------------------

    public void entrarUnidad(Unidad u, Clase c) {
        this.unidadActual = u;
        this.claseActual = c;
        this.aridad = u.getAridad();
        this.estatico = u.esEstatico();
        this.offsetRetorno = estatico ? aridad + 3 : aridad + 4;
        this.localesVivas = 0;
    }

    public Unidad getUnidadActual() {
        return unidadActual;
    }

    public Clase getClaseActual() {
        return claseActual;
    }

    public int getAridad() {
        return aridad;
    }

    public boolean esEstatico() {
        return estatico;
    }

    public int getOffsetRetorno() {
        return offsetRetorno;
    }

    public int getLocalesVivas() {
        return localesVivas;
    }

    public void setLocalesVivas(int valor) {
        this.localesVivas = valor;
    }

    public void incLocales() {
        this.localesVivas++;
    }

    /** Reserva una celda temporal en el RA y devuelve su offset. */
    public int reservarTemporal() {
        int offset = -localesVivas;
        localesVivas++;
        emit("RMEM 1", "celda temporal");
        return offset;
    }

    public boolean esConstructor() {
        return unidadActual instanceof Constructor;
    }

    public boolean metodoVoid() {
        return unidadActual instanceof Metodo
                && ((Metodo) unidadActual).getTipoRetorno() instanceof TipoVoid;
    }

    public static boolean dejaValor(Unidad u) {
        return !(u instanceof Metodo)
                || !(((Metodo) u).getTipoRetorno() instanceof TipoVoid);
    }

    /** Genera el cuerpo de una sentencia controlando las variables locales vivas. */
    public void generarCuerpo(NodoSentencia sentencia) {
        int inicial = localesVivas;
        sentencia.generar(this);
        int nuevas = localesVivas - inicial;
        if (nuevas > 0) {
            emit("FMEM " + nuevas, "libera variables locales del cuerpo");
            localesVivas = inicial;
        }
    }

    // ------------------------------------------------------------------
    // Etiquetas de strings literales
    // ------------------------------------------------------------------

    public String etiquetaString(String contenido) {
        String etiqueta = literales.get(contenido);
        if (etiqueta == null) {
            etiqueta = "lblStr" + (++contadorStrings);
            literales.put(contenido, etiqueta);
        }
        return etiqueta;
    }

    // ------------------------------------------------------------------
    // Calculo de offsets y etiquetas
    // ------------------------------------------------------------------

    public void calcularOffsets() {
        Entidad object = ts.getTipo("Object");
        if (object instanceof Clase) {
            vtClase((Clase) object);
        }
        for (Interfaz interfaz : ts.getInterfaces().values()) {
            vtInterfaz(interfaz);
        }
        for (Clase clase : ts.getClases().values()) {
            vtClase(clase);
        }
    }

    private List<Metodo> vtInterfaz(Interfaz interfaz) {
        List<Metodo> vt = vts.get(interfaz.getNombre());
        if (vt != null) {
            return vt;
        }
        vt = new ArrayList<>();
        vt.add(toStringObject());
        if (interfaz.getAncestro() != null) {
            Entidad entidad = ts.getTipo(interfaz.getAncestro().getNombreReferencia());
            if (entidad instanceof Interfaz) {
                List<Metodo> base = vtInterfaz((Interfaz) entidad);
                for (int i = 1; i < base.size(); i++) {
                    vt.add(base.get(i));
                }
            }
        }
        for (Metodo metodo : interfaz.getMetodos().values()) {
            ubicarEnVt(vt, metodo);
        }
        for (Metodo metodo : interfaz.getMetodos().values()) {
            metodo.setEtiqueta(etiquetaUnidad(metodo, interfaz.getNombre()));
        }
        vts.put(interfaz.getNombre(), vt);
        return vt;
    }

    private List<Metodo> vtClase(Clase clase) {
        List<Metodo> vt = vts.get(clase.getNombre());
        if (vt != null) {
            return vt;
        }

        // Atributos en el CIR: el 0 es la referencia a la VT.
        int siguiente = 1;
        for (Atributo atributo : clase.getAtributos().values()) {
            if (atributo.getOffset() >= 0) {
                siguiente = Math.max(siguiente, atributo.getOffset() + 1);
            }
        }
        for (Atributo atributo : clase.getAtributos().values()) {
            if (atributo.getOffset() < 0) {
                atributo.setOffset(siguiente++);
            }
        }

        // Tabla virtual.
        vt = new ArrayList<>();
        TipoReferencia ancestro = clase.getAncestro();
        if (ancestro != null && !clase.isAncestroEsInterfaz()) {
            Entidad entidad = ts.getTipo(ancestro.getNombreReferencia());
            if (entidad instanceof Clase) {
                vt.addAll(vtClase((Clase) entidad));
            }
        } else {
            vt.add(toStringObject());
            if (ancestro != null && clase.isAncestroEsInterfaz()) {
                Entidad entidad = ts.getTipo(ancestro.getNombreReferencia());
                if (entidad instanceof Interfaz) {
                    List<Metodo> base = vtInterfaz((Interfaz) entidad);
                    for (int i = 1; i < base.size(); i++) {
                        vt.add(base.get(i));
                    }
                }
            }
        }
        for (Unidad unidad : clase.getUnidadesPropias()) {
            if (unidad instanceof Metodo && !unidad.esEstatico()) {
                ubicarEnVt(vt, (Metodo) unidad);
            }
        }

        // Reflejar los offsets en las vistas guardadas en el mapa de la clase.
        for (Metodo metodo : clase.getMetodos().values()) {
            if (metodo.esEstatico()) {
                continue;
            }
            int indice = indicePorClave(vt, metodo);
            if (indice >= 0) {
                metodo.setOffset(vt.get(indice).getOffset());
            }
        }

        asignarEtiquetasYParametros(clase.getNombre(), clase.getUnidadesPropias());
        vts.put(clase.getNombre(), vt);
        return vt;
    }

    private void ubicarEnVt(List<Metodo> vt, Metodo metodo) {
        int indice = indicePorClave(vt, metodo);
        if (indice >= 0) {
            metodo.setOffset(vt.get(indice).getOffset());
            vt.set(indice, metodo);
        } else {
            metodo.setOffset(vt.size());
            vt.add(metodo);
        }
    }

    private static int indicePorClave(List<Metodo> vt, Metodo metodo) {
        for (int i = 0; i < vt.size(); i++) {
            if (vt.get(i).getClave().equals(metodo.getClave())) {
                return i;
            }
        }
        return -1;
    }

    private Metodo toStringObject() {
        Entidad object = ts.getTipo("Object");
        if (object instanceof Clase) {
            return ((Clase) object).getMetodos().get("toString/0");
        }
        return null;
    }

    private void asignarEtiquetasYParametros(String nombreClase, List<Unidad> unidades) {
        if (unidades == null) {
            return;
        }
        for (Unidad unidad : unidades) {
            unidad.setEtiqueta(etiquetaUnidad(unidad, nombreClase));
            for (Parametro parametro : unidad.getParams().values()) {
                int offset = unidad.esEstatico()
                        ? unidad.getAridad() + 2 - parametro.getPosicion()
                        : unidad.getAridad() + 3 - parametro.getPosicion();
                parametro.setOffset(offset);
            }
        }
    }

    public static String etiquetaUnidad(Unidad unidad, String nombreClase) {
        if (unidad instanceof Constructor) {
            return "lblConstructor_" + unidad.getAridad() + "@" + nombreClase;
        }
        return "lblMet" + unidad.getNombre() + "_" + unidad.getAridad() + "@" + nombreClase;
    }

    // ------------------------------------------------------------------
    // Programa completo
    // ------------------------------------------------------------------

    public String generar() {
        calcularOffsets();

        cruda(".CODE");
        emit("PUSH simple_heap_init", "inicializa el heap");
        emit("CALL");
        Metodo main = buscarMain();
        if (main != null) {
            emit("PUSH " + main.getEtiqueta(), "invoca el metodo main");
            emit("CALL");
        }
        emit("HALT");
        vacia();

        rutinasHeap();
        vacia();

        for (Clase clase : ts.getClases().values()) {
            generarClase(clase);
        }

        if (!literales.isEmpty()) {
            cruda(".DATA");
            for (Map.Entry<String, String> entrada : literales.entrySet()) {
                emitirString(entrada.getValue(), entrada.getKey());
            }
        }

        return String.join("\n", lineas) + "\n";
    }

    private Metodo buscarMain() {
        for (Clase clase : ts.getClases().values()) {
            for (Unidad unidad : clase.getUnidadesPropias()) {
                if (unidad instanceof Metodo
                        && unidad.getNombre().equals("main")
                        && unidad.getAridad() == 0
                        && unidad.esEstatico()) {
                    return (Metodo) unidad;
                }
            }
        }
        return null;
    }

    private void rutinasHeap() {
        cruda("simple_heap_init: RET 0  ; Inicializacion simplificada del heap");
        vacia();
        cruda("simple_malloc: LOADFP");
        cruda("\tLOADSP");
        cruda("\tSTOREFP");
        cruda("\tLOADHL");
        cruda("\tDUP");
        cruda("\tPUSH 1");
        cruda("\tADD");
        cruda("\tSTORE 4");
        cruda("\tLOAD 3");
        cruda("\tADD");
        cruda("\tSTOREHL");
        cruda("\tSTOREFP");
        cruda("\tRET 1");
    }

    private void generarClase(Clase clase) {
        vacia();
        cruda(".DATA");
        etiqueta("lblVT" + clase.getNombre());
        cruda("\tDW " + etiquetasVt(vtClase(clase)));
        cruda(".CODE");
        for (Constructor constructor : clase.getConstructores().values()) {
            vacia();
            generarConstructor(clase, constructor);
        }
        for (Unidad unidad : clase.getUnidadesPropias()) {
            if (unidad instanceof Metodo) {
                vacia();
                generarMetodo(clase, (Metodo) unidad);
            }
        }
    }

    private String etiquetasVt(List<Metodo> vt) {
        List<String> etiquetas = new ArrayList<>();
        for (Metodo metodo : vt) {
            etiquetas.add(metodo.getEtiqueta() != null ? metodo.getEtiqueta() : "0");
        }
        return String.join(",", etiquetas);
    }

    private void generarMetodo(Clase clase, Metodo metodo) {
        etiqueta(metodo.getEtiqueta());
        emit("LOADFP", "apila el valor del registro fp");
        emit("LOADSP", "apila el valor del registro sp");
        emit("STOREFP", "almacena el tope de la pila en el registro fp");
        if (esPredefinida(clase.getNombre())) {
            generarCuerpoPredefinido(clase.getNombre(), metodo);
        } else {
            entrarUnidad(metodo, clase);
            if (metodo.getCuerpo() != null) {
                for (NodoSentencia sentencia : metodo.getCuerpo().getSentencias()) {
                    sentencia.generar(this);
                }
            }
            emit("FMEM " + localesVivas, "libera variables locales");
        }
        emit("STOREFP", "restablece el contexto del llamador");
        emit("RET " + (metodo.esEstatico() ? metodo.getAridad() : metodo.getAridad() + 1));
    }

    private void generarConstructor(Clase clase, Constructor constructor) {
        etiqueta(constructor.getEtiqueta());
        emit("LOADFP", "apila el valor del registro fp");
        emit("LOADSP", "apila el valor del registro sp");
        emit("STOREFP", "almacena el tope de la pila en el registro fp");
        if (esPredefinida(clase.getNombre())) {
            emit("FMEM 0");
        } else {
            entrarUnidad(constructor, clase);
            if (constructor.getCuerpo() != null) {
                for (NodoSentencia sentencia : constructor.getCuerpo().getSentencias()) {
                    sentencia.generar(this);
                }
            }
            emit("FMEM " + localesVivas, "libera variables locales");
        }
        emit("STOREFP", "restablece el contexto del llamador");
        emit("RET " + (constructor.getAridad() + 1));
    }

    private boolean esPredefinida(String nombreClase) {
        return nombreClase.equals("Object")
                || nombreClase.equals("String")
                || nombreClase.equals("System");
    }

    private void generarCuerpoPredefinido(String nombreClase, Metodo metodo) {
        String nombre = metodo.getNombre();
        if (nombreClase.equals("Object")) {
            if (nombre.equals("debugPrint")) {
                emit("LOAD 3", "apila el parametro");
                emit("IPRINT", "imprime el entero en el tope de la pila");
            } else if (nombre.equals("toString")) {
                emit("PUSH " + etiquetaString(""), "cadena vacia");
                emit("STORE 4");
            }
        } else if (nombreClase.equals("System")) {
            if (nombre.equals("read")) {
                emit("READ", "lee un valor entero");
                emit("STORE 3");
            } else if (nombre.equals("println")) {
                emit("PRNLN", "imprime el caracter de nueva linea");
            } else {
                emit("LOAD 3", "apila el parametro");
                if (nombre.startsWith("printS")) {
                    emit("SPRINT", "imprime el string en el tope de la pila");
                } else if (nombre.startsWith("printI")) {
                    emit("IPRINT", "imprime el entero en el tope de la pila");
                } else if (nombre.startsWith("printB")) {
                    emit("BPRINT", "imprime el booleano en el tope de la pila");
                } else if (nombre.startsWith("printC")) {
                    emit("CPRINT", "imprime el char en el tope de la pila");
                }
                if (nombre.endsWith("ln")) {
                    emit("PRNLN", "imprime el caracter de nueva linea");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Strings literales
    // ------------------------------------------------------------------

    private void emitirString(String etiqueta, String contenido) {
        StringBuilder sb = new StringBuilder(etiqueta + ": DW ");
        String desescapado = desescapar(contenido);
        boolean primero = true;
        for (int i = 0; i < desescapado.length(); i++) {
            if (!primero) {
                sb.append(",");
            }
            sb.append((int) desescapado.charAt(i));
            primero = false;
        }
        if (!primero) {
            sb.append(",");
        }
        sb.append("0");
        cruda(sb.toString());
    }

    /** Quita las comillas de un literal de string y resuelve los escapes. */
    public static String desescapar(String lexema) {
        String cuerpo = lexema;
        if (cuerpo.length() >= 2 && cuerpo.charAt(0) == '"'
                && cuerpo.charAt(cuerpo.length() - 1) == '"') {
            cuerpo = cuerpo.substring(1, cuerpo.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cuerpo.length(); i++) {
            char c = cuerpo.charAt(i);
            if (c == '\\' && i + 1 < cuerpo.length()) {
                char s = cuerpo.charAt(++i);
                switch (s) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case '0': sb.append('\0'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    case '\'': sb.append('\''); break;
                    default: sb.append(s);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Codigo ASCII de un literal de caracter (con comillas y escapes). */
    public static int valorCaracter(String lexema) {
        String desescapado = desescapar(lexema.replace('\'', '"'));
        return desescapado.isEmpty() ? 0 : desescapado.charAt(0);
    }
}
