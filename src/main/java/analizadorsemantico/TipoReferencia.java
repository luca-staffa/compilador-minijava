package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/**
 * Tipo referencia: nombra una clase, interfaz o entidad predefinida, con un
 * argumento generico opcional (null si no se especifica).
 */
public class TipoReferencia extends Tipo {
    private final String nombre;
    private final Tipo argumento;

    public TipoReferencia(String nombre, Tipo argumento, Token token) {
        super(token);
        this.nombre = nombre;
        this.argumento = argumento;
    }

    public String getNombreReferencia() {
        return nombre;
    }

    public Tipo getArgumento() {
        return argumento;
    }

    public boolean esGenerico() {
        return argumento != null;
    }

    @Override
    public String getNombre() {
        if (argumento == null) {
            return nombre;
        }
        return nombre + "<" + argumento.getNombre() + ">";
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        if (!ts.esTipo(nombre)) {
            error("el tipo " + nombre + " no esta declarado");
        }
        if (argumento != null) {
            Entidad entidad = ts.getTipo(nombre);
            if (entidad.getParametroGenerico() == null) {
                error("el tipo " + nombre + " no es generico y no admite argumentos de tipo");
            }
            argumento.esValidoEn(ts, parametroContexto);
        }
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        return new TipoReferencia(nombre, sustituirSiCorresponde(argumento, sustituciones), token);
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        if (!(otro instanceof TipoReferencia)) {
            return false;
        }
        TipoReferencia referencia = (TipoReferencia) otro;
        if (!referencia.nombre.equals(nombre)) {
            return false;
        }
        if (argumento == null || referencia.argumento == null) {
            return argumento == null && referencia.argumento == null;
        }
        return argumento.esIgualA(referencia.argumento);
    }

    @Override
    public boolean esReferencia() {
        return true;
    }
}
