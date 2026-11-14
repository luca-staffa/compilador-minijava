///5&exitosamente
// Llamadas anidadas y encadenadas

class Caja {
    int valor;

    int obtener() {
        return valor;
    }

    Caja identidad() {
        return this;
    }
}

class Init {
    static void main() {
        var c = new Caja();
        c.valor = 5;
        debugPrint(c.identidad().identidad().obtener());
    }
}
