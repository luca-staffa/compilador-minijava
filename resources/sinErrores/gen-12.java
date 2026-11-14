///4&exitosamente
// Arreglo como parametro y longitud

class Util {
    static int sumar(int[] datos) {
        var suma = 0;
        var i = 0;
        while (i < datos.length) {
            suma = suma + datos[i];
            i = i + 1;
        }
        return suma;
    }
}

class Init {
    static void main() {
        var a = new int[3];
        a[0] = 1;
        a[1] = 1;
        a[2] = 2;
        debugPrint(Util.sumar(a));
    }
}
