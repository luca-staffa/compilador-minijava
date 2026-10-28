package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;

/**
 * Referencia: un primario seguido de un encadenado opcional. Si el primario es
 * una expresion parentizada, el resultado conserva el tipo pero pierde las
 * propiedades estructurales.
 */
public class NodoReferencia extends NodoExpresion {

    private final NodoExpresion primario;
    private final NodoEncadenado encadenado;

    public NodoReferencia(Token token, NodoExpresion primario, NodoEncadenado encadenado) {
        super(token);
        this.primario = primario;
        this.encadenado = encadenado;
    }

    public NodoExpresion getPrimario() {
        return primario;
    }

    public NodoEncadenado getEncadenado() {
        return encadenado;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck infoPrimario = primario.chequear(ts);
        if (encadenado == null) {
            return infoPrimario;
        }
        InfoCheck info = encadenado.chequear(ts, infoPrimario.getTipo());
        if (primario instanceof NodoExpresionParentizada) {
            return InfoCheck.valor(info.getTipo());
        }
        return info;
    }
}
