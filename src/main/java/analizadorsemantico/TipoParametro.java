package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/** Uso de un parametro de tipo generico (por ejemplo T). */
public class TipoParametro extends Tipo {
    private final String nombre;

    public TipoParametro(String nombre, Token token) {
        super(token);
        this.nombre = nombre;
    }

    public String getNombreParametro() {
        return nombre;
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
        return otro instanceof TipoParametro && ((TipoParametro) otro).nombre.equals(nombre);
    }

    @Override
    public boolean esParametro() {
        return true;
    }
}
