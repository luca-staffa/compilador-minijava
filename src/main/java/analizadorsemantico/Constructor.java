package analizadorsemantico;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analizadorlexico.Token;

/** Constructor de una clase. */
public class Constructor extends Entidad {
    private final Map<Integer, Parametro> params;

    public Constructor(Token token, List<Parametro> parametros, TablaDeSimbolos ts) {
        super(token, ts);
        this.params = new LinkedHashMap<>();
        for (Parametro parametro : parametros) {
            params.put(parametro.getPosicion(), parametro);
        }
        controlarNombresDeParametros();
    }

    private void controlarNombresDeParametros() {
        Set<String> nombres = new HashSet<>();
        for (Parametro parametro : params.values()) {
            if (!nombres.add(parametro.getNombre())) {
                throw new ExcepcionSemantica(
                        parametro.getToken().getLexema(),
                        parametro.getToken().getNroLinea(),
                        "el parametro " + parametro.getNombre() + " esta repetido en el constructor de " + nombre);
            }
        }
    }

    public Map<Integer, Parametro> getParams() {
        return params;
    }

    public int getAridad() {
        return params.size();
    }

    @Override
    public void estaBienDeclarada() {
        for (Parametro parametro : params.values()) {
            parametro.estaBienDeclarada();
        }
    }
}
