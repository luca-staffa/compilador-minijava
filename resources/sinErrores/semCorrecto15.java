///[SinErrores]
// Interfaz que extiende interfaz e implementacion de toda la jerarquia

interface Base {
    void a();
}

interface Der extends Base {
    void b();
}

class Impl implements Der {
    void a() {
    }

    void b() {
    }
}
