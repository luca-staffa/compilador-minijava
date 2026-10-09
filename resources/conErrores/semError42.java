///[Error:String|8]
// Genericidad avanzada: el argumento anidado debe ser un tipo generico

class Caja<T> {
}

class Uso {
    Caja<String<String>> campo;
}