///15&exitosamente
// Llamada implicita a metodos de instancia y this

class Calculadora {
    int base;

    Calculadora(int b) {
        base = b;
    }

    int sumar(int x) {
        return base + x;
    }

    int sumarDosVeces(int x) {
        return sumar(x) + sumar(x);
    }
}

class Init {
    static void main() {
        var c = new Calculadora(3);
        System.printIln(c.sumarDosVeces(4) + 1);
    }
}
