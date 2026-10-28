package analizadorsemantico;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ambito lexico de un bloque. Cada bloque conoce su bloque contenedor, de modo
 * que la resolucion de variables locales sube por la cadena de ambitos.
 */
public class Bloque {

    private final Bloque padre;
    private final Map<String, VariableLocal> variables = new LinkedHashMap<>();

    public Bloque(Bloque padre) {
        this.padre = padre;
    }

    public Bloque getPadre() {
        return padre;
    }

    public Map<String, VariableLocal> getVariables() {
        return variables;
    }

    public VariableLocal getVariable(String nombre) {
        return variables.get(nombre);
    }

    public void agregarVariable(VariableLocal variable) {
        variables.put(variable.getNombre(), variable);
    }
}
