///[SinErrores]
// Interface con genericidad, extension y varios metodos

interface Lista<E> extends Coleccion<E> {
    void agregar(E elem);
    E obtener(int indice);
    int tamanio();
    boolean esVacia();
}

interface Coleccion<T> {
    void agregar(T elem);
}
