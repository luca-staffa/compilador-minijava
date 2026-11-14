///7&exitosamente
// return en el medio de un metodo y variable local viva

class Calc {
    static int f(int n) {
        var doble = n * 2;
        if (n > 0) {
            return doble + 1;
        }
        return 0;
    }
}

class Init {
    static void main() {
        System.printIln(Calc.f(3));
    }
}
