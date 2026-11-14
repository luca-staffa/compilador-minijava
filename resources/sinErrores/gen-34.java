///7&exitosamente
// Metodo void y llamada como sentencia

class Acum {
    int total;
    void sumar(int x) {
        total = total + x;
    }
}

class Init {
    static void main() {
        var a = new Acum();
        a.sumar(3);
        a.sumar(4);
        System.printIln(a.total);
    }
}
