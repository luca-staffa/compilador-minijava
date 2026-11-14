package moduloprincipal;

import java.io.IOException;
import java.io.PrintWriter;

import analizadorlexico.AnalizadorLexico;
import analizadorlexico.ExcepcionLexica;
import analizadorsemantico.ExcepcionSemantica;
import analizadorsemantico.TablaDeSimbolos;
import analizadorsintactico.AnalizadorSintactico;
import analizadorsintactico.ExcepcionSintactica;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerEficiente;

public class ModuloPrincipal {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java -jar Compilador.jar <archivo_fuente> [<archivo_salida>]");
            return;
        }

        SourceManager gestorDeFuente = new SourceManagerEficiente();

        try {
            gestorDeFuente.open(args[0]);
            AnalizadorLexico analizadorLexico = new AnalizadorLexico(gestorDeFuente);
            TablaDeSimbolos tablaDeSimbolos = new TablaDeSimbolos();
            AnalizadorSintactico analizadorSintactico =
                    new AnalizadorSintactico(analizadorLexico, tablaDeSimbolos);

            analizadorSintactico.analizar();

            tablaDeSimbolos.estaBienDeclarada();
            tablaDeSimbolos.consolidar();
            tablaDeSimbolos.chequearCuerpos();

            String codigo = tablaDeSimbolos.generarCodigo();
            if (args.length >= 2) {
                escribirSalida(args[1], codigo);
            }

            System.out.println("Compilacion Exitosa");
            System.out.println("[SinErrores]");
        } catch (ExcepcionLexica e) {
            System.out.println("Error Lexico en linea " + e.getNroLinea() + ": " + e.getMessage());
            System.out.println("[Error:" + e.getLexema() + "|" + e.getNroLinea() + "]");
        } catch (ExcepcionSintactica e) {
            System.out.println("Error Sintactico en linea " + e.getNroLinea() + ": " + e.getMessage());
            System.out.println("[Error:" + e.getLexema() + "|" + e.getNroLinea() + "]");
        } catch (ExcepcionSemantica e) {
            System.out.println("Error Semantico en linea " + e.getNroLinea() + ": " + e.getMessage());
            System.out.println("[Error:" + e.getLexema() + "|" + e.getNroLinea() + "]");
        } catch (IOException e) {
            System.out.println("No se pudo procesar el archivo: " + args[0]);
        } finally {
            try {
                gestorDeFuente.close();
            } catch (IOException e) {
                // Nada que hacer si falla el cierre del archivo.
            }
        }
    }

    /** Escribe el codigo CeIASM generado en el archivo indicado. */
    private static void escribirSalida(String ruta, String codigo) {
        try (PrintWriter salida = new PrintWriter(ruta)) {
            salida.print(codigo);
        } catch (IOException e) {
            System.out.println("No se pudo escribir el archivo de salida: " + ruta);
        }
    }
}
