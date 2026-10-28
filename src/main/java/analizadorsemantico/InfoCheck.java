package analizadorsemantico;

/**
 * Resultado del chequeo de una expresion. Ademas del tipo resultante, conserva
 * las propiedades estructurales que el tipo por si solo no determina:
 * si el nodo designa un destino asignable y si es admisible como sentencia de
 * llamada (termina en una invocacion o en una creacion de objeto).
 */
public class InfoCheck {

    private final Tipo tipo;
    private final boolean asignable;
    private final boolean llamada;

    public InfoCheck(Tipo tipo, boolean asignable, boolean llamada) {
        this.tipo = tipo;
        this.asignable = asignable;
        this.llamada = llamada;
    }

    public static InfoCheck valor(Tipo tipo) {
        return new InfoCheck(tipo, false, false);
    }

    public static InfoCheck asignable(Tipo tipo) {
        return new InfoCheck(tipo, true, false);
    }

    public static InfoCheck llamada(Tipo tipo) {
        return new InfoCheck(tipo, false, true);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public boolean esAsignable() {
        return asignable;
    }

    public boolean esLlamada() {
        return llamada;
    }
}
