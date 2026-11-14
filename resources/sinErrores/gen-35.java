///10&exitosamente
// Interface como tipo de retorno y argumento

interface Func {
    int aplicar(int x);
}

class Inc implements Func {
    public int aplicar(int x) {
        return x + 1;
    }
}

class Init {
    static int usar(Func f) {
        return f.aplicar(9);
    }
    static Func crear() {
        return new Inc();
    }
    static void main() {
        System.printIln(usar(crear()));
    }
}
