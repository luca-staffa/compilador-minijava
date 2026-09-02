package analizadorlexico;

public class Token {
    private final String nombre;
    private final String lexema;
    private final int nroLinea;

    public Token(String nombre, String lexema, int nroLinea) {
        this.nombre = nombre;
        this.lexema = lexema;
        this.nroLinea = nroLinea;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLexema() {
        return lexema;
    }

    public int getNroLinea() {
        return nroLinea;
    }
}
