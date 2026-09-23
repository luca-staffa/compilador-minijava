///[SinErrores]
// Logro Visibilidad Mejorada E2

class Vis {
    public int pub;
    private int priv;
    int imp;
    public static void m1() { }
    private static int m2() { return 1; }
    static void m3() { }
    public void m4() { }
    private void m5() { }
    void m6() { }
    public Vis() { }
    public int[] m7(int[] a) { return a; }
    private ClaseBase m8(ClaseBase b) { return b; }
}

interface IV {
    void a();
    public void b();
    private int c();
}
