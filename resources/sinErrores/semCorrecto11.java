///[SinErrores]
// Herencia generica con argumento explicito (instanciacion de miembros)

class Alpha<T> {
    T dato;

    T obtener() {
        return dato;
    }
}

class Beta extends Alpha<String> {
}
