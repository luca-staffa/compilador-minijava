package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.Entidad;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Metodo;
import analizadorsemantico.TablaDeSimbolos;

/**
 * Llamada a un metodo mediante el nombre de la clase. El error de resolucion
 * (metodo inexistente o no estatico) se reporta sobre el selector punto; el
 * error de argumentos, sobre el nombre del metodo.
 */
public class NodoLlamadaEstatica extends NodoExpresion {

    private final Token tokenClase;
    private final Token tokenPunto;
    private final Token tokenMetodo;
    private final Token tokenParA;
    private final List<NodoExpresion> argumentos;

    public NodoLlamadaEstatica(Token tokenClase, Token tokenPunto, Token tokenMetodo,
                               Token tokenParA, List<NodoExpresion> argumentos) {
        super(tokenMetodo);
        this.tokenClase = tokenClase;
        this.tokenPunto = tokenPunto;
        this.tokenMetodo = tokenMetodo;
        this.tokenParA = tokenParA;
        this.argumentos = argumentos;
    }

    @Override
    public Token getTokenValor() {
        return tokenParA != null ? tokenParA : token;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        Entidad entidad = ts.getTipo(tokenClase.getLexema());
        if (!(entidad instanceof Clase)) {
            errorEn(tokenClase, "la clase " + tokenClase.getLexema() + " no esta declarada");
        }
        Clase clase = (Clase) entidad;
        Metodo metodo = clase.getMetodos().get(tokenMetodo.getLexema() + "/" + argumentos.size());
        if (metodo == null) {
            errorEn(tokenPunto, "el metodo " + tokenMetodo.getLexema() + " con "
                    + argumentos.size() + " parametro(s) no fue declarado en "
                    + clase.getNombre());
        }
        if (!metodo.esEstatico()) {
            errorEn(tokenPunto, "el metodo " + tokenMetodo.getLexema()
                    + " no es estatico y no puede invocarse con el nombre de la clase");
        }
        chequearArgumentos(ts, metodo.getParametros(), argumentos, tokenMetodo,
                "el metodo " + tokenMetodo.getLexema());
        return InfoCheck.llamada(metodo.getTipoRetorno());
    }
}
