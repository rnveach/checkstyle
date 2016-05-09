import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.puppycrawl.tools.checkstyle.AstTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class PrintAllFilesAst {
    private static final String BASE = "D:\\Rickys\\checkstyleWorkspaceEclipse\\checkstyle\\src\\";
    private static final String OUTPUT_BASE = "D:\\Rickys\\antlrWorkspaceEclipse\\astsPrintOuts\\";

    public static void main(String[] args) throws IOException, CheckstyleException {
        scan(new File(BASE));
    }

    private static void scan(File directory) throws IOException, CheckstyleException {
        if (directory.canRead()) {
            if (directory.isDirectory()) {
                final File[] files = directory.listFiles();

                if (files != null) {
                    for (final File element : files) {
                        scan(element);
                    }
                }
            }
            else if (directory.isFile()) {
                run(directory);
            }
        }
    }

    public static boolean skipFile(String name) {
        return ( //
        !name.endsWith(".java") || //
                // uncompilable file
                "InputGrammar.java".equals(name) || //
                "InputAstTreeStringPrinter.java".equals(name) || //
                "InputIncorrectClass.java".equals(name) || //
        false //
        );
    }

    private static void run(File file) throws IOException, CheckstyleException {
        if (skipFile(file.getName()))
            return;

        System.out.println("Working on: " + file.getAbsolutePath());

        final String results = AstTreeStringPrinter.printFileAst(file, false);

        if (results.length() > 500 * 1024) { // 500kb
            System.out.println("File too large, not saving...");
        }
        else {
            saveAst(results, file.getAbsolutePath(), BASE, OUTPUT_BASE);
        }
    }

    private static void saveAst(String results, String filePath, String basePath,
            String outputDirectory) throws IOException {
        final String fileName = filePath.substring(basePath.length());

        final File file = new File(outputDirectory + Integer.toHexString(fileName.hashCode())
                + ".txt");
        if (file.exists()) {
            System.out.println("File exists: " + fileName + " (" + fileName.hashCode() + ")");
            file.delete();
        }

        final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            bw.write(filePath);
            bw.write("\r\n\r\n");
            bw.write(results);
        }
        finally {
            bw.close();
        }
    }
}
