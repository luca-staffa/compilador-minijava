package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Entidad;
import analizadorsemantico.ExcepcionSemantica;
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
        if (tamanios.size() == 1) {
            generarUnaDimension(g);
        } else if (tamanios.size() == 2) {
            generarDosDimensiones(g);
        } else {
            throw new ExcepcionSemantica(token.getLexema(), token.getNroLinea(),
                    "la generacion de codigo solo admite arreglos de hasta dos dimensiones");
        }
    }

    /** new T[E]: deja la referencia al arreglo creado en el tope de la pila. */
    private void generarUnaDimension(Generador g) {
        int nTemp = g.reservarTemporal();
        tamanios.get(0).generar(g);
        g.emit("STORE " + nTemp, "guarda la longitud");
        g.emit("RMEM 1", "celda del resultado");
        g.emit("LOAD " + nTemp);
        g.emit("PUSH 2", "salta la VT y la longitud");
        g.emit("ADD");
        g.emit("PUSH simple_malloc");
        g.emit("CALL");
        g.emit("DUP");
        g.emit("PUSH lblVTObject", "VT del arreglo");
        g.emit("STOREREF 0");
        g.emit("DUP");
        g.emit("LOAD " + nTemp);
        g.emit("STOREREF 1", "inicializa la longitud");
    }

    /** new T[E1][E2]: crea el arreglo exterior y un subarreglo por componente. */
    private void generarDosDimensiones(Generador g) {
        int dimTemp = g.reservarTemporal();
        int subTemp = g.reservarTemporal();
        int refTemp = g.reservarTemporal();
        int iTemp = g.reservarTemporal();

        tamanios.get(0).generar(g);
        g.emit("STORE " + dimTemp, "guarda la primera dimension");
        tamanios.get(1).generar(g);
        g.emit("STORE " + subTemp, "guarda la segunda dimension");

        // Arreglo exterior.
        g.emit("RMEM 1", "celda del resultado");
        g.emit("LOAD " + dimTemp);
        g.emit("PUSH 2");
        g.emit("ADD");
        g.emit("PUSH simple_malloc");
        g.emit("CALL");
        g.emit("DUP");
        g.emit("PUSH lblVTObject");
        g.emit("STOREREF 0");
        g.emit("DUP");
        g.emit("LOAD " + dimTemp);
        g.emit("STOREREF 1");
        g.emit("STORE " + refTemp, "guarda la referencia al arreglo exterior");

        g.emit("PUSH 0");
        g.emit("STORE " + iTemp);
        String etiquetaInicio = g.nuevaEtiqueta("lArrIni");
        String etiquetaFin = g.nuevaEtiqueta("lArrFin");
        g.etiqueta(etiquetaInicio, "NOP");
        g.emit("LOAD " + iTemp);
        g.emit("LOAD " + dimTemp);
        g.emit("LT");
        g.emit("BF " + etiquetaFin);

        // Subarreglo de la fila i.
        g.emit("RMEM 1", "celda del resultado del subarreglo");
        g.emit("LOAD " + subTemp);
        g.emit("PUSH 2");
        g.emit("ADD");
        g.emit("PUSH simple_malloc");
        g.emit("CALL");
        g.emit("DUP");
        g.emit("PUSH lblVTObject");
        g.emit("STOREREF 0");
        g.emit("DUP");
        g.emit("LOAD " + subTemp);
        g.emit("STOREREF 1");
        // Almacena la referencia del subarreglo en refTemp[i].
        g.emit("LOAD " + refTemp);
        g.emit("LOAD " + iTemp);
        g.emit("ADD");
        g.emit("SWAP");
        g.emit("STOREREF 2");

        g.emit("LOAD " + iTemp);
        g.emit("PUSH 1");
        g.emit("ADD");
        g.emit("STORE " + iTemp);
        g.emit("JUMP " + etiquetaInicio);
        g.etiqueta(etiquetaFin, "NOP");

        g.emit("LOAD " + refTemp, "referencia al arreglo creado");
    }
}
