///15&exitosamente
// Constructor con parametros e inicializacion

class Punto {
    int x;
    int y;

    Punto(int a, int b) {
        x = a;
        y = b;
    }

    int suma() {
        return x + y;
    }
}

class Init {
    static void main() {
        var p = new Punto(7, 8);
        System.printIln(p.suma());
    }
}
