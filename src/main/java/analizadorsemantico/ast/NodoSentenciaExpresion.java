package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.TipoVoid;

/**
 * Sentencia que consiste en una expresion seguida de punto y coma. Solo es
 * valida si la expresion es una asignacion o termina en una llamada.
 */
public class NodoSentenciaExpresion extends NodoSentencia {

    private final NodoExpresion expresion;
    private boolean dejaValor;

    public NodoSentenciaExpresion(NodoExpresion expresion) {
        super(expresion.getToken());
        this.expresion = expresion;
    }

    public NodoExpresion getExpresion() {
        return expresion;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        InfoCheck info = expresion.chequear(ts);
        dejaValor = !(info.getTipo() instanceof TipoVoid);
        if (expresion instanceof NodoExpresionAsignacion) {
            return;
        }
        if (info.esLlamada()) {
            return;
        }
        errorEn(expresion.getTokenSentencia(),
                "la expresion no es una asignacion ni una llamada valida como sentencia");
    }

    @Override
    public void generar(Generador g) {
        expresion.generar(g);
        if (dejaValor) {
            g.emit("POP", "descarta el valor de la sentencia");
        }
    }
}
