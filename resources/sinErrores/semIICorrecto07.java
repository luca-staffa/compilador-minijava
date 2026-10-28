// Interfaces, herencia y conformidad entre tipos
interface Medible {
    int medir();
}

class Base {
    int valor;

    int obtener() {
        return valor;
    }
}

class Derivada extends Base {
}

class Implementa implements Medible {
    int medir() {
        return 1;
    }

    void usar(Medible medible, Implementa implementa) {
        medible = implementa;
    }
}

class Uso2 {
    void m(Derivada derivada, Base base) {
        base = derivada;
        var x = derivada.obtener();
    }
}
