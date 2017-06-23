////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.internal;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.utils.ModuleReflectionUtils;

public class VersionRegressionTest {
    private static final String JAR_FOLDER = //
    // "C:\\Users\\048723\\Downloads\\";
    "M:\\checkstyleWorkspace\\jars\\";

    private static final String[] VERSIONS = new String[] {
            // doesn't have the severity on the Checker
            // "3.0",
            "3.1", "3.2",
            "3.3",
            "3.4",
            "3.5", //
            "4.0", "4.1",
            "4.2",
            "4.3",
            "4.4", //
            "5.0", "5.1", "5.2", "5.3", "5.4", "5.5", "5.6", "5.7", "5.8", "5.9", "6.0", "6.1",
            "6.1.1", "6.2",
            "6.3",
            "6.4",
            "6.4.1",
            "6.5",
            "6.6",
            // can't instantiate module bug, just says `Checkstyle ends with 1
            // errors.`
            // "6.7",
            "6.8", "6.8.1", "6.8.2", "6.9", "6.10", "6.10.1", "6.11", "6.11.1", "6.11.2", "6.12",
            "6.12.1", "6.13", "6.14", "6.14.1", "6.15", "6.16", "6.16.1", "6.17", "6.18",
            "6.19", //
            "7.0", "7.1", "7.1.1", "7.1.2", "7.2", "7.3", "7.4", "7.5", "7.5.1", "7.6", "7.6.1",
            "7.7", "7.8", "7.8.1", "7.8.2"
    };

    private static final File CONFIG = new File("M:\\config.xml");
    private static final File DUMMY = new File("M:\\dummy.java");
    private static final File PACKAGE_INFO = new File("M:\\package-info.java");
    private static final File TEST_FILE = new File("abc");

    private static boolean DISPLAY_ALL_VERSIONS = true;

    private static boolean DISPLAY_COMMAND = true;
    private static boolean DEBUG = true;

    // true;

    @Test
    public void testAllModulesAreReferencedInConfigFile() throws Exception {
        FileUtils.writeStringToFile(DUMMY,
                "public final class TestClass {\n    private TestClass() {}\n}",
                Charset.defaultCharset());
        FileUtils.writeStringToFile(PACKAGE_INFO, "", Charset.defaultCharset());

        final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();

        for (Class<?> module : modules) {
            System.out.println(module.getSimpleName());
        }

        System.out.println();
        System.out.println();

        for (Class<?> module : modules) {
            final Set<String> properties = getProperties(module);

            runValidation(module);

            for (String property : properties) {
                runValidation(module, property);
            }
        }
    }

    private static void runValidation(Class<?> module) throws Exception {
        runValidation(module, null);
    }

    private static void runValidation(Class<?> module, String property) throws Exception {
        if (TEST_FILE.exists()) {
            TEST_FILE.delete();
        }

        createConfiguration(module, property);

        if (DISPLAY_COMMAND) {
            System.out.println("Starting: " + module.getSimpleName() + ","
                    + (property == null ? "" : property));
        }

        final String result = runConfiguration();

        System.out.println(module.getSimpleName() + "\t" + (property == null ? "" : property)
                + "\t" + result);
    }

    private static void createConfiguration(Class<?> module, String property) throws Exception {
        final String simpleName = module.getSimpleName();

        try (PrintWriter out = new PrintWriter(CONFIG)) {
            out.println("<?xml version=\"1.0\"?>");
            out.println("<!DOCTYPE module PUBLIC");
            out.println("    \"-//Puppy Crawl//DTD Check Configuration 1.3//EN\"");
            out.println("    \"http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd\">");
            out.println("");

            if (ModuleReflectionUtils.isRootModule(module)) {
                out.println("<module name=\"" + simpleName + "\">");
            }
            else {
                out.println("<module name=\"Checker\">");
            }

            if (!ModuleReflectionUtils.isRootModule(module)) {
                out.println("  <property name=\"severity\" value=\"ignore\"/>");
                out.println();

                if (ModuleReflectionUtils.isCheckstyleCheck(module)) {
                    out.println("  <module name=\"TreeWalker\">");
                }

                out.println("    <module name=\"" + simpleName + "\">");
            }

            if (property != null) {
                // dummy value, should use exception to determine existence or
                // not
                out.println("      <property name=\"" + property + "\" value=\"abc\"/>");
            }

            if (!ModuleReflectionUtils.isRootModule(module)) {
                out.println("    </module>");

                if (ModuleReflectionUtils.isCheckstyleCheck(module)) {
                    out.println("  </module>");
                }
            }

            out.println("</module>");
            out.println("");
        }
    }

    private static String runConfiguration() throws Exception {
        String result = "";

        final String dummyPath = DUMMY.getAbsolutePath();
        final String configPath = CONFIG.getAbsolutePath();

        if (DEBUG) {
            System.out.println("-----------------------------------------");
        }

        for (String version : VERSIONS) {
            final float versionFloat = getVersionFloat(version);
            final String[] command = new String[] {
                    "java",
                    "-jar",
                    JAR_FOLDER
                            + (versionFloat < 5.2f ? "checkstyle-all-" + version + ".jar"
                                    : "checkstyle-" + version + "-all.jar"), dummyPath, "-c",
                    configPath,
            };

            if (DISPLAY_COMMAND) {
                System.out.print("Command:");

                for (String s : command) {
                    System.out.print(" " + s);
                }

                System.out.println();
            }

            final InternalProcess process = new InternalProcess(command, version);

            if (process.getProcess() == null)
                continue;

            final int returnCode = process.getReturnCode();
            final String errorStream = process.getErrorStream();
            final String inputStream = process.getInputStream();

            if (DEBUG) {
                System.out.println(inputStream);
                System.out.println(errorStream);
                System.out.println("RC: " + returnCode);

                System.out.println("-----------------------------------------");
            }

            if (returnCode == 0) {
                if (result.length() != 0) {
                    result += ", ";
                }
                result += version;

                if (!DISPLAY_ALL_VERSIONS) {
                    break;
                }
            }
            else {
                // good
                if (errorStream
                        .contains("org.apache.commons.beanutils.ConversionException: Can't convert value 'abc' to")
                        || errorStream
                                .contains("com.puppycrawl.tools.checkstyle.api.CheckstyleException: illegal value 'abc' for property ")
                        || errorStream
                                .contains("Unable to create Checker: illegal value 'abc' for property ")
                        || errorStream
                                .contains("com.puppycrawl.tools.checkstyle.api.CheckstyleException: Cannot set property ")
                        || errorStream.contains("illegal value 'abc' for property ")
                        || errorStream
                                .contains("java.lang.IllegalArgumentException: No enum constant com.puppycrawl.tools.checkstyle")
                        || errorStream
                                .contains("java.io.UnsupportedEncodingException: unsupported charset: 'abc'")
                        || errorStream
                                .contains("java.lang.IllegalArgumentException: 'other' is different type of Path")
                        || errorStream.contains("abc (The system cannot find the file specified)")
                        || errorStream.contains(" to 'abc': abc is not an absolute path")
                        || errorStream.contains(" to 'abc': unsupported charset: 'abc'")
                        || errorStream
                                .contains("Cannot set property 'charset' to 'abc' in module ")
                        || errorStream
                                .contains("com.puppycrawl.tools.checkstyle.api.CheckstyleException: Unable to find: abc")
                        || errorStream
                                .contains("com.puppycrawl.tools.checkstyle.api.CheckstyleException: Unable to parse abc - ")
                        || errorStream
                                .contains("Exception in thread \"main\" java.lang.IllegalArgumentException: abc\n\tat com.puppycrawl.tools.checkstyle.api.SeverityLevel.getInstance")
                        || errorStream
                                .contains("Exception in thread \"main\" java.lang.IllegalArgumentException: abc\r\n\tat com.puppycrawl.tools.checkstyle.api.SeverityLevel.getInstance")
                        || errorStream
                                .contains("org.apache.commons.beanutils.PropertyUtilsBean invokeMethod\nSEVERE: Method invocation failed.\njava.lang.IllegalArgumentException: argument type mismatch")
                        || errorStream
                                .contains("org.apache.commons.beanutils.PropertyUtilsBean invokeMethod\r\nSEVERE: Method invocation failed.\r\njava.lang.IllegalArgumentException: argument type mismatch")
                        || errorStream
                                .contains("Exception in thread \"main\" java.lang.NullPointerException\n\tat java.util.regex.Pattern.<init>(Pattern.java:")
                        || errorStream
                                .contains("Exception in thread \"main\" java.lang.NullPointerException\r\n\tat java.util.regex.Pattern.<init>(Pattern.java:")
                        || errorStream
                                .contains("java.lang.NullPointerException\n\tat java.util.regex.Pattern.<init>(Pattern.java:")
                        || errorStream
                                .contains("java.lang.NullPointerException\r\n\tat java.util.regex.Pattern.<init>(Pattern.java:")
                        // illegal token
                        || errorStream
                                .contains("java.lang.IllegalArgumentException: given name abc")
                        || errorStream
                                .contains("java.lang.IllegalArgumentException: Unknown javadoc token name. Given name abc")) {
                    if (result.length() != 0) {
                        result += ", ";
                    }
                    result += version;

                    if (!DISPLAY_ALL_VERSIONS) {
                        break;
                    }
                }
                else {
                    if (
                    // com.puppycrawl.tools.checkstyle.api.CheckstyleException:
                    // Property 'XXXX' in module
                    // XXXCheck does not exist, please check the
                    // documentation
                    !errorStream.contains(" does not exist, please check the documentation")//
                            && !errorStream
                                    .contains("com.puppycrawl.tools.checkstyle.api.CheckstyleException: cannot initialize module ")
                            && !errorStream
                                    .contains("Unable to create Checker: cannot initialize module ")
                            && !errorStream.contains("cannot initialize module ")) {
                        System.out.println("Unknown error: " + errorStream);
                    }
                }
            }

            process.destroy();
        }

        return result;
    }

    private static float getVersionFloat(String version) {
        final String[] split = version.split("\\.", 3);

        if (split.length == 2) {
            return Float.parseFloat(version);
        }

        return Float.parseFloat(split[0] + "." + split[1]);
    }

    private static Set<String> getProperties(Class<?> clss) {
        final Set<String> result = new TreeSet<>();
        final PropertyDescriptor[] map = PropertyUtils.getPropertyDescriptors(clss);

        for (PropertyDescriptor p : map) {
            if (p.getWriteMethod() != null) {
                result.add(p.getName());
            }
        }

        return result;
    }

    private static final class InternalProcess {
        private Process process;
        private String errorStream;
        private String inputStream;
        private int returnCode;

        public InternalProcess(String[] command, String version) throws Exception {
            process = null;

            for (int i = 0; (i < 5) && (process == null); i++) {
                process = new ProcessBuilder(command).start();

                if (!process.waitFor(20, TimeUnit.SECONDS)) {
                    System.err.println("Timeout issue on " + version);

                    destroy();

                    process = null;
                    Thread.sleep(1000);
                    continue;
                }

                returnCode = process.exitValue();
                errorStream = toString(process.getErrorStream());
                inputStream = toString(process.getInputStream());

                if (errorStream.length() == 0) {
                    errorStream = inputStream;
                    inputStream = "";
                }

                if (errorStream.startsWith("Error: Unable to access jarfile")) {
                    throw new Exception(errorStream);
                }

                if (errorStream.contains("unable to read " + CONFIG.getAbsolutePath())) {
                    System.err.println("Config issue on " + version);

                    destroy();

                    process = null;
                    Thread.sleep(5000);
                    continue;
                }
            }
        }

        public void destroy() {
            if (process != null) {
                try {
                    process.destroyForcibly();
                }
                catch (Exception ex) {
                    // ignore
                }
            }
        }

        private static String toString(InputStream inputStream) throws IOException {
            final StringBuilder sb = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line + System.getProperty("line.separator"));
                }
            }

            return sb.toString();
        }

        public Process getProcess() {
            return process;
        }

        public String getErrorStream() {
            return errorStream;
        }

        public String getInputStream() {
            return inputStream;
        }

        public int getReturnCode() {
            return returnCode;
        }
    }
}
