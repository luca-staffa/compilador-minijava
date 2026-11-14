///120&exitosamente
// Recursividad

class Fact {
    static int fac(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * fac(n - 1);
    }
}

class Init {
    static void main() {
        System.printIln(Fact.fac(5));
    }
}
