///[SinErrores]
// Interface generica extendida con argumento explicito

interface Fuente<T> {
    T siguiente();
}

interface FuenteString extends Fuente<String> {
}
