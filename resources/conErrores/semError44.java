///[Error:NoGen|8]
// Genericidad avanzada E2: el diamante solo aplica al instanciar clases genericas

class NoGen {
}

class Uso {
    NoGen c = new NoGen<>();
}
