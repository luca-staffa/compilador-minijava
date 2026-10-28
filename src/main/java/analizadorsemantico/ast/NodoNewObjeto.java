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

/** Creacion de objetos mediante un constructor. */
public class NodoNewObjeto extends NodoExpresion {

    private final TipoReferencia tipo;
    private final List<NodoExpresion> argumentos;

    public NodoNewObjeto(Token token, TipoReferencia tipo, List<NodoExpresion> argumentos) {
        super(token);
        this.tipo = tipo;
        this.argumentos = argumentos;
    }

    @Override
    public InfoCheck chequear(TablaDeSimbolos ts) {
        Entidad entidad = ts.getTipo(tipo.getNombreReferencia());
        if (!(entidad instanceof Clase)) {
            error("no se puede instanciar " + tipo.getNombreReferencia()
                    + " porque no es una clase");
        }
        if (!tipo.esDiamante()) {
            SemanticaUtil.validarTipo(tipo, ts, token);
        }
        Clase clase = (Clase) entidad;
        if (clase.getParametroGenerico() != null && tipo.getArgumento() == null) {
            error("el tipo " + clase.getNombre()
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
