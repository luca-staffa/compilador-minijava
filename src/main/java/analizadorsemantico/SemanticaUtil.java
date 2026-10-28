package analizadorsemantico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import analizadorlexico.Token;

/**
 * Operaciones semanticas compartidas por el chequeo de declaraciones y el
 * chequeo de sentencias: conformidad de tipos, vistas de miembros bajo
 * instanciacion generica y contexto de la unidad actual.
 */
public final class SemanticaUtil {

    private SemanticaUtil() {
    }

    // ------------------------------------------------------------------
    // Conformidad de tipos (origen conforma con destino)
    // ------------------------------------------------------------------

    public static boolean conforma(Tipo origen, Tipo destino, TablaDeSimbolos ts) {
        if (origen == null || destino == null) {
            return false;
        }
        if (origen instanceof TipoNull) {
            return destino instanceof TipoNull
                    || destino.esReferencia()
                    || destino.esParametro()
                    || destino.esArreglo();
        }
        if (destino instanceof TipoNull) {
            return origen instanceof TipoNull;
        }
        if (origen instanceof TipoVoid || destino instanceof TipoVoid) {
            return false;
        }
        if (origen instanceof TipoPrimitivo || destino instanceof TipoPrimitivo) {
            return origen.esIgualA(destino);
        }
        if (origen instanceof TipoParametro || destino instanceof TipoParametro) {
            return origen.esIgualA(destino);
        }
        if (destino instanceof TipoArreglo) {
            return origen instanceof TipoArreglo && origen.esIgualA(destino);
        }
        if (origen instanceof TipoArreglo) {
            return esObject(destino);
        }
        if (origen instanceof TipoReferencia && destino instanceof TipoReferencia) {
            return conformaReferencia((TipoReferencia) origen, (TipoReferencia) destino, ts);
        }
        return false;
    }

    private static boolean esObject(Tipo destino) {
        return destino instanceof TipoReferencia
                && ((TipoReferencia) destino).getNombreReferencia().equals("Object")
                && ((TipoReferencia) destino).getArgumento() == null;
    }

    private static boolean conformaReferencia(TipoReferencia origen, TipoReferencia destino,
                                              TablaDeSimbolos ts) {
        TipoReferencia actual = origen;
        Set<String> visitados = new HashSet<>();
        while (actual != null) {
            if (actual.esIgualA(destino)) {
                return true;
            }
            if (esObject(destino)) {
                return true;
            }
            if (!visitados.add(actual.getNombreReferencia())) {
                return false;
            }
            Entidad entidad = ts.getTipo(actual.getNombreReferencia());
            if (entidad == null) {
                return false;
            }
            TipoReferencia ancestro = ancestroDe(entidad);
            if (ancestro == null) {
                return false;
            }
            Map<String, Tipo> sustitucion = sustitucionVista(entidad, actual);
            actual = instanciarAncestro(ancestro, sustitucion, ts);
        }
        return false;
    }

    private static TipoReferencia ancestroDe(Entidad entidad) {
        if (entidad instanceof Clase) {
            return ((Clase) entidad).getAncestro();
        }
        if (entidad instanceof Interfaz) {
            return ((Interfaz) entidad).getAncestro();
        }
        return null;
    }

    private static TipoReferencia instanciarAncestro(TipoReferencia ancestro,
                                                     Map<String, Tipo> sustitucion,
                                                     TablaDeSimbolos ts) {
        if (ancestro.getArgumento() != null) {
            return new TipoReferencia(
                    ancestro.getNombreReferencia(),
                    ancestro.getArgumento().sustituir(sustitucion),
                    ancestro.getToken());
        }
        Entidad entidad = ts.getTipo(ancestro.getNombreReferencia());
        String parametro = entidad == null ? null : entidad.getParametroGenerico();
        Tipo argumento = parametro == null ? null : new TipoParametro(parametro, ancestro.getToken());
        return new TipoReferencia(ancestro.getNombreReferencia(), argumento, ancestro.getToken());
    }

    // ------------------------------------------------------------------
    // Vistas con instanciacion generica
    // ------------------------------------------------------------------

    /** Mapea el parametro de tipo de la entidad al argumento del receptor. */
    public static Map<String, Tipo> sustitucionVista(Entidad entidad, TipoReferencia receptor) {
        Map<String, Tipo> sustitucion = new HashMap<>();
        String parametro = entidad.getParametroGenerico();
        if (parametro != null) {
            Tipo argumento = receptor.getArgumento();
            sustitucion.put(parametro,
                    argumento != null ? argumento : new TipoParametro(parametro, receptor.getToken()));
        }
        return sustitucion;
    }

    public static Metodo buscarMetodo(Entidad entidad, String nombre, int aridad) {
        String clave = nombre + "/" + aridad;
        if (entidad instanceof Clase) {
            return ((Clase) entidad).getMetodos().get(clave);
        }
        if (entidad instanceof Interfaz) {
            return ((Interfaz) entidad).getMetodos().get(clave);
        }
        return null;
    }

    public static Atributo buscarAtributo(Entidad entidad, String nombre) {
        if (entidad instanceof Clase) {
            return ((Clase) entidad).getAtributos().get(nombre);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Contexto
    // ------------------------------------------------------------------

    public static boolean esContextoEstatico(TablaDeSimbolos ts) {
        Entidad unidad = ts.getUnidadActual();
        return unidad instanceof Metodo && ((Metodo) unidad).esEstatico();
    }

    /** Tipo de this / de la clase actual, conservando su parametro si lo tiene. */
    public static TipoReferencia tipoDeClaseActual(TablaDeSimbolos ts) {
        Clase clase = ts.getClaseActual();
        if (clase == null) {
            return null;
        }
        String parametro = clase.getParametroGenerico();
        Tipo argumento = parametro == null
                ? null
                : new TipoParametro(parametro, clase.getToken());
        return new TipoReferencia(clase.getNombre(), argumento, clase.getToken());
    }

    /** Indica si el tipo menciona el parametro de tipo indicado. */
    public static boolean contieneParametro(Tipo tipo, String nombreParametro) {
        if (tipo == null || nombreParametro == null) {
            return false;
        }
        if (tipo instanceof TipoParametro) {
            return ((TipoParametro) tipo).getNombreParametro().equals(nombreParametro);
        }
        if (tipo instanceof TipoArreglo) {
            return contieneParametro(((TipoArreglo) tipo).getBase(), nombreParametro);
        }
        if (tipo instanceof TipoReferencia) {
            Tipo argumento = ((TipoReferencia) tipo).getArgumento();
            return contieneParametro(argumento, nombreParametro);
        }
        return false;
    }

    /** Valida un tipo en el contexto de la unidad/clase actual. */
    public static void validarTipo(Tipo tipo, TablaDeSimbolos ts, Token tokenDelError) {
        if (tipo == null) {
            return;
        }
        try {
            tipo.esValidoEn(ts, parametroDeContexto(ts));
        } catch (ExcepcionSemantica e) {
            throw new ExcepcionSemantica(
                    tokenDelError.getLexema(), tokenDelError.getNroLinea(), e.getMessage());
        }
        if (esContextoEstatico(ts)) {
            String parametro = parametroDeContexto(ts);
            if (parametro != null && contieneParametro(tipo, parametro)) {
                throw new ExcepcionSemantica(
                        tokenDelError.getLexema(),
                        tokenDelError.getNroLinea(),
                        "no se puede usar el parametro de tipo " + parametro
                                + " en un contexto estatico");
            }
        }
    }

    public static String parametroDeContexto(TablaDeSimbolos ts) {
        Entidad actual = ts.getClaseActual() != null
                ? ts.getClaseActual()
                : ts.getInterfazActual();
        return actual == null ? null : actual.getParametroGenerico();
    }
}
