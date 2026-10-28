///[Error:a|7]
// Acceso implicito a un atributo desde un contexto estatico
class AA {
    int a;

    static void m() {
        a = 1;
    }
}
