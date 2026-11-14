///5&6&7&exitosamente
// Arreglo con parametro de indice variable

class Init {
    static void main() {
        var a = new int[3];
        var i = 0;
        while (i < 3) {
            a[i] = i + 5;
            i = i + 1;
        }
        i = 0;
        while (i < 3) {
            System.printIln(a[i]);
            i = i + 1;
        }
    }
}
