package analizadorsemantico;

import analizadorlexico.Token;

/** Parametro formal de un metodo o constructor. */
public class Parametro extends Entidad {
    private final Tipo tipo;
    private final int posicion;
    private int offset;

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

    /** Desplazamiento del parametro dentro del RA de su unidad. */
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public void estaBienDeclarada() {
        tipo.esValidoEn(ts, parametroContexto());
    }
}
