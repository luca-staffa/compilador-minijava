package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Bloque;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoNull;
import analizadorsemantico.TipoVoid;
import analizadorsemantico.Unidad;
import analizadorsemantico.VariableLocal;

/** Declaracion de variable local con inferencia de tipo. */
public class NodoVarLocal extends NodoSentencia {

    private final Token tokenNombre;
    private final NodoExpresion inicializador;

    public NodoVarLocal(Token tokenNombre, NodoExpresion inicializador) {
        super(tokenNombre);
        this.tokenNombre = tokenNombre;
        this.inicializador = inicializador;
    }

    public Token getTokenNombre() {
        return tokenNombre;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        String nombre = tokenNombre.getLexema();

        if (ts.getUnidadActual() instanceof Unidad) {
            Unidad unidad = (Unidad) ts.getUnidadActual();
            if (unidad.buscarParametro(nombre) != null) {
                errorEn(tokenNombre, "la variable local " + nombre
                        + " coincide con un parametro de la unidad");
            }
        }
        if (ts.buscarVariableLocal(nombre) != null) {
            errorEn(tokenNombre, "la variable local " + nombre
                    + " ya fue declarada en un bloque visible");
        }

        InfoCheck info = inicializador.chequear(ts);
        Tipo tipo = info.getTipo();
        if (tipo instanceof TipoVoid) {
            errorEn(tokenNombre, "no se puede inferir el tipo de una llamada void");
        }
        if (tipo instanceof TipoNull) {
            errorEn(tokenNombre, "no se puede inferir el tipo de la variable " + nombre
                    + " a partir de null");
        }

        Bloque bloque = ts.getBloqueActual();
        bloque.agregarVariable(new VariableLocal(tokenNombre, tipo, bloque, ts));
    }
}
