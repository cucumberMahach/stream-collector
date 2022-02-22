package util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class DataUtil {

    public static Pair<PrintStream,ByteArrayOutputStream> getPrintStream(){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        return new Pair<>(ps, baos);
    }

}
