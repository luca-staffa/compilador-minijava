///2&3&exitosamente
// && y || deben evaluar con cortocircuito: el operando derecho no se ejecuta
// cuando el izquierdo ya decide el resultado (aqui 1/0 fallaria en ejecucion).
class Init {
    static boolean f() {
        return false;
    }
    static boolean t() {
        return true;
    }
    static void main() {
        if (f() && (1 / 0 == 0)) {
            System.printIln(1);
        }
        if (t() || (1 / 0 == 0)) {
            System.printIln(2);
        }
        System.printIln(3);
    }
}
