package analizadorsemantico.ast;

import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Atributo;
import analizadorsemantico.Clase;
import analizadorsemantico.Entidad;
import analizadorsemantico.Generador;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoArreglo;
import analizadorsemantico.TipoPrimitivo;
import analizadorsemantico.TipoReferencia;

/** Acceso a un atributo mediante punto (incluye array.length). */
public class NodoEncadenadoAtributo extends NodoEncadenado {

    private final String nombre;

    private Atributo atributo;
    private boolean esLongitud;

    public NodoEncadenadoAtributo(Token tokenNombre, NodoEncadenado siguiente) {
        super(tokenNombre, siguiente);
        this.nombre = tokenNombre.getLexema();
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts, Tipo receptor) {
        if (receptor instanceof TipoArreglo) {
            if (!nombre.equals("length")) {
                errorEn(token, "un arreglo no tiene el atributo " + nombre);
            }
            esLongitud = true;
            Tipo tipo = new TipoPrimitivo(TipoPrimitivo.Base.INT, token);
            if (siguiente != null) {
                return siguiente.chequear(ts, tipo);
            }
            // length es de solo lectura.
            return InfoCheck.valor(tipo);
        }

        if (!(receptor instanceof TipoReferencia)) {
            errorEn(token, "no se puede acceder al atributo " + nombre
                    + " sobre un valor de tipo " + receptor.getNombre());
        }
        TipoReferencia referencia = (TipoReferencia) receptor;
        Entidad entidad = ts.getTipo(referencia.getNombreReferencia());
        if (!(entidad instanceof Clase)) {
            errorEn(token, "el tipo " + referencia.getNombreReferencia()
                    + " no declara atributos");
        }
        Clase clase = (Clase) entidad;
        Atributo resuelto = clase.getAtributos().get(nombre);
        if (resuelto == null) {
            errorEn(token, "el atributo " + nombre + " no existe en "
                    + clase.getNombre());
        }
        atributo = resuelto;
        Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(clase, referencia);
        Tipo tipo = resuelto.getTipo().sustituir(sustitucion);
        if (siguiente != null) {
            return siguiente.chequear(ts, tipo);
        }
        return InfoCheck.asignable(tipo);
    }

    @Override
    public void generar(Generador g, boolean destino) {
        int offset = esLongitud ? 1 : atributo.getOffset();
        if (destino && siguiente == null) {
            g.emit("SWAP");
            g.emit("STOREREF " + offset, "guarda en el atributo " + nombre);
            return;
        }
        g.emit("LOADREF " + offset, esLongitud
                ? "apila la longitud del arreglo"
                : "apila el atributo " + nombre);
        if (siguiente != null) {
            siguiente.generar(g, destino);
        }
    }
}
