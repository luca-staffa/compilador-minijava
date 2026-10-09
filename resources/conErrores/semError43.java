///[Error:NoExiste|8]
// Genericidad avanzada: tipo anidado inexistente

class Caja<T> {
}

class Uso {
    Caja<Caja<NoExiste>> campo;
}