package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoPrimitivo;
import analizadorsemantico.TipoReferencia;

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

    @Override
    public void generar(Generador g) {
        if (tipo instanceof TipoPrimitivo) {
            TipoPrimitivo.Base base = ((TipoPrimitivo) tipo).getBase();
            switch (base) {
                case INT:
                    g.emit("PUSH " + token.getLexema(), "apila el entero " + token.getLexema());
                    break;
                case CHAR:
                    g.emit("PUSH " + Generador.valorCaracter(token.getLexema()),
                            "apila el caracter " + token.getLexema());
                    break;
                case BOOLEAN:
                    g.emit("PUSH " + (token.getLexema().equals("true") ? 1 : 0),
                            "apila el booleano " + token.getLexema());
                    break;
                default:
                    break;
            }
        } else if (tipo instanceof TipoReferencia
                && ((TipoReferencia) tipo).getNombreReferencia().equals("String")) {
            g.generarString(Generador.desescapar(token.getLexema()));
        } else {
            g.emit("PUSH 0", "apila null");
        }
    }
}
