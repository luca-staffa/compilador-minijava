package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.ast.NodoBloque;

/**
 * Unidad de programa: metodo o constructor. Toda unidad tiene parametros
 * formales ordenados y puede tener asociado el AST de su bloque, que se
 * construye durante el analisis sintactico y se chequea luego del chequeo y la
 * consolidacion de declaraciones.
 */
public abstract class Unidad extends Entidad {

    private NodoBloque cuerpo;

    protected Unidad(Token token, TablaDeSimbolos ts) {
        super(token, ts);
    }

    public NodoBloque getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(NodoBloque cuerpo) {
        this.cuerpo = cuerpo;
    }

    /** Chequea sentencias del cuerpo de la unidad, si fue construido. */
    public void chequearCuerpo(TablaDeSimbolos ts) {
        if (cuerpo != null) {
            cuerpo.chequear(ts);
        }
    }

    /** Parametros formales ordenados por posicion. */
    public abstract Map<Integer, Parametro> getParams();

    public abstract int getAridad();

    public abstract boolean esEstatico();

    /** Busca un parametro formal por nombre; null si no existe. */
    public Parametro buscarParametro(String nombreParametro) {
        for (Parametro parametro : getParams().values()) {
            if (parametro.getNombre().equals(nombreParametro)) {
                return parametro;
            }
        }
        return null;
    }
}
