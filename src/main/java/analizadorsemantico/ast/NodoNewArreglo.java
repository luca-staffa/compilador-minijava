package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Entidad;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;
import analizadorsemantico.TipoReferencia;

/** Creacion de arreglos con una o mas dimensiones. */
public class NodoNewArreglo extends NodoExpresion {

    private final Tipo tipoBase;
    private final List<NodoExpresion> tamanios;

    public NodoNewArreglo(Token token, Tipo tipoBase, List<NodoExpresion> tamanios) {
        super(token);
        this.tipoBase = tipoBase;
        this.tamanios = tamanios;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        SemanticaUtil.validarTipo(tipoBase, ts, token);
        if (tipoBase instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) tipoBase;
            Entidad entidad = ts.getTipo(referencia.getNombreReferencia());
            if (entidad != null && entidad.getParametroGenerico() != null
                    && referencia.getArgumento() == null) {
                error("el tipo " + referencia.getNombreReferencia()
                        + " es generico y requiere un argumento de tipo");
            }
        }
        for (NodoExpresion tamanio : tamanios) {
            InfoCheck info = tamanio.chequear(ts);
            if (!esInt(info.getTipo())) {
                errorEn(tamanio.getToken(), "el tamanio de una dimension debe ser int");
            }
        }
        return InfoCheck.valor(new TipoArreglo(tipoBase, tamanios.size(), token));
    }
}
