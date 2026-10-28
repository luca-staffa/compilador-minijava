package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.TablaDeSimbolos;

/** Sentencia vacia. */
public class NodoSentenciaVacia extends NodoSentencia {

    public NodoSentenciaVacia(Token token) {
        super(token);
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        // Nada que controlar.
    }
}
