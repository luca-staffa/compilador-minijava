# Diagrama de secuencia — Compilador MiniJava (Etapa 3)

Flujo completo del compilador, desde que el módulo principal abre el fuente hasta
que reporta el resultado: análisis léxico/sintáctico con construcción de la Tabla de
Símbolos (1ª pasada), chequeo de declaraciones (2ª pasada) y consolidación.

Para visualizarlo: pegar el bloque en <https://mermaid.live> o abrirlo en un visor
que soporte Mermaid (GitHub, VS Code con extensión).

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant MP as ModuloPrincipal
    participant SM as SourceManager
    participant AL as AnalizadorLexico
    participant AS as AnalizadorSintactico
    participant TS as TablaDeSimbolos
    participant E as Entidades
    participant T as Tipo
    participant O as System.out

    U->>MP: main(rutaDelFuente)
    MP->>SM: open(ruta)
    MP->>AL: new AnalizadorLexico(SM)
    MP->>TS: new TablaDeSimbolos()
    activate TS
    Note over TS,E: carga Object, String y System
    TS->>E: inicializarPredefinidas()
    deactivate TS
    MP->>AS: new AnalizadorSintactico(AL, TS)
    MP->>AS: analizar()
    activate AS
    AS->>AL: proximoToken()
    AL-->>AS: token

    Note over AS,TS: 1ª pasada - construccion de la TS<br/>(acciones semanticas y nombres repetidos)

    loop por cada clase o interfaz
        AS->>TS: insertar(Clase / Interfaz)
        alt nombre de tipo repetido
            TS-->>AS: throw ExcepcionSemantica
        end
        AS->>TS: setClaseActual(...) / setInterfazActual(...)
        AS->>T: tipo() / tipoReferencia() / tipoPrimitivo()
        T-->>AS: Tipo
        AS->>E: registrar ancestro y parametro generico
        loop por cada miembro
            alt atributo
                AS->>E: agregarAtributo(Atributo)
            else metodo
                AS->>E: agregarMetodo(Metodo)
                AS->>TS: setMetodoActual(metodo)
            else constructor
                AS->>E: agregarConstructor(Constructor)
            end
        end
        AS->>E: agregarConstructorPorDefecto()
        AS->>TS: setClaseActual(null) / setInterfazActual(null)
    end

    AS->>AL: proximoToken() hasta EOF
    AS-->>MP: fin del parseo
    deactivate AS

    Note over MP,TS: 2ª pasada - chequeo de declaraciones

    MP->>TS: estaBienDeclarada()
    activate TS
    loop por cada interfaz
        TS->>E: interfaz.estaBienDeclarada()
        E->>T: esValidoEn(ts, contexto)
        T-->>E: valida tipo / parametro / argumento generico
        E->>E: checkCircularidad()
    end
    loop por cada clase
        TS->>E: clase.estaBienDeclarada()
        E->>T: esValidoEn(...) del ancestro y de los miembros
        E->>E: checkCircularidad()
        E->>E: verificarRedefiniciones() - recorre ancestros
        E->>E: verificarContratoDeInterfaz()
    end
    deactivate TS

    Note over MP,TS: Consolidacion

    MP->>TS: consolidar()
    activate TS
    loop por cada interfaz y clase
        TS->>E: entidad.consolidar()
        E->>E: ancestro.consolidar()
        E->>E: heredarAtributos() y heredarMetodos()
    end
    deactivate TS

    alt sin errores
        MP->>O: "Compilacion Exitosa" + "[SinErrores]"
    else error lexico, sintactico o semantico
        Note over AL,MP: la excepcion se propaga al modulo principal
        MP->>O: mensaje + "[Error:lexema|nroLinea]"
    end
```

## Notas

- **Construcción (1ª pasada)**: las acciones semánticas viven en el
  `AnalizadorSintactico`; la TS es global y va almacenando las entidades a medida
  que se reconocen. Los controles de nombres repetidos ocurren al insertar.
- **Chequeo (2ª pasada)**: arranca desde la TS (`estaBienDeclarada`), que delega en
  cada clase/interfaz; cada entidad controla sus tipos, su relación de herencia y su
  circularidad, y la clase controla redefiniciones y contrato de interfaces.
- **Consolidación**: delega hacia el ancestro (recursión) y luego incorpora
  atributos y métodos heredados, instanciando o conservando los parámetros de tipo.
- **Errores**: cualquier `ExcepcionLexica`, `ExcepcionSintactica` o
  `ExcepcionSemantica` se propaga hasta `ModuloPrincipal`, que la reporta y finaliza.
