package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.ExcepcionSemantica;
import analizadorsemantico.TablaDeSimbolos;

/** Nodo de sentencia del AST. Las sentencias no tienen tipo. */
public abstract class NodoSentencia {

    protected final Token token;

    protected NodoSentencia(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public abstract void chequear(TablaDeSimbolos ts);

    protected void error(String mensaje) {
        throw new ExcepcionSemantica(token.getLexema(), token.getNroLinea(), mensaje);
    }

    protected static void errorEn(Token tokenDelError, String mensaje) {
        throw new ExcepcionSemantica(
                tokenDelError.getLexema(), tokenDelError.getNroLinea(), mensaje);
    }
}
