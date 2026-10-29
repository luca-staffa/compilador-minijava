package analizadorsemantico.ast;

import java.util.List;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Metodo;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;

/**
 * Llamada a un metodo sin receptor explicito. Los errores de resolucion se
 * reportan sobre el nombre del metodo; el parentesis de los argumentos es el
 * token de valor (el que se senala cuando la llamada aparece en un contexto que
 * exige un tipo, como una condicion o un return).
 */
public class NodoLlamada extends NodoExpresion {

    private final String nombre;
    private final Token tokenParA;
    private final List<NodoExpresion> argumentos;

    public NodoLlamada(Token tokenNombre, Token tokenParA, List<NodoExpresion> argumentos) {
        super(tokenNombre);
        this.nombre = tokenNombre.getLexema();
        this.tokenParA = tokenParA;
        this.argumentos = argumentos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<NodoExpresion> getArgumentos() {
        return argumentos;
    }

    @Override
    public Token getTokenValor() {
        return tokenParA != null ? tokenParA : token;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        Clase clase = ts.getClaseActual();
        if (clase == null) {
            error("no hay una clase actual para resolver el metodo " + nombre);
        }
        Metodo metodo = clase.getMetodos().get(nombre + "/" + argumentos.size());
        if (metodo == null) {
            error("el metodo " + nombre + " con " + argumentos.size()
                    + " parametro(s) no fue declarado");
        }
        if (SemanticaUtil.esContextoEstatico(ts) && !metodo.esEstatico()) {
            error("no se puede llamar al metodo de instancia " + nombre
                    + " desde un contexto estatico");
        }
        chequearArgumentos(ts, metodo.getParametros(), argumentos,
                token, "el metodo " + nombre);
        return InfoCheck.llamada(metodo.getTipoRetorno());
    }
}
