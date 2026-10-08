///[SinErrores]
// Interfaz generica implementada con argumento explicito

interface Repo<T> {
    T buscar(int id);
}

class RepoString implements Repo<String> {
    String buscar(int id) {
        return null;
    }
}
