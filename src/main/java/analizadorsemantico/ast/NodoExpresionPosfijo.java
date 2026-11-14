package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.TipoPrimitivo;

/**
 * Expresion posfija ++ / --. En MiniJava solo se admite un operando asignable
 * de tipo int.
 */
public class NodoExpresionPosfijo extends NodoExpresion {

    private final Token operador;
    private final NodoExpresion operando;

    public NodoExpresionPosfijo(Token operador, NodoExpresion operando) {
        super(operador);
        this.operador = operador;
        this.operando = operando;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck info = operando.chequear(ts);
        if (!info.esAsignable()) {
            error("el operando de " + operador.getLexema() + " debe ser un destino asignable");
        }
        if (!esInt(info.getTipo())) {
            error("el operador " + operador.getLexema()
                    + " solo se admite sobre operandos int");
        }
        return InfoCheck.valor(new TipoPrimitivo(TipoPrimitivo.Base.INT, operador));
    }

    @Override
    public void generar(Generador g) {
        boolean incremento = operador.getNombre().equals("op++");
        // El valor de la expresion es el valor previo del operando.
        operando.generar(g);
        g.emit("DUP", "valor previo del operando");
        g.emit("PUSH 1");
        g.emit(incremento ? "ADD" : "SUB");
        operando.generarDestino(g);
    }
}
