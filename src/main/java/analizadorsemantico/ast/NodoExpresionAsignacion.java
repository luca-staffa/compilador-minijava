package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoVoid;

/** Asignacion. Produce un valor del tipo del destino, pero no es asignable. */
public class NodoExpresionAsignacion extends NodoExpresion {

    private final NodoExpresion izquierda;
    private final NodoExpresion derecha;

    public NodoExpresionAsignacion(Token token, NodoExpresion izquierda, NodoExpresion derecha) {
        super(token);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        InfoCheck infoIzquierda = izquierda.chequear(ts);
        InfoCheck infoDerecha = derecha.chequear(ts);
        Tipo tipoIzquierda = infoIzquierda.getTipo();
        Tipo tipoDerecha = infoDerecha.getTipo();

        if (!infoIzquierda.esAsignable()) {
            error("el lado izquierdo de la asignacion no es asignable");
        }
        if (tipoIzquierda instanceof TipoVoid || tipoDerecha instanceof TipoVoid) {
            error("no se puede asignar un valor de tipo void");
        }
        if (!SemanticaUtil.conforma(tipoDerecha, tipoIzquierda, ts)) {
            error("los tipos son incompatibles en la asignacion: "
                    + tipoDerecha.getNombre() + " no conforma con "
                    + tipoIzquierda.getNombre());
        }
        return InfoCheck.valor(tipoIzquierda);
    }
}
