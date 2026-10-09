///[SinErrores]
// Genericidad avanzada E2: notacion diamante al instanciar clases genericas

class Caja<T> {
    Caja(T x) {
    }
}

class Par<T> {
}

class Uso {
    Caja<String> a = new Caja<>("hola");
    Caja<Par<String>> b = new Caja<Par<String>>();
}
