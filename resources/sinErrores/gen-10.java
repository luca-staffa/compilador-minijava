///99&99&exitosamente
// Interfaz con vinculacion dinamica

interface Operacion {
    int ejecutar(int x);
}

class Doble implements Operacion {
    public int ejecutar(int x) {
        return x * 2;
    }
}

class Triple implements Operacion {
    public int ejecutar(int x) {
        return x * 3;
    }
}

class Init {
    static int aplicar(Operacion o, int x) {
        return o.ejecutar(x);
    }

    static void main() {
        debugPrint(aplicar(new Doble(), 49) + 1);
        debugPrint(aplicar(new Triple(), 33));
    }
}
