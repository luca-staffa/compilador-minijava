///[SinErrores]
// Herencia encadenada y redefinicion exacta de metodo

class Alpha {
    int m(int x) {
        return x;
    }
}

class Beta extends Alpha {
    int m(int y) {
        return y;
    }
}

class Gamma extends Beta {
}
