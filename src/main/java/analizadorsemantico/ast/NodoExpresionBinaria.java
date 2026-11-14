package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
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

    @Override
    public void generar(Generador g) {
        izquierda.generar(g);
        derecha.generar(g);
        g.emit(instruccion(), "operador " + operador.getLexema());
    }

    private String instruccion() {
        switch (operador.getNombre()) {
            case "op+": return "ADD";
            case "op-": return "SUB";
            case "op*": return "MUL";
            case "op/": return "DIV";
            case "op%": return "MOD";
            case "op<": return "LT";
            case "op>": return "GT";
            case "op<=": return "LE";
            case "op>=": return "GE";
            case "op&&": return "AND";
            case "op||": return "OR";
            case "op==": return "EQ";
            case "op!=": return "NE";
            default: throw new IllegalStateException("operador binario desconocido");
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
