///[SinErrores]
// Redefinicion valida luego de instanciar el parametro generico

class Contenedor<T> {
    T contenido;

    T m(T x) {
        return x;
    }
}

class ContenedorString extends Contenedor<String> {
    String m(String x) {
        return x;
    }
}
