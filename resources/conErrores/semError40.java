///[Error:toString|8]
// Redefinicion invalida de toString heredado de Object en un descendiente indirecto

class A1 {
}

class B2 extends A1 {
    int toString() {
        return 0;
    }
}
