///[Error:Caja|9]
// Creacion de una entidad generica sin argumento de tipo (tipo raw)
class Caja<T> {
    T contenido;
}

class Uso {
    void m() {
        var caja = new Caja();
    }
}
