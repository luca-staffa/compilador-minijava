package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;

/** Sentencia if con rama else opcional. */
public class NodoIf extends NodoSentencia {

    private final NodoExpresion condicion;
    private final NodoSentencia entonces;
    private final NodoSentencia sino;

    public NodoIf(Token token, NodoExpresion condicion, NodoSentencia entonces, NodoSentencia sino) {
        super(token);
        this.condicion = condicion;
        this.entonces = entonces;
        this.sino = sino;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        InfoCheck info = condicion.chequear(ts);
        if (!NodoExpresion.esBoolean(info.getTipo())) {
            errorEn(condicion.getToken(), "la condicion del if debe ser de tipo boolean");
        }
        chequearRama(entonces, ts);
        if (sino != null) {
            chequearRama(sino, ts);
        }
    }

    private void chequearRama(NodoSentencia sentencia, TablaDeSimbolos ts) {
        if (sentencia instanceof NodoVarLocal) {
            errorEn(((NodoVarLocal) sentencia).getTokenNombre(),
                    "no se puede declarar una variable local como cuerpo directo de un if");
        }
        sentencia.chequear(ts);
    }
}
