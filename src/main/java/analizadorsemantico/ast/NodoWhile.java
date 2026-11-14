package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
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
            errorEn(condicion.getTokenValor(), "la condicion del while debe ser de tipo boolean");
        }
        if (cuerpo instanceof NodoVarLocal) {
            errorEn(((NodoVarLocal) cuerpo).getTokenVar(),
                    "no se puede declarar una variable local como cuerpo directo de un while");
        }
        cuerpo.chequear(ts);
    }

    @Override
    public void generar(Generador g) {
        String etiquetaInicio = g.nuevaEtiqueta("lWhile");
        String etiquetaFin = g.nuevaEtiqueta("lFin");
        g.etiqueta(etiquetaInicio, "NOP");
        condicion.generar(g);
        g.emit("BF " + etiquetaFin, "sale si la condicion es falsa");
        g.generarCuerpo(cuerpo);
        g.emit("JUMP " + etiquetaInicio);
        g.etiqueta(etiquetaFin, "NOP");
    }
}
