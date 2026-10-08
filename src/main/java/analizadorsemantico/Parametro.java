package analizadorsemantico;

import analizadorlexico.Token;

/** Parametro formal de un metodo o constructor. */
public class Parametro extends Entidad {
    private final Tipo tipo;
    private final int posicion;

    public Parametro(Token token, Tipo tipo, int posicion, TablaDeSimbolos ts) {
        super(token, ts);
        this.tipo = tipo;
        this.posicion = posicion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getPosicion() {
        return posicion;
    }

    @Override
    public void estaBienDeclarada() {
        tipo.esValidoEn(ts, parametroContexto());
    }
}
