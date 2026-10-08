package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/**
 * Tipo arreglo: un tipo base (primitivo, referencia o parametro) con una
 * cantidad de dimensiones.
 */
public class TipoArreglo extends Tipo {
    private final Tipo base;
    private final int dimensiones;

    public TipoArreglo(Tipo base, int dimensiones, Token token) {
        super(token);
        this.base = base;
        this.dimensiones = dimensiones;
    }

    public Tipo getBase() {
        return base;
    }

    public int getDimensiones() {
        return dimensiones;
    }

    @Override
    public String getNombre() {
        StringBuilder sb = new StringBuilder(base.getNombre());
        for (int i = 0; i < dimensiones; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        base.esValidoEn(ts, parametroContexto);
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        return new TipoArreglo(base.sustituir(sustituciones), dimensiones, token);
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        return otro instanceof TipoArreglo
                && ((TipoArreglo) otro).dimensiones == dimensiones
                && base.esIgualA(((TipoArreglo) otro).base);
    }

    @Override
    public boolean esArreglo() {
        return true;
    }
}
