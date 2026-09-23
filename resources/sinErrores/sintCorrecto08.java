///[SinErrores]
// Expresiones compuestas, unarias, parentizadas y literales

class Expr {
    void m() {
        var x = -1 + +2 * !3;
        var y = (1 + 2) * (3 - 4) / 5 % 6;
        var z = 1 < 2 && 3 >= 4 || 5 == 6 != false;
        var b = true || false && !true;
        var c = 'a';
        var n = null;
        x = 10;
    }
}
