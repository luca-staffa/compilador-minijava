///[Error:m|9]
// Una interfaz no puede redeclarar un metodo heredado con distinta signatura

interface Base {
    void m();
}

interface Der extends Base {
    int m();
}
