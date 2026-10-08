///[SinErrores]
// Implementacion de una interfaz con contrato cumplido

interface InterfazBase {
    int f(int x);
}

class Gamma implements InterfazBase {
    int f(int x) {
        return x;
    }
}
