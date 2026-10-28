package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;

/** Literal (entero, caracter, booleano, cadena o null) ya tipado. */
public class NodoLiteral extends NodoExpresion {

    private final Tipo tipo;

    public NodoLiteral(Token token, Tipo tipo) {
        super(token);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        return InfoCheck.valor(tipo);
    }
}
