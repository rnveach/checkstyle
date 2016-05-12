import java.io.File;
import java.io.IOException;

import com.puppycrawl.tools.checkstyle.AstTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class TestAllRealFiles {
    static long min = Long.MAX_VALUE;
    static long max = Long.MIN_VALUE;
    static long count = 0;

    public static void main(String[] args) throws IOException, CheckstyleException {
        final long start = System.nanoTime();
        System.out.println("Start: " + start);
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\android-launcher"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\apache-ant"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\apacheapex"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\checkstyle"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\elasticsearch"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\findbugs"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\guava-mvnstyle"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\Hbase"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\hibernate-orm"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\infinispan"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\java-design-patterns"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\jOOL"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\lombok-ast"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\MaterialDesignLibrary"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\nbia-dcm4che-tools"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\Orekit"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\pmd"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\protonpack"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\RxJava"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\sevntu-checkstyle"));
        scan(new File("M:\\antlrWorkspaceEclipse\\regression\\spring-framework"));

        final long end = System.nanoTime();
        System.out.println("End: " + end);
        System.out.println("Total: " + ((end - start) / 1000000000.0d) + " seconds");
        System.out.println("Min: " + ((min) / 1000000000.0d) + " seconds");
        System.out.println("Max: " + ((max) / 1000000000.0d) + " seconds");
        System.out.println("Average: " + (((end - start) / count) / 1000000000.0d) + " seconds");
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
                name.contains("\\checkstyle\\grammars\\InputGrammar.java") || //
                name.contains("\\checkstyle\\InputAstTreeStringPrinter.java") || //
                name.contains("\\checkstyle\\InputIncorrectClass.java") || //
                name.contains("\\apache-ant\\src\\etc\\testcases\\") || //
                name.contains("\\apache-ant\\src\\tests\\") || //
                name.contains("\\apex-app-archetype\\src\\main\\resources\\archetype-resources\\")
                || //
                name.contains("\\hibernate-orm\\documentation\\") || //
                name.contains("\\lombok-ast\\test\\") || //
                name.contains("\\pmd\\pmd-java\\src\\test\\") || //
                false //
        );
    }

    private static void run(File file) throws IOException, CheckstyleException {
        if (skipFile(file.getAbsolutePath()))
            return;

        System.out.println("Working on: " + file.getAbsolutePath());

        final long start = System.nanoTime();
        AstTreeStringPrinter.parseFile(file, false);
        final long end = System.nanoTime();
        final long time = end - start;

        System.out.println("Time: " + (time / 1000000000.0d) + " seconds");
        if (time < min)
            min = time;
        if (time > max)
            max = time;
        count++;
    }
}
