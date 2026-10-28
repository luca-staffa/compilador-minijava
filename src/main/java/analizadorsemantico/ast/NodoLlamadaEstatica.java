package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.Entidad;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Metodo;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;

/** Llamada a un metodo estatico mediante el nombre de la clase. */
public class NodoLlamadaEstatica extends NodoExpresion {

    private final Token tokenClase;
    private final Token tokenMetodo;
    private final List<NodoExpresion> argumentos;

    public NodoLlamadaEstatica(Token tokenClase, Token tokenMetodo,
                               List<NodoExpresion> argumentos) {
        super(tokenMetodo);
        this.tokenClase = tokenClase;
        this.tokenMetodo = tokenMetodo;
        this.argumentos = argumentos;
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
            errorEn(tokenMetodo, "el metodo " + tokenMetodo.getLexema() + " con "
                    + argumentos.size() + " parametro(s) no fue declarado en "
                    + clase.getNombre());
        }
        if (!metodo.esEstatico()) {
            errorEn(tokenMetodo, "el metodo " + tokenMetodo.getLexema()
                    + " no es estatico y no puede invocarse con el nombre de la clase");
        }
        chequearArgumentos(ts, metodo.getParametros(), argumentos, tokenMetodo,
                "el metodo " + tokenMetodo.getLexema());
        return InfoCheck.llamada(metodo.getTipoRetorno());
    }
}
