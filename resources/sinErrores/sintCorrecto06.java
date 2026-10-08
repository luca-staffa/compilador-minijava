///[SinErrores]
// Clase generica con herencia, atributos de varios tipos y constructor

class Mapa<K> extends Diccionario<K> {
    int tamanio;
    boolean[] flags;
    Mapa<K> otro;
    K clave;

    public Mapa(int cap) {
        tamanio = cap;
    }
}

class Diccionario<C> {
}
