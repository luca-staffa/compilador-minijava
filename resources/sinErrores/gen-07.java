///30&exitosamente
// Variables locales, parametros y asignacion como expresion

class Calc {
    static int sumar(int a, int b) {
        return a + b;
    }

    static void main() {
        var x = 10;
        var y = 20;
        var z = x;
        z = y;
        debugPrint(x + y + z - 20);
    }
}
