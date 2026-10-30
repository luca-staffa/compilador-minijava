package analizadorsemantico.ast;

import java.util.List;
import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Entidad;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Metodo;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;
import analizadorsemantico.TipoReferencia;

/** Llamada a un metodo mediante un receptor explicito. */
public class NodoEncadenadoLlamada extends NodoEncadenado {

    private final String nombre;
    private final Token tokenNombre;
    private final List<NodoExpresion> argumentos;

    public NodoEncadenadoLlamada(Token tokenNombre, List<NodoExpresion> argumentos,
                                 NodoEncadenado siguiente) {
        super(tokenNombre, siguiente);
        this.tokenNombre = tokenNombre;
        this.nombre = tokenNombre.getLexema();
        this.argumentos = argumentos;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts, Tipo receptor) {
        Tipo tipoRetorno;
        if (receptor instanceof TipoArreglo) {
            if (!nombre.equals("toString") || !argumentos.isEmpty()) {
                errorEn(token, "un arreglo solo dispone del metodo toString() sin parametros");
            }
            tipoRetorno = new TipoReferencia("String", null, token);
        } else if (receptor instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) receptor;
            Entidad entidad = ts.getTipo(referencia.getNombreReferencia());
            if (entidad == null) {
                errorEn(token, "el tipo " + referencia.getNombreReferencia()
                        + " no esta declarado");
            }
            Metodo metodo = SemanticaUtil.buscarMetodo(entidad, nombre, argumentos.size());
            if (metodo == null && entidad instanceof analizadorsemantico.Interfaz) {
                // Las interfaces ofrecen los metodos de instancia de Object (toString),
                // pero no sus metodos estaticos ni los de la clase que las implementa.
                metodo = metodoDeObject(ts, nombre, argumentos.size());
            }
            if (metodo == null) {
                errorEn(token, "el metodo " + nombre + " con " + argumentos.size()
                        + " parametro(s) no existe en " + referencia.getNombreReferencia());
            }
            Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(entidad, referencia);
            Metodo vista = metodo.instanciar(sustitucion);
            NodoExpresion.chequearArgumentos(ts, vista.getParametros(), argumentos,
                    tokenNombre, "el metodo " + nombre);
            tipoRetorno = vista.getTipoRetorno();
        } else {
            errorEn(token, "no se puede invocar el metodo " + nombre
                    + " sobre un valor de tipo " + receptor.getNombre());
            return null;
        }

        if (siguiente != null) {
            return siguiente.chequear(ts, tipoRetorno);
        }
        return InfoCheck.llamada(tipoRetorno);
    }

    /** Metodo de instancia de Object con esa clave, o null. */
    private static Metodo metodoDeObject(TablaDeSimbolos ts, String nombre, int aridad) {
        Entidad object = ts.getTipo("Object");
        if (!(object instanceof analizadorsemantico.Clase)) {
            return null;
        }
        Metodo metodo =
                ((analizadorsemantico.Clase) object).getMetodos().get(nombre + "/" + aridad);
        return metodo != null && !metodo.esEstatico() ? metodo : null;
    }
}
