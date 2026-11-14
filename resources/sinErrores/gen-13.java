///6&0&exitosamente
// Arreglo bidimensional: creacion, longitud y acceso

class Init {
    static void main() {
        var m = new int[2][3];
        m[0][0] = 6;
        System.printIln(m.length * m[0].length);
        System.printIln(m[0][0] - 6);
    }
}
