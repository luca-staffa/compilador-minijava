package analizadorlexico;

public class ExcepcionLexica extends RuntimeException {
    private final String lexema;
    private final int nroLinea;

    public ExcepcionLexica(String lexema, int nroLinea, String mensaje) {
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
