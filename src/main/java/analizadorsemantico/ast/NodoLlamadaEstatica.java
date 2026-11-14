package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.Entidad;
import analizadorsemantico.Generador;
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

    private Metodo metodo;

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
        Metodo resuelto = clase.getMetodos().get(tokenMetodo.getLexema() + "/" + argumentos.size());
        if (resuelto == null) {
            errorEn(tokenMetodo, "el metodo " + tokenMetodo.getLexema() + " con "
                    + argumentos.size() + " parametro(s) no fue declarado en "
                    + clase.getNombre());
        }
        if (!resuelto.esEstatico()) {
            errorEn(tokenMetodo, "el metodo " + tokenMetodo.getLexema()
                    + " no es estatico y no puede invocarse con el nombre de la clase");
        }
        chequearArgumentos(ts, resuelto.getParametros(), argumentos, tokenMetodo,
                "el metodo " + tokenMetodo.getLexema());
        metodo = resuelto;
        return InfoCheck.llamada(resuelto.getTipoRetorno());
    }

    @Override
    public void generar(Generador g) {
        if (Generador.dejaValor(metodo)) {
            g.emit("RMEM 1", "reserva la celda de retorno");
        }
        for (NodoExpresion argumento : argumentos) {
            argumento.generar(g);
        }
        g.emit("PUSH " + metodo.getEtiqueta(), "apila el metodo " + tokenMetodo.getLexema());
        g.emit("CALL", "llama al metodo en el tope de la pila");
    }
}
