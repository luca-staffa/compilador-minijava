///10&exitosamente
// Bloques anidados con variables locales y reutilizacion de offsets

class Init {
    static void main() {
        var total = 0;
        {
            var a = 4;
            {
                var b = 6;
                total = a + b;
            }
        }
        System.printIln(total);
    }
}
