package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Constructor;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Metodo;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoVoid;

/** Sentencia return, con expresion opcional. */
public class NodoReturn extends NodoSentencia {

    private final NodoExpresion expresion;

    public NodoReturn(Token token, NodoExpresion expresion) {
        super(token);
        this.expresion = expresion;
    }

    @Override
    public void chequear(TablaDeSimbolos ts) {
        if (ts.getUnidadActual() instanceof Constructor) {
            if (expresion != null) {
                errorEn(expresion.getTokenValor(),
                        "un constructor no puede retornar una expresion");
            }
            return;
        }
        if (!(ts.getUnidadActual() instanceof Metodo)) {
            return;
        }
        Metodo metodo = (Metodo) ts.getUnidadActual();
        Tipo tipoRetorno = metodo.getTipoRetorno();
        if (tipoRetorno instanceof TipoVoid) {
            if (expresion != null) {
                errorEn(expresion.getTokenValor(),
                        "un metodo void solo admite return; sin expresion");
            }
            return;
        }
        if (expresion == null) {
            error("el metodo " + metodo.getNombre()
                    + " requiere una expresion en el return");
        }
        InfoCheck info = expresion.chequear(ts);
        if (!SemanticaUtil.conforma(info.getTipo(), tipoRetorno, ts)) {
            errorEn(expresion.getTokenValor(), "el tipo " + info.getTipo().getNombre()
                    + " no conforma con el tipo de retorno "
                    + tipoRetorno.getNombre());
        }
    }
}
