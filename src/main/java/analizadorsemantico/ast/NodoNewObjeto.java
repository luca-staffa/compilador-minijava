package analizadorsemantico.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.Constructor;
import analizadorsemantico.Entidad;
import analizadorsemantico.InfoCheck;
import analizadorsemantico.Parametro;
import analizadorsemantico.SemanticaUtil;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsemantico.Tipo;
import analizadorsemantico.TipoReferencia;

/**
 * Creacion de objetos mediante un constructor. El token base es la palabra
 * reservada {@code new}; el token del tipo se conserva aparte para los errores
 * que apuntan al tipo (tipo inexistente o uso invalido de genericidad).
 */
public class NodoNewObjeto extends NodoExpresion {

    private final Token tokenTipo;
    private final TipoReferencia tipo;
    private final List<NodoExpresion> argumentos;

    public NodoNewObjeto(Token tokenNew, Token tokenTipo, TipoReferencia tipo,
                         List<NodoExpresion> argumentos) {
        super(tokenNew);
        this.tokenTipo = tokenTipo;
        this.tipo = tipo;
        this.argumentos = argumentos;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        Entidad entidad = ts.getTipo(tipo.getNombreReferencia());
        if (entidad == null) {
            errorEn(tokenTipo, "no se puede instanciar " + tipo.getNombreReferencia()
                    + " porque no es una clase");
        }
        if (!(entidad instanceof Clase)) {
            errorEn(tokenTipo, "no se puede instanciar " + tipo.getNombreReferencia()
                    + " porque no es una clase");
        }
        if (!tipo.esDiamante()) {
            SemanticaUtil.validarTipo(tipo, ts, tokenTipo);
        }
        Clase clase = (Clase) entidad;
        if (clase.getParametroGenerico() != null && tipo.getArgumento() == null) {
            errorEn(tokenTipo, "el tipo " + clase.getNombre()
                    + " es generico y requiere un argumento de tipo");
        }

        Constructor constructor = clase.getConstructores().get(argumentos.size());
        if (constructor == null) {
            error("la clase " + clase.getNombre() + " no tiene un constructor con "
                    + argumentos.size() + " parametro(s)");
        }

        Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(clase, tipo);
        List<Parametro> formales = new ArrayList<>();
        for (Parametro parametro : constructor.getParams().values()) {
            formales.add(new Parametro(
                    parametro.getToken(),
                    parametro.getTipo().sustituir(sustitucion),
                    parametro.getPosicion(),
                    ts));
        }
        chequearArgumentos(ts, formales, argumentos, token,
                "el constructor de " + clase.getNombre());
        return InfoCheck.llamada(tipo);
    }
}
