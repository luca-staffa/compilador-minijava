///42&42&exitosamente
// Metodo estatico invocado por nombre de clase y por receptor

class Util {
    static int doble(int n) {
        return n * 2;
    }
}

class Init {
    static void main() {
        var u = new Util();
        System.printIln(Util.doble(21));
        System.printIln(u.doble(21));
    }
}
