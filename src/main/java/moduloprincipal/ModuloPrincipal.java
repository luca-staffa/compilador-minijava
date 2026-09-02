package moduloprincipal;

import java.io.IOException;
import analizadorlexico.AnalizadorLexico;
import analizadorlexico.ExcepcionLexica;
import analizadorlexico.Token;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerEficiente;

public class ModuloPrincipal {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java -jar Compilador.jar <archivo_fuente>");
            return;
        }

        SourceManager gestorDeFuente = new SourceManagerEficiente();

        try {
            gestorDeFuente.open(args[0]);
            AnalizadorLexico lexico = new AnalizadorLexico(gestorDeFuente);

            Token token;
            boolean huboErrores = false;
            boolean fin = false;
            while (!fin) {
                try {
                    token = lexico.proximoToken();
                    System.out.println("(" + token.getNombre() + "," + token.getLexema() + ","
                            + token.getNroLinea() + ")");
                    fin = "EOF".equals(token.getNombre());
                } catch (ExcepcionLexica e) {
                    System.out.println("Error Léxico en linea " + e.getNroLinea() + ": " + e.getMessage());
                    System.out.println("[Error:" + e.getLexema() + "|" + e.getNroLinea() + "]");
                    huboErrores = true;
                }
            }

            if (!huboErrores) {
                System.out.println("[SinErrores]");
            }
        } catch (IOException e) {
            System.out.println("No se pudo procesar el archivo: " + args[0]);
        } finally {
            try {
                gestorDeFuente.close();
            } catch (IOException e) {
            }
        }
    }
}
