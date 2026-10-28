package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoPrimitivo;

/** Expresion unaria prefija. */
public class NodoExpresionUnaria extends NodoExpresion {

    private final Token operador;
    private final NodoExpresion operando;

    public NodoExpresionUnaria(Token operador, NodoExpresion operando) {
        super(operador);
        this.operador = operador;
        this.operando = operando;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck info = operando.chequear(ts);
        Tipo tipo = info.getTipo();
        if (operador.getNombre().equals("op!")) {
            if (!esBoolean(tipo)) {
                error("el operador ! requiere un operando boolean y se encontro "
                        + tipo.getNombre());
            }
            return InfoCheck.valor(new TipoPrimitivo(TipoPrimitivo.Base.BOOLEAN, operador));
        }
        if (!esInt(tipo)) {
            error("el operador " + operador.getLexema()
                    + " requiere un operando int y se encontro " + tipo.getNombre());
        }
        return InfoCheck.valor(new TipoPrimitivo(TipoPrimitivo.Base.INT, operador));
    }
}
