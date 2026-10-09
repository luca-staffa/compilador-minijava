///[SinErrores]
// El metodo requerido por la interfaz se satisface con el metodo heredado de
// Object, y una interfaz puede redeclarar un metodo heredado con la misma
// signatura.

interface Mostrable {
    String toString();
}

class Foo implements Mostrable {
}

interface Base {
    void m();
}

interface Der extends Base {
    void m();
}
