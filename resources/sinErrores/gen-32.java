///52&exitosamente
// Herencia encadenada: atributos y metodos de tres niveles

class Aa {
    int a;

    Aa() {
        a = 1;
    }

    int valor() {
        return a;
    }
}

class Bb extends Aa {
    int b;

    Bb() {
        b = 2;
    }

    int valor() {
        return a + b;
    }
}

class Cc extends Bb {
    int c;

    Cc() {
        c = 52;
    }

    int valor() {
        return a + b + c;
    }
}

class Init {
    static void main() {
        var x = new Cc();
        System.printIln(x.valor());
    }
}
