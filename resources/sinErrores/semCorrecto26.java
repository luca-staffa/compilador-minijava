///[SinErrores]
// Genericidad avanzada: tipos genericos anidados en extends e implements

class Caja<T> {
}

class Par<T> {
}

class Base<T> {
}

class Derivada extends Base<Par<Caja<String>>> {
}

interface Servicio<T> {
    T obtener();
}

class Implementacion implements Servicio<Caja<String>> {
    Caja<String> obtener() {
        return null;
    }
}
