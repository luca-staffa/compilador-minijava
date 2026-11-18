///1&2&3&4&exitosamente
// El lenguaje CeIASM no distingue mayusculas de minusculas: las etiquetas de
// getX/getx y de las clases Aa/AA no deben colisionar.
class Aa {
    int getX() {
        return 1;
    }
    int getx() {
        return 2;
    }
    static void main() {
        var a = new Aa();
        var b = new AA();
        System.printIln(a.getX());
        System.printIln(a.getx());
        System.printIln(b.getX());
        System.printIln(b.getx());
    }
}
class AA {
    int getX() {
        return 3;
    }
    int getx() {
        return 4;
    }
}
