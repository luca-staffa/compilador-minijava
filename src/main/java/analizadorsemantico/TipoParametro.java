package analizadorsemantico;

import java.util.Map;
import java.util.Objects;

import analizadorlexico.Token;

/**
 * Uso de un parametro de tipo generico (por ejemplo T). Conserva, ademas del
 * nombre, la entidad que lo declaro: dos parametros que comparten letra pero
 * provienen de declaraciones distintas son tipos distintos (Seccion 5).
 */
public class TipoParametro extends Tipo {
    private final String nombre;
    private final Entidad origen;

    public TipoParametro(String nombre, Entidad origen, Token token) {
        super(token);
        this.nombre = nombre;
        this.origen = origen;
    }

    public String getNombreParametro() {
        return nombre;
    }

    public Entidad getOrigen() {
        return origen;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        if (parametroContexto == null || !parametroContexto.equals(nombre)) {
            error("el parametro de tipo " + nombre + " no esta declarado en este contexto");
        }
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        Tipo reemplazo = sustituciones.get(nombre);
        return reemplazo != null ? reemplazo : this;
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        if (!(otro instanceof TipoParametro)) {
            return false;
        }
        TipoParametro parametro = (TipoParametro) otro;
        return parametro.nombre.equals(nombre)
                && Objects.equals(parametro.origen, origen);
    }

    @Override
    public boolean esParametro() {
        return true;
    }
}
