package analizadorsemantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import analizadorlexico.Token;

/** Clase concreta de MiniJava. */
public class Clase extends Entidad {

    private String parametroGenerico;
    private TipoReferencia ancestro;
    private boolean ancestroEsInterfaz;

    private final Map<String, Atributo> atributos = new LinkedHashMap<>();
    private final Map<String, Metodo> metodos = new LinkedHashMap<>();
    private final Map<Integer, Constructor> constructores = new LinkedHashMap<>();

    private boolean consolidada;

    public Clase(Token token, TablaDeSimbolos ts) {
        super(token, ts);
    }

    @Override
    public String getParametroGenerico() {
        return parametroGenerico;
    }

    public void setParametroGenerico(String parametroGenerico) {
        this.parametroGenerico = parametroGenerico;
    }

    public TipoReferencia getAncestro() {
        return ancestro;
    }

    /** Nombre del tipo heredado (o "Object" si la relacion es implicita). */
    public String getHerencia() {
        return ancestro == null ? "Object" : ancestro.getNombreReferencia();
    }

    public boolean isAncestroEsInterfaz() {
        return ancestroEsInterfaz;
    }

    public void setAncestro(TipoReferencia ancestro, boolean esInterfaz) {
        this.ancestro = ancestro;
        this.ancestroEsInterfaz = esInterfaz;
    }

    public Map<String, Atributo> getAtributos() {
        return atributos;
    }

    public Map<String, Metodo> getMetodos() {
        return metodos;
    }

    public Map<Integer, Constructor> getConstructores() {
        return constructores;
    }

    public boolean estaConsolidada() {
        return consolidada;
    }

    // ------------------------------------------------------------------
    // Construccion de la tabla (primera pasada)
    // ------------------------------------------------------------------

    public void agregarAtributo(Atributo atributo) {
        if (atributos.containsKey(atributo.getNombre())) {
            errorEn(atributo.getToken(),
                    "el atributo " + atributo.getNombre() + " ya fue declarado en la clase " + nombre);
        }
        atributos.put(atributo.getNombre(), atributo);
    }

    public void agregarMetodo(Metodo metodo) {
        if (metodos.containsKey(metodo.getClave())) {
            errorEn(metodo.getToken(),
                    "el metodo " + metodo.getNombre() + " con " + metodo.getAridad()
                            + " parametro(s) ya fue declarado en la clase " + nombre);
        }
        metodos.put(metodo.getClave(), metodo);
    }

    public void agregarConstructor(Constructor constructor) {
        if (!constructor.getNombre().equals(nombre)) {
            errorEn(constructor.getToken(),
                    "el constructor " + constructor.getNombre()
                            + " debe llamarse igual que su clase " + nombre);
        }
        if (constructores.containsKey(constructor.getAridad())) {
            errorEn(constructor.getToken(),
                    "el constructor de " + nombre + " con " + constructor.getAridad()
                            + " parametro(s) ya fue declarado");
        }
        constructores.put(constructor.getAridad(), constructor);
    }

    public void agregarConstructorPorDefecto() {
        if (constructores.isEmpty()) {
            constructores.put(0, new Constructor(token, new ArrayList<>(), ts));
        }
    }

    // ------------------------------------------------------------------
    // Chequeo de declaraciones (segunda pasada)
    // ------------------------------------------------------------------

    @Override
    public void estaBienDeclarada() {
        ts.setClaseActual(this);
        try {
            verificarRelacionDeHerencia();
            checkCircularidad();
            for (Atributo atributo : atributos.values()) {
                atributo.estaBienDeclarada();
            }
            for (Metodo metodo : metodos.values()) {
                metodo.estaBienDeclarada();
            }
            for (Constructor constructor : constructores.values()) {
                constructor.estaBienDeclarada();
            }
            verificarRedefiniciones();
            verificarContratoDeInterfaz();
        } finally {
            ts.setClaseActual(null);
        }
    }

    private void verificarRelacionDeHerencia() {
        if (ancestro == null) {
            return;
        }
        ancestro.esValidoEn(ts, parametroGenerico);
        Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
        if (ancestroEsInterfaz && !(entidadAncestro instanceof Interfaz)) {
            errorEn(ancestro.getToken(),
                    "la clase " + nombre + " no puede implementar a "
                            + ancestro.getNombreReferencia() + " porque no es una interfaz");
        }
        if (!ancestroEsInterfaz && !(entidadAncestro instanceof Clase)) {
            errorEn(ancestro.getToken(),
                    "la clase " + nombre + " no puede extender a "
                            + ancestro.getNombreReferencia() + " porque no es una clase");
        }
    }

    private void checkCircularidad() {
        Set<String> visitados = new HashSet<>();
        visitados.add(nombre);
        Entidad actual = this;
        while (actual instanceof Clase) {
            Clase clase = (Clase) actual;
            if (clase.ancestro == null || clase.ancestroEsInterfaz) {
                return;
            }
            String nombreAncestro = clase.ancestro.getNombreReferencia();
            if (!visitados.add(nombreAncestro)) {
                error("la clase " + nombre + " tiene herencia circular");
            }
            actual = ts.getTipo(nombreAncestro);
        }
    }

    // ------------------------------------------------------------------
    // Redefiniciones y contrato de interfaces (segunda pasada)
    // ------------------------------------------------------------------

    private void verificarRedefiniciones() {
        if (ancestro == null) {
            Entidad object = ts.getTipo("Object");
            if (object instanceof Clase) {
                recorrerAncestros(new HashMap<>(), (Clase) object);
            }
            return;
        }
        if (ancestroEsInterfaz) {
            return;
        }
        Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
        if (entidadAncestro instanceof Clase) {
            recorrerAncestros(sustitucionPara(ancestro, entidadAncestro), (Clase) entidadAncestro);
        }
    }

    private void recorrerAncestros(Map<String, Tipo> sustitucion, Clase claseAncestro) {
        for (Metodo metodoHeredado : claseAncestro.metodos.values()) {
            Metodo heredado = metodoHeredado.instanciar(sustitucion);
            Metodo propio = metodos.get(heredado.getClave());
            if (propio == null) {
                continue;
            }
            if (heredado.esEstatico() || propio.esEstatico()) {
                errorEn(propio.getToken(),
                        "el metodo " + propio.getNombre() + " con " + propio.getAridad()
                                + " parametro(s) entra en conflicto con un metodo heredado de "
                                + claseAncestro.getNombre());
            }
            if (!propio.mismaSignatura(heredado)) {
                errorEn(propio.getToken(),
                        "el metodo " + propio.getNombre() + " no redefine correctamente a "
                                + claseAncestro.getNombre() + "." + heredado.getNombre());
            }
        }
        if (claseAncestro.ancestro != null && !claseAncestro.ancestroEsInterfaz) {
            Entidad siguiente = ts.getTipo(claseAncestro.ancestro.getNombreReferencia());
            if (siguiente instanceof Clase) {
                Map<String, Tipo> nuevaSustitucion = new HashMap<>();
                String parametro = siguiente.getParametroGenerico();
                if (parametro != null) {
                    Tipo argumento = claseAncestro.ancestro.getArgumento();
                    nuevaSustitucion.put(parametro,
                            argumento != null
                                    ? argumento.sustituir(sustitucion)
                                    : new TipoParametro(parametro, claseAncestro.ancestro.getToken()));
                }
                recorrerAncestros(nuevaSustitucion, (Clase) siguiente);
            }
        }
    }

    private void verificarContratoDeInterfaz() {
        if (!ancestroEsInterfaz || ancestro == null) {
            return;
        }
        Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
        if (!(entidadAncestro instanceof Interfaz)) {
            return;
        }
        Interfaz interfaz = (Interfaz) entidadAncestro;
        for (Metodo requerido : interfaz.getMetodosInstanciados(sustitucionPara(ancestro, interfaz)).values()) {
            Metodo propio = metodos.get(requerido.getClave());
            if (propio == null || propio.esEstatico() || !propio.mismaSignatura(requerido)) {
                error("la clase " + nombre + " no implementa el metodo " + requerido.getNombre()
                        + " de la interfaz " + interfaz.getNombre());
            }
        }
    }

    private static Map<String, Tipo> sustitucionPara(TipoReferencia referencia, Entidad ancestro) {
        Map<String, Tipo> sustitucion = new HashMap<>();
        String parametro = ancestro.getParametroGenerico();
        if (parametro != null) {
            Tipo argumento = referencia.getArgumento();
            sustitucion.put(parametro,
                    argumento != null
                            ? argumento
                            : new TipoParametro(parametro, referencia.getToken()));
        }
        return sustitucion;
    }

    // ------------------------------------------------------------------
    // Consolidacion
    // ------------------------------------------------------------------

    public void consolidar() {
        if (consolidada) {
            return;
        }
        if (nombre.equals("Object")) {
            consolidada = true;
            return;
        }

        if (ancestro == null) {
            Entidad object = ts.getTipo("Object");
            if (object instanceof Clase) {
                Clase claseAncestro = (Clase) object;
                claseAncestro.consolidar();
                heredarAtributos(claseAncestro, new HashMap<>());
                heredarMetodos(claseAncestro, new HashMap<>());
            }
        } else if (!ancestroEsInterfaz) {
            Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
            if (entidadAncestro instanceof Clase) {
                Clase claseAncestro = (Clase) entidadAncestro;
                claseAncestro.consolidar();
                Map<String, Tipo> sustitucion = sustitucionPara(ancestro, claseAncestro);
                heredarAtributos(claseAncestro, sustitucion);
                heredarMetodos(claseAncestro, sustitucion);
            }
        }

        consolidada = true;
    }

    private void heredarAtributos(Clase claseAncestro, Map<String, Tipo> sustitucion) {
        for (Atributo atributoHeredado : claseAncestro.atributos.values()) {
            Atributo atributo = atributoHeredado.instanciar(sustitucion);
            if (atributos.containsKey(atributo.getNombre())) {
                Atributo propio = atributos.get(atributo.getNombre());
                errorEn(propio.getToken(),
                        "el atributo " + propio.getNombre()
                                + " ya fue declarado en un ancestro de " + nombre);
            }
            atributos.put(atributo.getNombre(), atributo);
        }
    }

    private void heredarMetodos(Clase claseAncestro, Map<String, Tipo> sustitucion) {
        for (Metodo metodoHeredado : claseAncestro.metodos.values()) {
            Metodo heredado = metodoHeredado.instanciar(sustitucion);
            // La version propia (redefinicion valida) tiene prioridad.
            metodos.putIfAbsent(heredado.getClave(), heredado);
        }
    }
}
