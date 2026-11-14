///8&exitosamente
// Acceso a atributo de objeto contenido en arreglo y encadenado

class Nodo {
    int valor;
    Nodo sig;

    Nodo(int v) {
        valor = v;
    }
}

class Init {
    static void main() {
        var n = new Nodo(3);
        n.sig = new Nodo(5);
        System.printIln(n.valor + n.sig.valor);
    }
}
