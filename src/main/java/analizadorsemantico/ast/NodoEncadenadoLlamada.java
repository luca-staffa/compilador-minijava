package analizadorsemantico.ast;

import java.util.List;
import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Entidad;
import analizadorsemantico.Generador;
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

    private Metodo metodo;

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
            metodo = metodoToString(ts);
            tipoRetorno = new TipoReferencia("String", null, token);
        } else if (receptor instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) receptor;
            Entidad entidad = ts.getTipo(referencia.getNombreReferencia());
            if (entidad == null) {
                errorEn(token, "el tipo " + referencia.getNombreReferencia()
                        + " no esta declarado");
            }
            Metodo resuelto = SemanticaUtil.buscarMetodo(entidad, nombre, argumentos.size());
            if (resuelto == null && entidad instanceof analizadorsemantico.Interfaz) {
                // Las interfaces ofrecen los metodos de instancia de Object (toString),
                // pero no sus metodos estaticos ni los de la clase que las implementa.
                resuelto = metodoDeObject(ts, nombre, argumentos.size());
            }
            if (resuelto == null) {
                errorEn(token, "el metodo " + nombre + " con " + argumentos.size()
                        + " parametro(s) no existe en " + referencia.getNombreReferencia());
            }
            Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(entidad, referencia);
            Metodo vista = resuelto.instanciar(sustitucion);
            NodoExpresion.chequearArgumentos(ts, vista.getParametros(), argumentos,
                    tokenNombre, "el metodo " + nombre);
            metodo = vista;
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

    @Override
    public void generar(Generador g, boolean destino) {
        // El tope de la pila contiene la referencia al receptor.
        boolean dejaValor = Generador.dejaValor(metodo);
        if (metodo.esEstatico()) {
            // Llamada estatica mediante una expresion receptora: se descarta.
            g.emit("POP", "descarta el receptor de la llamada estatica");
            if (dejaValor) {
                g.emit("RMEM 1", "reserva la celda de retorno");
            }
            for (NodoExpresion argumento : argumentos) {
                argumento.generar(g);
            }
            g.emit("PUSH " + metodo.getEtiqueta(), "apila el metodo " + nombre);
            g.emit("CALL", "llama al metodo en el tope de la pila");
        } else {
            if (dejaValor) {
                g.emit("RMEM 1", "reserva la celda de retorno");
                g.emit("SWAP");
            }
            for (NodoExpresion argumento : argumentos) {
                argumento.generar(g);
                g.emit("SWAP");
            }
            g.emit("DUP", "conserva la referencia al receptor");
            g.emit("LOADREF 0", "apila la referencia a la VT");
            g.emit("LOADREF " + metodo.getOffset(), "apila el metodo " + nombre);
            g.emit("CALL", "llama al metodo en el tope de la pila");
        }
        if (siguiente != null) {
            siguiente.generar(g, destino);
        }
    }

    private static Metodo metodoToString(TablaDeSimbolos ts) {
        return metodoDeObject(ts, "toString", 0);
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
