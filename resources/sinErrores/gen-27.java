///9&exitosamente
// this explicito y asignacion de atributo via this

class Cuenta {
    int saldo;

    void depositar(int monto) {
        this.saldo = this.saldo + monto;
    }

    int consultar() {
        return this.saldo;
    }
}

class Init {
    static void main() {
        var c = new Cuenta();
        c.depositar(4);
        c.depositar(5);
        System.printIln(c.consultar());
    }
}
