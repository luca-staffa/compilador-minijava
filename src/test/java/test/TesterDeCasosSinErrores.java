package test;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import moduloprincipal.ModuloPrincipal;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tester de la catedra para la Etapa 5: compila cada caso de
 * resources/sinErrores/ generando el archivo .out y luego ejecuta la CeIVM
 * comparando su salida con la esperada (primera linea, separada por &).
 */
@RunWith(Parameterized.class)
public class TesterDeCasosSinErrores {

    private static final String msgExito = "[SinErrores]";
    private static final String testFilesDirectoryPath = "resources/sinErrores/";

    // Clase que declara el main del compilador. No es necesario instanciarla.
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

    public TesterDeCasosSinErrores(String input) {
        this.input = input;
    }

    @Test
    public void testIterado() {
        probarExito(input);
    }

    void probarExito(String name) {
        String path = testFilesDirectoryPath + name;
        String[] expectedValues = getExpectedValues(path);

        List<Matcher<? super String>> expectedValuesMatchers = new ArrayList();
        for (String s : expectedValues) {
            expectedValuesMatchers.add(CoreMatchers.containsString(s));
        }
        String generatedFileName = "[" + name + "].out";
        String[] args = {path, generatedFileName};
        init.main(args);
        String result = "";
        try {
            Process proc = Runtime.getRuntime().exec("java -jar CeIVM-cei2011.jar " + generatedFileName);
            result = new BufferedReader(new InputStreamReader(proc.getInputStream())).lines().collect(Collectors.joining("\n"));
            result += new BufferedReader(new InputStreamReader(proc.getErrorStream())).lines().collect(Collectors.joining("\n"));
            result += "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
            proc.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setOut(originalOut);

        System.out.println(outContent.toString());
        System.out.println(result);

        assertThat("Mensaje Incorrecto en: " + path, result, CoreMatchers.allOf(expectedValuesMatchers));
    }

    String[] getExpectedValues(String path) {
        String lineWithTheCode = null;
        try {
            lineWithTheCode = (new BufferedReader(new FileReader(path))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lineWithTheCode = lineWithTheCode.substring(3);
        return lineWithTheCode.split("&");
    }
}
