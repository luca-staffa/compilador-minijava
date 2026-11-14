///6&exitosamente
// while con cuerpo de bloque que declara variables locales

class Init {
    static void main() {
        var i = 0;
        var suma = 0;
        while (i < 3) {
            var doble = i * 2;
            suma = suma + doble;
            i = i + 1;
        }
        System.printIln(suma);
    }
}
