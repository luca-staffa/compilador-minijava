package analizadorsemantico.ast;

import analizadorlexico.Token;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;

/** Indexacion de un arreglo. */
public class NodoEncadenadoArreglo extends NodoEncadenado {

    private final NodoExpresion indice;

    public NodoEncadenadoArreglo(Token tokenCorchete, NodoExpresion indice,
                                 NodoEncadenado siguiente) {
        super(tokenCorchete, siguiente);
        this.indice = indice;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts, Tipo receptor) {
        if (!(receptor instanceof TipoArreglo)) {
            errorEn(token, "no se puede indexar un valor de tipo " + receptor.getNombre());
        }
        InfoCheck infoIndice = indice.chequear(ts);
        if (!NodoExpresion.esInt(infoIndice.getTipo())) {
            errorEn(token, "el indice de un arreglo debe ser int");
        }
        TipoArreglo arreglo = (TipoArreglo) receptor;
        Tipo componente = arreglo.getDimensiones() == 1
                ? arreglo.getBase()
                : new TipoArreglo(arreglo.getBase(), arreglo.getDimensiones() - 1,
                        arreglo.getToken());
        if (siguiente != null) {
            return siguiente.chequear(ts, componente);
        }
        return InfoCheck.asignable(componente);
    }

    @Override
    public void generar(Generador g, boolean destino) {
        // El tope de la pila contiene la referencia al arreglo.
        indice.generar(g);
        g.emit("ADD", "suma el indice a la referencia");
        if (destino && siguiente == null) {
            g.emit("SWAP");
            g.emit("STOREREF 2", "guarda en la componente del arreglo");
            return;
        }
        g.emit("LOADREF 2", "apila la componente del arreglo");
        if (siguiente != null) {
            siguiente.generar(g, destino);
        }
    }
}
