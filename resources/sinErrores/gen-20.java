///3&exitosamente
// Arreglo de referencias y encadenado

class Caja {
    int v;

    Caja(int x) {
        v = x;
    }
}

class Init {
    static void main() {
        var cajas = new Caja[2];
        cajas[0] = new Caja(1);
        cajas[1] = new Caja(2);
        System.printIln(cajas[0].v + cajas[1].v);
    }
}
