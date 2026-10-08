///[SinErrores]
// Redefinicion valida con ancestro declarado despues (referencia adelantada)

class Delta extends Romeo {
    int m() {
        return 1;
    }
}

class Romeo {
    int m() {
        return 0;
    }
}
