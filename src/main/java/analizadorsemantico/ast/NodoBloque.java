package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.TablaDeSimbolos;

/** Bloque de sentencias: establece un ambito lexico al entrar y lo restaura al salir. */
public class NodoBloque extends NodoSentencia {

    private final List<NodoSentencia> sentencias;

    public NodoBloque(Token token, List<NodoSentencia> sentencias) {
        super(token);
        this.sentencias = sentencias;
    }

    public List<NodoSentencia> getSentencias() {
        return sentencias;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        ts.entrarBloque();
        try {
            for (NodoSentencia sentencia : sentencias) {
                sentencia.chequear(ts);
            }
        } finally {
            ts.salirBloque();
        }
    }
}
