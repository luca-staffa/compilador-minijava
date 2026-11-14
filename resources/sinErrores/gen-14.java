///42&exitosamente
// if/else y while

class Init {
    static void main() {
        var x = 0;
        while (x < 40) {
            if (x < 20) {
                x = x + 2;
            } else {
                x = x + 1;
            }
        }
        debugPrint(x + 2);
    }
}
