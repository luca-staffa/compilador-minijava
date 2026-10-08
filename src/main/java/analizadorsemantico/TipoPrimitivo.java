package analizadorsemantico;

import java.util.Map;

import analizadorlexico.Token;

/** Tipo primitivo: boolean, char o int. */
public class TipoPrimitivo extends Tipo {

    public enum Base {
        BOOLEAN("boolean"),
        CHAR("char"),
        INT("int");

        private final String nombre;

        Base(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private final Base base;

    public TipoPrimitivo(Base base, Token token) {
        super(token);
        this.base = base;
    }

    public Base getBase() {
        return base;
    }

    @Override
    public String getNombre() {
        return base.getNombre();
    }

    @Override
    public void esValidoEn(TablaDeSimbolos ts, String parametroContexto) {
        // Siempre valido.
    }

    @Override
    public Tipo sustituir(Map<String, Tipo> sustituciones) {
        return this;
    }

    @Override
    public boolean esIgualA(Tipo otro) {
        return otro instanceof TipoPrimitivo && ((TipoPrimitivo) otro).base == base;
    }

    @Override
    public boolean esPrimitivo() {
        return true;
    }
}
