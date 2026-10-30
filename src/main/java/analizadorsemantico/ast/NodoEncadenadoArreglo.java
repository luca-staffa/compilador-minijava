package analizadorsemantico.ast;

import analizadorlexico.Token;
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
}
