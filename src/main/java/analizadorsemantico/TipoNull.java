package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/**
 * Tipo especial del literal null. Conforma con clases, interfaces, parametros
 * de tipo y arreglos, pero no con tipos primitivos. No puede escribirse en una
 * declaracion.
 */
public class TipoNull extends Tipo {

    public TipoNull(Token token) {
        super(token);
    }

    @Override
    public String getNombre() {
        return "null";
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        error("null no puede utilizarse como tipo declarado");
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        return this;
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        return otro instanceof TipoNull;
    }

    public boolean esNull() {
        return true;
    }
}
