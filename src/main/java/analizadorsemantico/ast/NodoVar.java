package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Atributo;
import analizadorsemantico.Clase;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Parametro;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Unidad;
import analizadorsemantico.VariableLocal;

/** Acceso a una variable sin receptor explicito (local, parametro o atributo). */
public class NodoVar extends NodoExpresion {

    private final String nombre;

    public NodoVar(Token token) {
        super(token);
        this.nombre = token.getLexema();
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        VariableLocal local = ts.buscarVariableLocal(nombre);
        if (local != null) {
            return InfoCheck.asignable(local.getTipo());
        }

        if (ts.getUnidadActual() instanceof Unidad) {
            Parametro parametro = ((Unidad) ts.getUnidadActual()).buscarParametro(nombre);
            if (parametro != null) {
                return InfoCheck.asignable(parametro.getTipo());
            }
        }

        Clase clase = ts.getClaseActual();
        if (clase != null) {
            Atributo atributo = clase.getAtributos().get(nombre);
            if (atributo != null) {
                if (SemanticaUtil.esContextoEstatico(ts)) {
                    error("no se puede acceder al atributo " + nombre
                            + " desde un contexto estatico");
                }
                return InfoCheck.asignable(atributo.getTipo());
            }
        }

        error("la variable " + nombre + " no fue declarada");
        return null;
    }
}
