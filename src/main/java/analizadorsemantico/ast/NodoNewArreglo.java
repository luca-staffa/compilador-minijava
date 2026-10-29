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

/**
 * Creacion de arreglos con una o mas dimensiones. El token base es la palabra
 * reservada {@code new}; el token del tipo base se conserva aparte.
 */
public class NodoNewArreglo extends NodoExpresion {

    private final Token tokenTipo;
    private final Tipo tipoBase;
    private final List<NodoExpresion> tamanios;

    public NodoNewArreglo(Token tokenNew, Token tokenTipo, Tipo tipoBase,
                          List<NodoExpresion> tamanios) {
        super(tokenNew);
        this.tokenTipo = tokenTipo;
        this.tipoBase = tipoBase;
        this.tamanios = tamanios;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        SemanticaUtil.validarTipo(tipoBase, ts, tokenTipo);
        if (tipoBase instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) tipoBase;
            Entidad entidad = ts.getTipo(referencia.getNombreReferencia());
            if (entidad != null && entidad.getParametroGenerico() != null
                    && referencia.getArgumento() == null) {
                errorEn(tokenTipo, "el tipo " + referencia.getNombreReferencia()
                        + " es generico y requiere un argumento de tipo");
            }
        }
        for (NodoExpresion tamanio : tamanios) {
            InfoCheck info = tamanio.chequear(ts);
            if (!esInt(info.getTipo())) {
                errorEn(tamanio.getToken(), "el tamanio de una dimension debe ser int");
            }
        }
        return InfoCheck.valor(new TipoArreglo(tipoBase, tamanios.size(), tokenTipo));
    }
}
