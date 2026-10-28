package analizadorsemantico;

import analizadorlexico.Token;

/**
 * Variable local de un bloque. Registra, como minimo, nombre, tipo, token de
 * declaracion y bloque al que pertenece.
 */
public class VariableLocal extends Entidad {

    private final Tipo tipo;
    private final Bloque bloque;

    public VariableLocal(Token token, Tipo tipo, Bloque bloque, TablaDeSimbolos ts) {
        super(token, ts);
        this.tipo = tipo;
        this.bloque = bloque;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Bloque getBloque() {
        return bloque;
    }

    @Override
    public void estaBienDeclarada() {
        // El tipo ya fue inferido y controlado al chequear el inicializador.
    }
}
