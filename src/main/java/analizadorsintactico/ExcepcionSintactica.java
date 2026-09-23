package analizadorsintactico;

public class ExcepcionSintactica extends RuntimeException {
    private final String lexema;
    private final int nroLinea;

    public ExcepcionSintactica(String lexema, int nroLinea, String mensaje) {
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
