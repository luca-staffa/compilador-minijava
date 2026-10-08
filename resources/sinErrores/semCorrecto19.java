///[SinErrores]
// Sobrecarga por aridad en una interfaz

interface Servicios {
    void ejecutar();

    void ejecutar(int codigo);

    int ejecutar(int codigo, String dato);
}
