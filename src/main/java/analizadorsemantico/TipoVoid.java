package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/** Tipo void. Solo puede utilizarse como tipo de retorno de un metodo. */
public class TipoVoid extends Tipo {

    public TipoVoid(Token token) {
        super(token);
    }

    @Override
    public String getNombre() {
        return "void";
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        // Siempre valido en el contexto de un tipo de retorno.
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        return this;
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        return otro instanceof TipoVoid;
    }
}
