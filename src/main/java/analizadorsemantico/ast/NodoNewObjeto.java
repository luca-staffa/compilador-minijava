package analizadorsemantico.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import analizadorlexico.Token;
import analizadorsemantico.Clase;
import analizadorsemantico.Constructor;
import analizadorsemantico.Entidad;
import analizadorsemantico.Generador;
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

    private Clase clase;
    private Constructor constructor;

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
        Clase claseResuelta = (Clase) entidad;
        if (claseResuelta.getParametroGenerico() != null && tipo.getArgumento() == null) {
            errorEn(tokenTipo, "el tipo " + claseResuelta.getNombre()
                    + " es generico y requiere un argumento de tipo");
        }

        Constructor constructorResuelto = claseResuelta.getConstructores().get(argumentos.size());
        if (constructorResuelto == null) {
            error("la clase " + claseResuelta.getNombre() + " no tiene un constructor con "
                    + argumentos.size() + " parametro(s)");
        }

        Map<String, Tipo> sustitucion = SemanticaUtil.sustitucionVista(claseResuelta, tipo);
        List<Parametro> formales = new ArrayList<>();
        for (Parametro parametro : constructorResuelto.getParams().values()) {
            formales.add(new Parametro(
                    parametro.getToken(),
                    parametro.getTipo().sustituir(sustitucion),
                    parametro.getPosicion(),
                    ts));
        }
        chequearArgumentos(ts, formales, argumentos, token,
                "el constructor de " + claseResuelta.getNombre());
        clase = claseResuelta;
        constructor = constructorResuelto;
        return InfoCheck.llamada(tipo);
    }

    @Override
    public void generar(Generador g) {
        int tamano = clase.getAtributos().size() + 1; // atributos + referencia a la VT
        g.emit("RMEM 1", "reserva la celda de retorno (referencia al objeto)");
        g.emit("PUSH " + tamano, "tamano del CIR");
        g.emit("PUSH simple_malloc", "reserva memoria en el heap");
        g.emit("CALL");
        g.emit("DUP", "conserva la referencia al objeto");
        g.emit("PUSH " + Generador.etiquetaVT(clase.getNombre()), "apila la tabla virtual de la clase");
        g.emit("STOREREF 0", "inicializa la referencia a la VT");
        g.emit("DUP");
        for (NodoExpresion argumento : argumentos) {
            argumento.generar(g);
            g.emit("SWAP");
        }
        g.emit("PUSH " + constructor.getEtiqueta(), "apila el constructor");
        g.emit("CALL");
    }
}
