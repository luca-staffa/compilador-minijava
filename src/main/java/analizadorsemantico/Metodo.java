package analizadorsemantico;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analizadorlexico.Token;

/** Metodo de instancia o estatico de una clase o interfaz. */
public class Metodo extends Entidad {
    private final Tipo tipoRetorno;
    private final Map<Integer, Parametro> params;
    private final boolean esEstatico;

    public Metodo(Token token, Tipo tipoRetorno, List<Parametro> parametros,
                  boolean esEstatico, TablaDeSimbolos ts) {
        super(token, ts);
        this.tipoRetorno = tipoRetorno;
        this.esEstatico = esEstatico;
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
                        "el parametro " + parametro.getNombre() + " esta repetido en " + nombre);
            }
        }
    }

    public Tipo getTipoRetorno() {
        return tipoRetorno;
    }

    /** Parametros ordenados por posicion. */
    public Map<Integer, Parametro> getParams() {
        return params;
    }

    public List<Parametro> getParametros() {
        return new ArrayList<>(params.values());
    }

    public boolean esEstatico() {
        return esEstatico;
    }

    /** Clave de la signatura: (nombre, aridad). */
    public String getClave() {
        return nombre + "/" + params.size();
    }

    public int getAridad() {
        return params.size();
    }

    /**
     * Dos metodos tienen la misma signatura si coinciden en aridad, tipos de
     * los parametros en el mismo orden y tipo de retorno. Los nombres de los
     * parametros no forman parte de la signatura.
     */
    public boolean mismaSignatura(Metodo otro) {
        if (params.size() != otro.params.size()) {
            return false;
        }
        if (!tipoRetorno.esIgualA(otro.tipoRetorno)) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).getTipo().esIgualA(otro.params.get(i).getTipo())) {
                return false;
            }
        }
        return true;
    }

    /** Copia el metodo sustituyendo los parametros de tipo indicados. */
    public Metodo instanciar(Map<String, Tipo> sustituciones) {
        if (sustituciones.isEmpty()) {
            return this;
        }
        List<Parametro> nuevos = new ArrayList<>();
        for (Parametro parametro : params.values()) {
            nuevos.add(new Parametro(
                    parametro.getToken(),
                    parametro.getTipo().sustituir(sustituciones),
                    parametro.getPosicion(),
                    ts));
        }
        return new Metodo(token, tipoRetorno.sustituir(sustituciones), nuevos, esEstatico, ts);
    }

    @Override
    public void estaBienDeclarada() {
        tipoRetorno.esValidoEn(ts, parametroContexto());
        for (Parametro parametro : params.values()) {
            parametro.estaBienDeclarada();
        }
    }
}
