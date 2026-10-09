///[SinErrores]
// Un descendiente de una clase que implementa una interfaz hereda Object y
// puede redefinir validamente toString

interface I1 {
    void m();
}

class A1 implements I1 {
    void m() {
    }
}

class B2 extends A1 {
}

class C3 extends B2 {
    String toString() {
        return "c";
    }
}