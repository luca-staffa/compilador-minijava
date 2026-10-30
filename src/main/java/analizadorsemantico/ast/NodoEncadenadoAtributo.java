package analizadorsemantico.ast;

import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Atributo;
import analizadorsemantico.Clase;
import analizadorsemantico.Entidad;
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
        Atributo atributo = clase.getAtributos().get(nombre);
        if (atributo == null) {
            errorEn(token, "el atributo " + nombre + " no existe en "
                    + clase.getNombre());
        }
        Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(clase, referencia);
        Tipo tipo = atributo.getTipo().sustituir(sustitucion);
        if (siguiente != null) {
            return siguiente.chequear(ts, tipo);
        }
        return InfoCheck.asignable(tipo);
    }
}
