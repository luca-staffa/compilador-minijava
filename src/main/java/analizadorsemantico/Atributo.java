package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/** Atributo de instancia de una clase. */
public class Atributo extends Entidad {
    private final Tipo tipo;

    public Atributo(Token token, Tipo tipo, TablaDeSimbolos ts) {
        super(token, ts);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Atributo instanciar(Map<String, Tipo> sustituciones) {
        if (sustituciones.isEmpty()) {
            return this;
        }
        return new Atributo(token, tipo.sustituir(sustituciones), ts);
    }

    @Override
    public void estaBienDeclarada() {
        tipo.esValidoEn(ts, parametroContexto());
    }
}
