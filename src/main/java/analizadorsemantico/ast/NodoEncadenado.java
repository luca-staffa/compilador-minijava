package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.ExcepcionSemantica;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;

/**
 * Eslabon de un encadenado (acceso a atributo, llamada o indexacion). Cada
 * eslabon recibe el tipo estatico del receptor, lo controla y delega en el
 * eslabon siguiente.
 */
public abstract class NodoEncadenado {

    protected final Token token;
    protected final NodoEncadenado siguiente;

    protected NodoEncadenado(Token token, NodoEncadenado siguiente) {
        this.token = token;
        this.siguiente = siguiente;
    }

    public Token getToken() {
        return token;
    }

    public NodoEncadenado getSiguiente() {
        return siguiente;
    }

    public abstract InfoCheck chequear(TablaDeSimbolos ts, Tipo receptor);

    protected static void errorEn(Token tokenDelError, String mensaje) {
        throw new ExcepcionSemantica(
                tokenDelError.getLexema(), tokenDelError.getNroLinea(), mensaje);
    }
}
