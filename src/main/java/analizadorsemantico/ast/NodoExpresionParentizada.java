package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;

/**
 * Expresion parentizada. Conserva el tipo de la expresion contenida, pero no
 * sus caracteristicas estructurales: no es asignable ni cuenta como llamada.
 */
public class NodoExpresionParentizada extends NodoExpresion {

    private final NodoExpresion expresion;

    public NodoExpresionParentizada(Token token, NodoExpresion expresion) {
        super(token);
        this.expresion = expresion;
    }

    public NodoExpresion getExpresion() {
        return expresion;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck info = expresion.chequear(ts);
        return InfoCheck.valor(info.getTipo());
    }
}
