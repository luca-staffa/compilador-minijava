package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/**
 * Modelo uniforme de los tipos de MiniJava: primitivos, void, parametros de
 * tipo, arreglos y tipos referencia (con argumento generico opcional).
 *
 * Cada tipo conserva el token de la ocurrencia que lo origino para poder
 * reportar el error con el lexema y la linea correctos.
 */
public abstract class Tipo {
    protected final Token token;

    protected Tipo(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    /** Nombre canonico del tipo, usado en los mensajes de error. */
    public abstract String getNombre();

    /**
     * Controla que el tipo sea valido en el contexto dado. El parametro
     * {@code parametroContexto} es el nombre del parametro de tipo declarado
     * por la clase/interfaz actual (o {@code null} si no declara ninguno).
     * Lanza {@link ExcepcionSemantica} si no es valido.
     */
    public abstract void esValidoEn(TablaDeSimbolos ts, String parametroContexto);

    /** Reemplaza los parametros de tipo segun la sustitucion indicada. */
    public abstract Tipo sustituir(Map<String, Tipo> sustituciones);

    /** Igualdad estructural entre tipos. */
    public abstract boolean esIgualA(Tipo otro);

    public boolean esPrimitivo() {
        return false;
    }

    public boolean esArreglo() {
        return false;
    }

    public boolean esParametro() {
        return false;
    }

    public boolean esReferencia() {
        return false;
    }

    protected void error(String mensaje) {
        throw new ExcepcionSemantica(token.getLexema(), token.getNroLinea(), mensaje);
    }

    protected static Tipo sustituirSiCorresponde(Tipo tipo, Map<String, Tipo> sustituciones) {
        return tipo == null ? null : tipo.sustituir(sustituciones);
    }
}
