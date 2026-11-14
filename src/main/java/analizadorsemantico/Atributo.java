package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/** Atributo de instancia de una clase. */
public class Atributo extends Entidad {
    private final Tipo tipo;
    private int offset = -1;

    public Atributo(Token token, Tipo tipo, TablaDeSimbolos ts) {
        super(token, ts);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    /** Desplazamiento dentro del CIR (0 es la referencia a la VT). */
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Atributo instanciar(Map<String, Tipo> sustituciones) {
        if (sustituciones.isEmpty()) {
            return this;
        }
        Atributo copia = new Atributo(token, tipo.sustituir(sustituciones), ts);
        copia.offset = offset;
        return copia;
    }

    @Override
    public void estaBienDeclarada() {
        tipo.esValidoEn(ts, parametroContexto());
    }
}
