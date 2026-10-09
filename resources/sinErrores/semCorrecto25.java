///[SinErrores]
// Genericidad avanzada: tipos genericos anidados en atributos

class Caja<T> {
}

class Par<T> {
}

class Uso<T> {
    Caja<Par<String>> campo;
    Par<Caja<T>> otro;
}
