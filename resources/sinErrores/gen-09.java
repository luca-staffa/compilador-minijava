///1&2&3&exitosamente
// Herencia: atributos y metodos heredados y redefinidos

class Base {
    int comun;

    void poner() {
        comun = 1;
    }

    void mostrar() {
        debugPrint(comun);
    }
}

class Hijo extends Base {
    int propio;

    void poner() {
        comun = 2;
    }

    void mostrarHijo() {
        debugPrint(3);
    }
}

class Init {
    static void main() {
        var b = new Base();
        b.poner();
        b.mostrar();

        var h = new Hijo();
        h.poner();
        h.mostrar();
        h.mostrarHijo();
    }
}
