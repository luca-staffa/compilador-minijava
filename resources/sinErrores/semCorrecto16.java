///[SinErrores]
// Los metodos estaticos heredados no participan de la redefinicion

class Alpha {
    static void s() {
    }

    void instancia() {
    }
}

class Beta extends Alpha {
    static void s2() {
    }
}
