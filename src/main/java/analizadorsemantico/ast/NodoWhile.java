package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;

/** Sentencia while. */
public class NodoWhile extends NodoSentencia {

    private final NodoExpresion condicion;
    private final NodoSentencia cuerpo;

    public NodoWhile(Token token, NodoExpresion condicion, NodoSentencia cuerpo) {
        super(token);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        InfoCheck info = condicion.chequear(ts);
        if (!NodoExpresion.esBoolean(info.getTipo())) {
            errorEn(condicion.getToken(), "la condicion del while debe ser de tipo boolean");
        }
        if (cuerpo instanceof NodoVarLocal) {
            errorEn(((NodoVarLocal) cuerpo).getTokenNombre(),
                    "no se puede declarar una variable local como cuerpo directo de un while");
        }
        cuerpo.chequear(ts);
    }
}
