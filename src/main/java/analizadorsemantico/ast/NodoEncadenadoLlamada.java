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
    private final List<NodoExpresion> argumentos;

    public NodoEncadenadoLlamada(Token token, List<NodoExpresion> argumentos,
                                 NodoEncadenado siguiente) {
        super(token, siguiente);
        this.nombre = token.getLexema();
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
            if (metodo == null) {
                errorEn(token, "el metodo " + nombre + " con " + argumentos.size()
                        + " parametro(s) no existe en " + referencia.getNombreReferencia());
            }
            Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(entidad, referencia);
            Metodo vista = metodo.instanciar(sustitucion);
            NodoExpresion.chequearArgumentos(ts, vista.getParametros(), argumentos,
                    token, "el metodo " + nombre);
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
}
