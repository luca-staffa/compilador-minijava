///[SinErrores]
// Predefinidas: Object, String, System y redefinicion de toString

class Punto {
    int x;
    String nombre;

    String toString() {
        return nombre;
    }
}

class Uso {
    Punto p;
    Object o;
    System consola;
    String texto;
}
