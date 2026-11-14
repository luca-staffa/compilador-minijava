package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Constructor;
import analizadorsemantico.Generador;
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
                errorEn(token, "un constructor no puede retornar una expresion");
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
                errorEn(token, "un metodo void solo admite return; sin expresion");
            }
            return;
        }
        if (expresion == null) {
            error("el metodo " + metodo.getNombre()
                    + " requiere una expresion en el return");
        }
        InfoCheck info = expresion.chequear(ts);
        if (!SemanticaUtil.conforma(info.getTipo(), tipoRetorno, ts)) {
            errorEn(expresion.getToken(), "el tipo " + info.getTipo().getNombre()
                    + " no conforma con el tipo de retorno "
                    + tipoRetorno.getNombre());
        }
    }

    @Override
    public void generar(Generador g) {
        boolean constructor = g.esConstructor();
        boolean dejaValor = !g.metodoVoid();
        if (constructor) {
            g.emit("LOAD 3", "copia this en la celda de retorno");
            g.emit("STORE " + g.getOffsetRetorno());
        } else if (dejaValor && expresion != null) {
            expresion.generar(g);
            g.emit("STORE " + g.getOffsetRetorno(), "guarda el valor de retorno");
        }
        g.emit("FMEM " + g.getLocalesVivas(), "libera las variables locales vivas");
        g.emit("STOREFP", "restablece el contexto del llamador");
        int m = g.esEstatico() ? g.getAridad() : g.getAridad() + 1;
        g.emit("RET " + m);
    }
}
