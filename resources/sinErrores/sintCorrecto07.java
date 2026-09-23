///[SinErrores]
// Sentencias: if/else, while, var, return, bloques anidados

class Control {
    static int maximo(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    int suma(int n) {
        var total = 0;
        var i = 1;
        while (i <= n) {
            total = total + i;
            i = i + 1;
        }
        return total;
    }

    void nada() {
        ;
        {
            ;
        }
        return;
    }
}
