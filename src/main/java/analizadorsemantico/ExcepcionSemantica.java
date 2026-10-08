package analizadorsemantico;

/**
 * Excepcion propagada ante un error en el chequeo de declaraciones.
 * Conserva el lexema y el numero de linea del token asociado al error.
 */
public class ExcepcionSemantica extends RuntimeException {
    private final String lexema;
    private final int nroLinea;

    public ExcepcionSemantica(String lexema, int nroLinea, String mensaje) {
        super(mensaje);
        this.lexema = lexema;
        this.nroLinea = nroLinea;
    }

    public String getLexema() {
        return lexema;
    }

    public int getNroLinea() {
        return nroLinea;
    }
}
