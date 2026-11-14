///1&2&3&4&5&6&exitosamente
// Matriz recorrida con dos while

class Init {
    static void main() {
        var m = new int[2][3];
        var i = 0;
        while (i < 2) {
            var j = 0;
            while (j < 3) {
                m[i][j] = i * 3 + j + 1;
                j = j + 1;
            }
            i = i + 1;
        }
        i = 0;
        while (i < 2) {
            var j = 0;
            while (j < 3) {
                System.printIln(m[i][j]);
                j = j + 1;
            }
            i = i + 1;
        }
    }
}
