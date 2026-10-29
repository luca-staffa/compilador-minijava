package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.ExcepcionSemantica;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Parametro;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoPrimitivo;
import analizadorsemantico.TipoVoid;

/**
 * Nodo de expresion del AST. Cada nodo conoce su token, controla lo que le
 * corresponde y delega el chequeo en sus hijos. El resultado del chequeo es un
 * {@link InfoCheck}, que ademas del tipo conserva las propiedades estructurales
 * (destino asignable y forma admisible como sentencia de llamada).
 */
public abstract class NodoExpresion {

    protected final Token token;

    protected NodoExpresion(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    /**
     * Token que representa el valor de la expresion cuando se la usa en un
     * contexto que exige un tipo (condicion, return, argumento). Por defecto es
     * el token propio; las llamadas usan el parentesis y los parentizados, su
     * contenido.
     */
    public Token getTokenValor() {
        return token;
    }

    /**
     * Token que se senala cuando la expresion no es admisible como sentencia.
     * Por defecto es el token propio; las referencias usan el ultimo eslabon.
     */
    public Token getTokenSentencia() {
        return token;
    }

    public abstract InfoCheck chequear(TablaDeSimbolos ts);

    protected void error(String mensaje) {
        errorEn(token, mensaje);
    }

    protected static void errorEn(Token tokenDelError, String mensaje) {
        throw new ExcepcionSemantica(
                tokenDelError.getLexema(), tokenDelError.getNroLinea(), mensaje);
    }

    public static boolean esPrimitivo(Tipo tipo, TipoPrimitivo.Base base) {
        return tipo instanceof TipoPrimitivo && ((TipoPrimitivo) tipo).getBase() == base;
    }

    public static boolean esInt(Tipo tipo) {
        return esPrimitivo(tipo, TipoPrimitivo.Base.INT);
    }

    public static boolean esBoolean(Tipo tipo) {
        return esPrimitivo(tipo, TipoPrimitivo.Base.BOOLEAN);
    }

    /**
     * Chequea los argumentos actuales contra los parametros formales ya
     * instanciados. El error se reporta sobre el token de la llamada.
     */
    public static void chequearArgumentos(TablaDeSimbolos ts, List<Parametro> formales,
                                          List<NodoExpresion> argumentos, Token tokenLlamada,
                                          String descripcion) {
        for (int i = 0; i < argumentos.size(); i++) {
            InfoCheck info = argumentos.get(i).chequear(ts);
            Tipo tipoActual = info.getTipo();
            if (tipoActual instanceof TipoVoid) {
                errorEn(tokenLlamada,
                        "el argumento " + (i + 1) + " de " + descripcion + " es de tipo void");
            }
            Tipo tipoFormal = formales.get(i).getTipo();
            if (!SemanticaUtil.conforma(tipoActual, tipoFormal, ts)) {
                errorEn(tokenLlamada,
                        "el argumento " + (i + 1) + " de " + descripcion + " es de tipo "
                                + tipoActual.getNombre() + " y no conforma con "
                                + tipoFormal.getNombre());
            }
        }
    }
}
