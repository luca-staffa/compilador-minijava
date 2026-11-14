///7&exitosamente
// Metodos de instancia, atributos y asignacion de atributo

class Contador {
    int valor;

    void iniciar(int v) {
        valor = v;
    }

    void sumar(int v) {
        valor = valor + v;
    }

    int obtener() {
        return valor;
    }
}

class Init {
    static void main() {
        var c = new Contador();
        c.iniciar(3);
        c.sumar(4);
        debugPrint(c.obtener());
    }
}
