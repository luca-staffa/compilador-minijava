package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Entidad;
import analizadorsemantico.Generador;
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
                errorEn(token, "el tamanio de una dimension debe ser int");
            }
        }
        return InfoCheck.valor(new TipoArreglo(tipoBase, tamanios.size(), tokenTipo));
    }

    @Override
    public void generar(Generador g) {
        int dimensiones = tamanios.size();
        int[] dimTemp = new int[dimensiones];
        for (int k = 0; k < dimensiones; k++) {
            dimTemp[k] = g.reservarTemporal();
        }

        // Se evaluan y controlan todas las dimensiones de izquierda a derecha.
        for (int k = 0; k < dimensiones; k++) {
            tamanios.get(k).generar(g);
            g.emit("STORE " + dimTemp[k], "guarda la dimension " + (k + 1));
            g.emit("LOAD " + dimTemp[k]);
            g.emit("PUSH 0");
            g.emit("LT");
            g.emit("BT " + Generador.ETIQUETA_ERROR_TAMANIO,
                    "tamanio de arreglo negativo");
        }

        int niveles = dimensiones - 1;
        int[] idxTemp = new int[niveles];
        int[] refTemp = new int[niveles];
        for (int k = 0; k < niveles; k++) {
            idxTemp[k] = g.reservarTemporal();
            refTemp[k] = g.reservarTemporal();
        }

        generarDimension(g, 0, dimTemp, idxTemp, refTemp);

        for (int k = 0; k < niveles; k++) {
            g.liberarTemporal();
            g.liberarTemporal();
        }
        for (int k = 0; k < dimensiones; k++) {
            g.liberarTemporal();
        }
    }

    /**
     * Deja en el tope de la pila la referencia al arreglo correspondiente a la
     * dimension indicada. Para las dimensiones que no son la ultima, recorre
     * sus componentes y almacena en cada una el subarreglo construido.
     */
    private void generarDimension(Generador g, int nivel, int[] dimTemp,
                                  int[] idxTemp, int[] refTemp) {
        g.emit("RMEM 1", "celda del resultado");
        g.emit("LOAD " + dimTemp[nivel]);
        g.emit("PUSH 2", "salta la VT y la longitud");
        g.emit("ADD");
        g.emit("PUSH simple_malloc");
        g.emit("CALL");
        g.emit("DUP");
        g.emit("PUSH lblVTObject", "VT del arreglo");
        g.emit("STOREREF 0");
        g.emit("DUP");
        g.emit("LOAD " + dimTemp[nivel]);
        g.emit("STOREREF 1", "inicializa la longitud");

        if (nivel == dimTemp.length - 1) {
            return;
        }

        g.emit("STORE " + refTemp[nivel], "guarda la referencia al arreglo");
        g.emit("PUSH 0");
        g.emit("STORE " + idxTemp[nivel]);
        String etiquetaInicio = g.nuevaEtiqueta("lArrIni");
        String etiquetaFin = g.nuevaEtiqueta("lArrFin");
        g.etiqueta(etiquetaInicio, "NOP");
        g.emit("LOAD " + idxTemp[nivel]);
        g.emit("LOAD " + dimTemp[nivel]);
        g.emit("LT");
        g.emit("BF " + etiquetaFin);

        generarDimension(g, nivel + 1, dimTemp, idxTemp, refTemp);
        g.emit("LOAD " + refTemp[nivel]);
        g.emit("LOAD " + idxTemp[nivel]);
        g.emit("ADD");
        g.emit("SWAP");
        g.emit("STOREREF 2", "almacena el subarreglo en la componente");

        g.emit("LOAD " + idxTemp[nivel]);
        g.emit("PUSH 1");
        g.emit("ADD");
        g.emit("STORE " + idxTemp[nivel]);
        g.emit("JUMP " + etiquetaInicio);
        g.etiqueta(etiquetaFin, "NOP");

        g.emit("LOAD " + refTemp[nivel], "referencia al arreglo creado");
    }
}
