package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoPrimitivo;

/** Expresion binaria. */
public class NodoExpresionBinaria extends NodoExpresion {

    private final Token operador;
    private final NodoExpresion izquierda;
    private final NodoExpresion derecha;

    public NodoExpresionBinaria(Token operador, NodoExpresion izquierda, NodoExpresion derecha) {
        super(operador);
        this.operador = operador;
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck infoIzquierda = izquierda.chequear(ts);
        InfoCheck infoDerecha = derecha.chequear(ts);
        Tipo tipoIzquierda = infoIzquierda.getTipo();
        Tipo tipoDerecha = infoDerecha.getTipo();
        String operacion = operador.getNombre();

        switch (operacion) {
            case "op+":
            case "op-":
            case "op*":
            case "op/":
            case "op%":
                exigirInt(tipoIzquierda);
                exigirInt(tipoDerecha);
                return InfoCheck.valor(primitivo(TipoPrimitivo.Base.INT));
            case "op<":
            case "op>":
            case "op<=":
            case "op>=":
                exigirInt(tipoIzquierda);
                exigirInt(tipoDerecha);
                return InfoCheck.valor(primitivo(TipoPrimitivo.Base.BOOLEAN));
            case "op&&":
            case "op||":
                exigirBoolean(tipoIzquierda);
                exigirBoolean(tipoDerecha);
                return InfoCheck.valor(primitivo(TipoPrimitivo.Base.BOOLEAN));
            case "op==":
            case "op!=":
                if (!SemanticaUtil.conforma(tipoIzquierda, tipoDerecha, ts)
                        && !SemanticaUtil.conforma(tipoDerecha, tipoIzquierda, ts)) {
                    error("los tipos " + tipoIzquierda.getNombre() + " y "
                            + tipoDerecha.getNombre()
                            + " no son comparables con el operador " + operador.getLexema());
                }
                return InfoCheck.valor(primitivo(TipoPrimitivo.Base.BOOLEAN));
            default:
                error("operador binario desconocido " + operador.getLexema());
                return null;
        }
    }

    private void exigirInt(Tipo tipo) {
        if (!esInt(tipo)) {
            error("el operador " + operador.getLexema() + " requiere operandos int y se encontro "
                    + tipo.getNombre());
        }
    }

    private void exigirBoolean(Tipo tipo) {
        if (!esBoolean(tipo)) {
            error("el operador " + operador.getLexema()
                    + " requiere operandos boolean y se encontro " + tipo.getNombre());
        }
    }

    private TipoPrimitivo primitivo(TipoPrimitivo.Base base) {
        return new TipoPrimitivo(base, operador);
    }
}
