///10&exitosamente
// Interfaz extendida por otra interfaz y vinculacion dinamica como parametro

interface InterA {
    int valor();
}

interface InterB extends InterA {
    int doble();
}

class Impl implements InterB {
    public int valor() {
        return 5;
    }

    public int doble() {
        return this.valor() * 2;
    }
}

class Init {
    static int usar(InterB b) {
        return b.doble();
    }

    static void main() {
        System.printIln(usar(new Impl()));
    }
}
