///[SinErrores]
// Clase generica: parametro de tipo en atributos, retornos y parametros

class Caja<T> {
    T contenido;

    T obtener() {
        return contenido;
    }

    void poner(T x) {
    }
}

class Uso {
    Caja<String> c;
    Caja<Uso> d;
    Caja cruda;
}
