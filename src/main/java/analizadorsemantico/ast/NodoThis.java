package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;

/** Expresion this. Solo valida en metodos de instancia y constructores. */
public class NodoThis extends NodoExpresion {

    public NodoThis(Token token) {
        super(token);
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        if (SemanticaUtil.esContextoEstatico(ts)) {
            error("this no puede utilizarse en un contexto estatico");
        }
        Tipo tipo = SemanticaUtil.tipoDeClaseActual(ts);
        if (tipo == null) {
            error("this no puede utilizarse fuera de una clase");
        }
        return InfoCheck.valor(tipo);
    }
}
