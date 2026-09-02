package analizadorlexico;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import sourcemanager.SourceManager;

public class AnalizadorLexico {
    private static final Map<String, String> palabrasClave = new HashMap<>();

    static {
        palabrasClave.put("class", "pr_class");
        palabrasClave.put("extends", "pr_extends");
        palabrasClave.put("interface", "pr_interface");
        palabrasClave.put("implements", "pr_implements");
        palabrasClave.put("static", "pr_static");
        palabrasClave.put("boolean", "pr_boolean");
        palabrasClave.put("char", "pr_char");
        palabrasClave.put("int", "pr_int");
        palabrasClave.put("void", "pr_void");
        palabrasClave.put("public", "pr_public");
        palabrasClave.put("if", "pr_if");
        palabrasClave.put("else", "pr_else");
        palabrasClave.put("while", "pr_while");
        palabrasClave.put("return", "pr_return");
        palabrasClave.put("var", "pr_var");
        palabrasClave.put("this", "pr_this");
        palabrasClave.put("new", "pr_new");
        palabrasClave.put("null", "pr_null");
        palabrasClave.put("true", "pr_true");
        palabrasClave.put("false", "pr_false");
    }

    private final SourceManager gestorDeFuente;
    private StringBuilder lexema;
    private char caracterActual;

    public AnalizadorLexico(SourceManager gestorDeFuente) throws IOException {
        this.gestorDeFuente = gestorDeFuente;
        lexema = new StringBuilder();
        actualizarCaracterActual();
    }

    public Token proximoToken() throws IOException {
        lexema.setLength(0);
        return e0();
    }

    private void actualizarLexema() {
        lexema.append(caracterActual);
    }

    private void actualizarCaracterActual() throws IOException {
        caracterActual = gestorDeFuente.getNextChar();
    }

    private int nroLinea() {
        return gestorDeFuente.getLineNumber();
    }

    private Token token(String nombre) {
        return new Token(nombre, lexema.toString(), nroLinea());
    }

    private void error(String mensaje) {
        throw new ExcepcionLexica(lexema.toString(), nroLinea(), mensaje);
    }

    private boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean esLetraMinuscula(char c) {
        return c >= 'a' && c <= 'z';
    }

    private boolean esLetraMayuscula(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private boolean esCaracterId(char c) {
        return esLetraMayuscula(c) || esLetraMinuscula(c) || esDigito(c) || c == '_';
    }

    private Token e0() throws IOException {
        if (esDigito(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eEntero(1);
        }
        if (esLetraMinuscula(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eIdMetVar();
        }
        if (esLetraMayuscula(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eIdMayuscula();
        }
        if (caracterActual == '"') {
            actualizarLexema();
            actualizarCaracterActual();
            return eString();
        }
        if (caracterActual == '\'') {
            actualizarLexema();
            actualizarCaracterActual();
            return eCaracter();
        }
        if (caracterActual == ' ' || caracterActual == '\t' || caracterActual == '\n') {
            actualizarCaracterActual();
            return e0();
        }
        if (caracterActual == SourceManager.END_OF_FILE) {
            return new Token("EOF", "$", nroLinea());
        }
        if (caracterActual == '+') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpMas();
        }
        if (caracterActual == '-') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpMenos();
        }
        if (caracterActual == '*') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op*");
        }
        if (caracterActual == '/') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpDiv();
        }
        if (caracterActual == '%') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op%");
        }
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpIgual();
        }
        if (caracterActual == '!') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpNot();
        }
        if (caracterActual == '>') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpMayor();
        }
        if (caracterActual == '<') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpMenor();
        }
        if (caracterActual == '&') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpAnd();
        }
        if (caracterActual == '|') {
            actualizarLexema();
            actualizarCaracterActual();
            return eOpOr();
        }
        if (caracterActual == '(') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("parA");
        }
        if (caracterActual == ')') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("parC");
        }
        if (caracterActual == '{') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("llaveA");
        }
        if (caracterActual == '}') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("llaveC");
        }
        if (caracterActual == '[') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("corcheteA");
        }
        if (caracterActual == ']') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("corcheteC");
        }
        if (caracterActual == ';') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("puntoComa");
        }
        if (caracterActual == ',') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("coma");
        }
        if (caracterActual == '.') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("punto");
        }
        if (caracterActual == ':') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("dosPuntos");
        }
        actualizarLexema();
        actualizarCaracterActual();
        error(lexema.toString() + " no es un símbolo valido");
        return null;
    }

    private Token eEntero(int cantidadDigitos) throws IOException {
        if (esDigito(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eEntero(cantidadDigitos + 1);
        }
        if (cantidadDigitos > 9) {
            error("literal entero demasiado largo");
        }
        return token("litInt");
    }

    private Token eIdMetVar() throws IOException {
        if (esCaracterId(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eIdMetVar();
        }
        String nombre = palabrasClave.get(lexema.toString());
        if (nombre != null) {
            return new Token(nombre, lexema.toString(), nroLinea());
        }
        return token("idMV");
    }

    private Token eIdMayuscula() throws IOException {
        if (esCaracterId(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eIdClase();
        }
        return token("idGen");
    }

    private Token eIdClase() throws IOException {
        if (esCaracterId(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return eIdClase();
        }
        return token("idClase");
    }

    private Token eString() throws IOException {
        if (caracterActual == '"') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("litString");
        }
        if (caracterActual == '\\') {
            actualizarLexema();
            actualizarCaracterActual();
            return eStringEscape();
        }
        if (caracterActual == '\n' || caracterActual == SourceManager.END_OF_FILE) {
            error("literal string mal formado");
        }
        actualizarLexema();
        actualizarCaracterActual();
        return eString();
    }

    private Token eStringEscape() throws IOException {
        if (caracterActual == '\n' || caracterActual == SourceManager.END_OF_FILE) {
            error("literal string mal formado");
        }
        actualizarLexema();
        actualizarCaracterActual();
        return eString();
    }

    private Token eCaracter() throws IOException {
        if (caracterActual == '\'') {
            error("literal caracter mal formado");
        }
        if (caracterActual == '\n' || caracterActual == SourceManager.END_OF_FILE) {
            error("literal caracter mal formado");
        }
        if (caracterActual == '\\') {
            actualizarLexema();
            actualizarCaracterActual();
            return eCaracterEscape();
        }
        actualizarLexema();
        actualizarCaracterActual();
        return eCaracterFin();
    }

    private Token eCaracterEscape() throws IOException {
        if (caracterActual == '\n' || caracterActual == SourceManager.END_OF_FILE) {
            error("literal caracter mal formado");
        }
        actualizarLexema();
        actualizarCaracterActual();
        return eCaracterFin();
    }

    private Token eCaracterFin() throws IOException {
        if (caracterActual == '\'') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("litChar");
        }
        error("literal caracter mal formado");
        return null;
    }

    private Token eOpMas() throws IOException {
        if (caracterActual == '+') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op++");
        }
        return token("op+");
    }

    private Token eOpMenos() throws IOException {
        if (caracterActual == '-') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op--");
        }
        return token("op-");
    }

    private Token eOpIgual() throws IOException {
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op==");
        }
        return token("op=");
    }

    private Token eOpNot() throws IOException {
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op!=");
        }
        return token("op!");
    }

    private Token eOpMayor() throws IOException {
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op>=");
        }
        return token("op>");
    }

    private Token eOpMenor() throws IOException {
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op<=");
        }
        return token("op<");
    }

    private Token eOpAnd() throws IOException {
        if (caracterActual == '&') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op&&");
        }
        error(lexema.toString() + " no es un símbolo valido");
        return null;
    }

    private Token eOpOr() throws IOException {
        if (caracterActual == '|') {
            actualizarLexema();
            actualizarCaracterActual();
            return token("op||");
        }
        error(lexema.toString() + " no es un símbolo valido");
        return null;
    }

    private Token eOpDiv() throws IOException {
        if (caracterActual == '/') {
            actualizarLexema();
            actualizarCaracterActual();
            return eComentarioLinea();
        }
        if (caracterActual == '*') {
            actualizarLexema();
            actualizarCaracterActual();
            return eComentarioBloque();
        }
        return token("op/");
    }

    private Token eComentarioLinea() throws IOException {
        if (caracterActual == '\n' || caracterActual == SourceManager.END_OF_FILE) {
            lexema.setLength(0);
            return e0();
        }
        actualizarCaracterActual();
        return eComentarioLinea();
    }

    private Token eComentarioBloque() throws IOException {
        if (caracterActual == SourceManager.END_OF_FILE) {
            lexema.setLength(0);
            lexema.append("/*");
            error("comentario sin cerrar");
        }
        if (caracterActual == '*') {
            actualizarCaracterActual();
            if (caracterActual == '/') {
                actualizarCaracterActual();
                lexema.setLength(0);
                return e0();
            }
            return eComentarioBloque();
        }
        actualizarLexema();
        actualizarCaracterActual();
        return eComentarioBloque();
    }
}
