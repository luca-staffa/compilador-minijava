package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import moduloprincipal.ModuloPrincipal;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tester de regresion del analisis semantico (Etapa 4): los casos de
 * resources/sinErroresSemantico/ deben compilar sin errores. No ejecuta la
 * CeIVM porque estos casos no declaran un main.
 */
@RunWith(Parameterized.class)
public class TesterDeCasosSinErroresSemantico {

    private static final String msgExito = "[SinErrores]";
    private static final String testFilesDirectoryPath = "resources/sinErroresSemantico/";

    private static final ModuloPrincipal init = null;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpClass() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDownClass() {
        System.setOut(originalOut);
    }

    @Parameters(name = "{0}")
    public static Iterable<? extends Object> data() {
        File folder = new File(testFilesDirectoryPath);
        ArrayList<String> names = new ArrayList();
        for (File f : folder.listFiles()) {
            names.add(f.getName());
        }
        names.sort(String::compareTo);
        return names;
    }

    private final String input;

    public TesterDeCasosSinErroresSemantico(String input) {
        this.input = input;
    }

    @Test
    public void testIterado() {
        String path = testFilesDirectoryPath + input;
        String[] args = {path};
        init.main(args);
        assertThat("Mensaje Incorrecto en: " + path, outContent.toString(),
                CoreMatchers.containsString(msgExito));
    }
}
