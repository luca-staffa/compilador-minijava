///[Error:toString|13]
// Una clase que implementa una interfaz tambien extiende Object: no puede
// declarar un toString incompatible

interface I1 {
    void m();
}

class A1 implements I1 {
    void m() {
    }

    int toString() {
        return 0;
    }
}
