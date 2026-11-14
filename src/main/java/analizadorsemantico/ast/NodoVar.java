package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Atributo;
import analizadorsemantico.Clase;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Parametro;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Unidad;
import analizadorsemantico.VariableLocal;

/** Acceso a una variable sin receptor explicito (local, parametro o atributo). */
public class NodoVar extends NodoExpresion {

    private final String nombre;

    private VariableLocal local;
    private Parametro parametro;
    private Atributo atributo;

    public NodoVar(Token token) {
        super(token);
        this.nombre = token.getLexema();
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        VariableLocal variable = ts.buscarVariableLocal(nombre);
        if (variable != null) {
            local = variable;
            return InfoCheck.asignable(variable.getTipo());
        }

        if (ts.getUnidadActual() instanceof Unidad) {
            Parametro formal = ((Unidad) ts.getUnidadActual()).buscarParametro(nombre);
            if (formal != null) {
                parametro = formal;
                return InfoCheck.asignable(formal.getTipo());
            }
        }

        Clase clase = ts.getClaseActual();
        if (clase != null) {
            Atributo atributoPropio = clase.getAtributos().get(nombre);
            if (atributoPropio != null) {
                if (SemanticaUtil.esContextoEstatico(ts)) {
                    error("no se puede acceder al atributo " + nombre
                            + " desde un contexto estatico");
                }
                atributo = atributoPropio;
                return InfoCheck.asignable(atributoPropio.getTipo());
            }
        }

        error("la variable " + nombre + " no fue declarada");
        return null;
    }

    @Override
    public void generar(Generador g) {
        if (local != null) {
            g.emit("LOAD " + local.getOffset(), "apila la variable local " + nombre);
        } else if (parametro != null) {
            g.emit("LOAD " + parametro.getOffset(), "apila el parametro " + nombre);
        } else {
            g.emit("LOAD 3", "apila la referencia a this");
            g.emit("LOADREF " + atributo.getOffset(), "apila el atributo " + nombre);
        }
    }

    @Override
    public void generarDestino(Generador g) {
        if (local != null) {
            g.emit("STORE " + local.getOffset(), "guarda en la variable local " + nombre);
        } else if (parametro != null) {
            g.emit("STORE " + parametro.getOffset(), "guarda en el parametro " + nombre);
        } else {
            g.emit("LOAD 3", "apila la referencia a this");
            g.emit("SWAP");
            g.emit("STOREREF " + atributo.getOffset(), "guarda en el atributo " + nombre);
        }
    }
}
