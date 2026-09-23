///[SinErrores]
// Referencias encadenadas, arreglos y accesos con indice

class Refs {
    int[] arreglo;
    Refs siguiente;

    void m() {
        var a = new int[10];
        var m = new int[3][4];
        arreglo[0] = 1;
        siguiente.siguiente.m();
        this.arreglo[1 + 2] = this.siguiente.arreglo[3];
        var r = a[0] + m[1][2];
    }
}
