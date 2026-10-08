package analizadorsemantico;

import analizadorlexico.Token;

/**
 * Entidad declarable de MiniJava. Toda entidad conserva el token de su
 * declaracion (lexema y numero de linea) para el reporte de errores, y conoce
 * la Tabla de Simbolos (elemento global) en la que vive.
 */
public abstract class Entidad {
    protected final Token token;
    protected final String nombre;
    protected final TablaDeSimbolos ts;

    protected Entidad(Token token, TablaDeSimbolos ts) {
        this.token = token;
        this.nombre = token.getLexema();
        this.ts = ts;
    }

    public Token getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    public int getLinea() {
        return token.getNroLinea();
    }

    /** Nombre del parametro de tipo declarado, o null si no declara ninguno. */
    public String getParametroGenerico() {
        return null;
    }

    /** Controla que esta entidad este correctamente declarada. */
    public abstract void estaBienDeclarada();

    /**
     * Parametro de tipo del tipo actualmente en analisis (clase o interfaz),
     * provisto por la Tabla de Simbolos como ambiente actual.
     */
    protected String parametroContexto() {
        Entidad actual = ts.getClaseActual() != null
                ? ts.getClaseActual()
                : ts.getInterfazActual();
        return actual == null ? null : actual.getParametroGenerico();
    }

    protected void error(String mensaje) {
        errorEn(token, mensaje);
    }

    protected static void errorEn(Token tokenDelError, String mensaje) {
        throw new ExcepcionSemantica(
                tokenDelError.getLexema(), tokenDelError.getNroLinea(), mensaje);
    }
}
