package analizadorsemantico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import analizadorlexico.Token;

/** Interfaz de MiniJava. */
public class Interfaz extends Entidad {

    private String parametroGenerico;
    private TipoReferencia ancestro;
    private final Map<String, Metodo> metodos = new LinkedHashMap<>();
    private boolean consolidada;

    public Interfaz(Token token, TablaDeSimbolos ts) {
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

    public void setAncestro(TipoReferencia ancestro) {
        this.ancestro = ancestro;
    }

    public Map<String, Metodo> getMetodos() {
        return metodos;
    }

    public boolean estaConsolidada() {
        return consolidada;
    }

    public void agregarMetodo(Metodo metodo) {
        if (metodos.containsKey(metodo.getClave())) {
            errorEn(metodo.getToken(),
                    "el metodo " + metodo.getNombre() + " con " + metodo.getAridad()
                            + " parametro(s) ya fue declarado en la interfaz " + nombre);
        }
        metodos.put(metodo.getClave(), metodo);
    }

    @Override
    public void estaBienDeclarada() {
        ts.setInterfazActual(this);
        try {
            if (ancestro != null) {
                ancestro.esValidoEn(ts, parametroGenerico);
                Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
                if (!(entidadAncestro instanceof Interfaz)) {
                    errorEn(ancestro.getToken(),
                            "la interfaz " + nombre + " no puede extender a "
                                    + ancestro.getNombreReferencia() + " porque no es una interfaz");
                }
            }
            checkCircularidad();
            for (Metodo metodo : metodos.values()) {
                metodo.estaBienDeclarada();
            }
            verificarRedefiniciones();
        } finally {
            ts.setInterfazActual(null);
        }
    }

    /**
     * Un metodo de la interfaz puede redeclarar uno heredado de la interfaz
     * extendida solo si ambas signaturas coinciden exactamente (luego de
     * aplicar la instanciacion generica correspondiente).
     */
    private void verificarRedefiniciones() {
        for (Metodo metodo : metodos.values()) {
            Map<String, Tipo> sustitucion = new HashMap<>();
            Entidad actual = null;
            if (ancestro != null) {
                Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
                if (!(entidadAncestro instanceof Interfaz)) {
                    return;
                }
                actual = entidadAncestro;
                sustitucion = sustitucionPara(ancestro, (Interfaz) entidadAncestro);
            }
            while (actual instanceof Interfaz) {
                Interfaz interfazAncestro = (Interfaz) actual;
                Metodo heredado = interfazAncestro.metodos.get(metodo.getClave());
                if (heredado != null) {
                    heredado = heredado.instanciar(sustitucion);
                    if (!metodo.mismaSignatura(heredado)) {
                        errorEn(metodo.getToken(),
                                "el metodo " + metodo.getNombre() + " con " + metodo.getAridad()
                                        + " parametro(s) no redefine correctamente al metodo heredado de la interfaz "
                                        + interfazAncestro.getNombre());
                    }
                    break;
                }
                if (interfazAncestro.ancestro == null) {
                    break;
                }
                Entidad siguiente = ts.getTipo(interfazAncestro.ancestro.getNombreReferencia());
                if (!(siguiente instanceof Interfaz)) {
                    break;
                }
                Map<String, Tipo> nuevaSustitucion = new HashMap<>();
                String parametro = ((Interfaz) siguiente).getParametroGenerico();
                if (parametro != null) {
                    Tipo argumento = interfazAncestro.ancestro.getArgumento();
                    nuevaSustitucion.put(parametro,
                            argumento != null
                                    ? argumento.sustituir(sustitucion)
                                    : new TipoParametro(parametro, interfazAncestro.ancestro.getToken()));
                }
                sustitucion = nuevaSustitucion;
                actual = siguiente;
            }
        }
    }

    private void checkCircularidad() {
        Set<String> visitados = new HashSet<>();
        visitados.add(nombre);
        Entidad actual = this;
        while (actual instanceof Interfaz) {
            Interfaz interfaz = (Interfaz) actual;
            if (interfaz.ancestro == null) {
                return;
            }
            String nombreAncestro = interfaz.ancestro.getNombreReferencia();
            if (!visitados.add(nombreAncestro)) {
                error("la interfaz " + nombre + " tiene extension circular");
            }
            actual = ts.getTipo(nombreAncestro);
        }
    }

    public void consolidar() {
        if (consolidada) {
            return;
        }
        if (ancestro != null) {
            Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
            if (entidadAncestro instanceof Interfaz) {
                Interfaz interfazAncestro = (Interfaz) entidadAncestro;
                interfazAncestro.consolidar();
                Map<String, Tipo> sustitucion = sustitucionPara(ancestro, interfazAncestro);
                for (Metodo metodoHeredado : interfazAncestro.metodos.values()) {
                    Metodo heredado = metodoHeredado.instanciar(sustitucion);
                    metodos.putIfAbsent(heredado.getClave(), heredado);
                }
            }
        }
        consolidada = true;
    }

    /**
     * Metodos de la interfaz (propios y heredados de forma recursiva) con la
     * sustitucion indicada aplicada. No depende del orden de consolidacion.
     */
    public Map<String, Metodo> getMetodosInstanciados(Map<String, Tipo> sustitucion) {
        Map<String, Metodo> resultado = new LinkedHashMap<>();
        for (Metodo metodo : metodos.values()) {
            resultado.put(metodo.getClave(), metodo.instanciar(sustitucion));
        }
        if (ancestro != null) {
            Entidad entidadAncestro = ts.getTipo(ancestro.getNombreReferencia());
            if (entidadAncestro instanceof Interfaz) {
                Interfaz interfazAncestro = (Interfaz) entidadAncestro;
                Map<String, Tipo> sustitucionAncestro = new HashMap<>();
                String parametro = interfazAncestro.getParametroGenerico();
                if (parametro != null) {
                    Tipo argumento = ancestro.getArgumento();
                    sustitucionAncestro.put(parametro,
                            argumento != null
                                    ? argumento.sustituir(sustitucion)
                                    : new TipoParametro(parametro, ancestro.getToken()));
                }
                for (Map.Entry<String, Metodo> entrada
                        : interfazAncestro.getMetodosInstanciados(sustitucionAncestro).entrySet()) {
                    resultado.putIfAbsent(entrada.getKey(), entrada.getValue());
                }
            }
        }
        return resultado;
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
}
