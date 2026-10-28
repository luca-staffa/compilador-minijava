// Genericos, arreglos, acceso a miembros y sentencias de control
class Caja<T> {
    T contenido;

    Caja(T contenido) {
        this.contenido = contenido;
    }

    T obtener() {
        return contenido;
    }

    void guardar(T valor) {
        contenido = valor;
    }
}

class Uso {
    int[] datos;

    static int doble(int x) {
        return x * 2;
    }

    void m() {
        var caja = new Caja<String>("hola");
        var texto = caja.obtener();
        caja.guardar("mundo");
        datos = new int[3];
        datos[0] = doble(2);
        var largo = datos.length;
        if (largo > 0) { datos[1] = largo; } else { datos[1] = 0; }
        while (largo > 0) { largo = largo - 1; }
        var cadena = "texto".toString();
        var referencia = caja;
        referencia = null;
        texto = cadena;
    }
}
