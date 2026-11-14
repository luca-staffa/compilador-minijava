///11&exitosamente
// Metodo de instancia con dos parametros y retorno

class Op {
    int combinar(int a, int b) {
        return a * 3 + b;
    }
}

class Init {
    static void main() {
        var o = new Op();
        System.printIln(o.combinar(2, 5));
    }
}
