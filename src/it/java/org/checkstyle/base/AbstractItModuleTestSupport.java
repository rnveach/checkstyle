///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package org.checkstyle.base;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.AbstractPathTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractViolationReporter;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.internal.utils.BriefUtLogger;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public abstract class AbstractItModuleTestSupport extends AbstractPathTestSupport {

    protected static final String ROOT_MODULE_NAME = Checker.class.getSimpleName();

    private static final Pattern WARN_PATTERN = CommonUtil
            .createPattern(".*[ ]*//[ ]*warn[ ]*|/[*]\\*?\\s?warn\\s?[*]/");

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    /**
     * Returns test logger.
     *
     * @return logger for tests
     */
    private BriefUtLogger getBriefUtLogger() {
        return new BriefUtLogger(stream);
    }

    /**
     * Creates a default module configuration {@link DefaultConfiguration} for a given object
     * of type {@link Class}.
     *
     * @param clazz a {@link Class} type object.
     * @return default module configuration for the given {@link Class} instance.
     */
    protected static DefaultConfiguration createModuleConfig(Class<?> clazz) {
        return new DefaultConfiguration(clazz.getName());
    }

    /**
     * Returns {@link Configuration} instance for the given module name pulled
     * from the {@code masterConfig}.
     *
     * @param masterConfig The master configuration to examine.
     * @param moduleName module name.
     * @return {@link Configuration} instance for the given module name.
     * @throws IllegalStateException if there is a problem retrieving the module
     *         or config.
     */
    protected static Configuration getModuleConfig(Configuration masterConfig, String moduleName) {
        final Configuration result;

        final List<Configuration> configs = getModuleConfigsByName(masterConfig, moduleName);
        if (configs.size() == 1) {
            result = configs.get(0);
        }
        else if (configs.isEmpty()) {
            throw new IllegalStateException("no instances of the Module was found: " + moduleName);
        }
        else {
            throw new IllegalStateException("multiple instances of the same Module are detected");
        }

        return result;
    }

    /**
     * Returns a {@link Configuration} instance for the given module IDs in the
     * {@code masterConfig}.
     *
     * @param masterConfig The master configuration to pull results from.
     * @param moduleIds module IDs.
     * @return {@link Configuration} instances.
     * @throws CheckstyleException if there is an error with the config.
     */
    protected static Configuration getModuleConfigById(Configuration masterConfig,
            String... moduleIds) throws CheckstyleException {
        DefaultConfiguration result = null;

        for (Configuration currentConfig : masterConfig.getChildren()) {
            if ("TreeWalker".equals(currentConfig.getName())) {
                for (Configuration moduleConfig : currentConfig.getChildren()) {
                    final String id = getProperty(moduleConfig, "id");
                    if (id != null && isIn(id, moduleIds)) {
                        if (result == null) {
                            result = createTreeWalkerConfig(moduleConfig);
                        }
                        else {
                            result.addChild(moduleConfig);
                        }
                    }
                }
            }
            else {
                final String id = getProperty(currentConfig, "id");
                if (id != null && isIn(id, moduleIds)) {
                    if (result == null) {
                        result = createRootConfig(currentConfig);
                    }
                    else {
                        result.addChild(currentConfig);
                    }
                }
            }
        }

        if (result == null) {
            throw new IllegalStateException(
                    "no instances of the Module was found: " + String.join(", ", moduleIds));
        }

        return result;
    }

    /**
     * Returns a list of all {@link Configuration} instances for the given
     * module name pulled from the {@code masterConfig}.
     *
     * @param masterConfig The master configuration to examine.
     * @param moduleName module name.
     * @return {@link Configuration} instance for the given module name.
     */
    private static List<Configuration> getModuleConfigsByName(Configuration masterConfig,
            String moduleName) {
        final List<Configuration> result = new ArrayList<>();
        for (Configuration currentConfig : masterConfig.getChildren()) {
            if ("TreeWalker".equals(currentConfig.getName())) {
                for (Configuration moduleConfig : currentConfig.getChildren()) {
                    if (moduleName.equals(moduleConfig.getName())) {
                        result.add(createTreeWalkerConfig(moduleConfig));
                    }
                }
            }
            else if (moduleName.equals(currentConfig.getName())) {
                result.add(createRootConfig(currentConfig));
            }
        }
        return result;
    }

    /**
     * Finds the specific property {@code name} in the {@code config}.
     *
     * @param config The configuration to examine.
     * @param name The property name to find.
     * @return The property value or {@code null} if not found.
     * @throws CheckstyleException if there is an error with the config.
     */
    private static String getProperty(Configuration config, String name)
            throws CheckstyleException {
        String result = null;

        if (isIn(name, config.getPropertyNames())) {
            result = config.getProperty(name);
        }

        return result;
    }

    /**
     * Finds the specific ID in a list of IDs.
     *
     * @param find The ID to find.
     * @param list The list of module IDs.
     * @return {@code true} if the ID is in the list.
     */
    private static boolean isIn(String find, String... list) {
        boolean found = false;

        for (String item : list) {
            if (find.equals(item)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Creates {@link Checker} instance based on the given {@link Configuration} instance.
     *
     * @param moduleConfig {@link Configuration} instance.
     * @return {@link Checker} instance based on the given {@link Configuration} instance.
     * @throws Exception if an exception occurs during checker configuration.
     */
    protected final Checker createChecker(Configuration moduleConfig)
            throws Exception {
        final String moduleName = moduleConfig.getName();
        final Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        // make sure the tests always run with English error messages
        // so the tests don't fail in supported locales like German
        final Locale locale = Locale.ENGLISH;
        checker.setLocaleCountry(locale.getCountry());
        checker.setLocaleLanguage(locale.getLanguage());

        if (ROOT_MODULE_NAME.equals(moduleName)) {
            checker.configure(moduleConfig);
        }
        else {
            final Configuration config = createRootConfig(moduleConfig);
            checker.configure(config);
        }

        checker.addListener(getBriefUtLogger());
        return checker;
    }

    /**
     * Creates {@link DefaultConfiguration} for the {@link TreeWalker}
     * based on the given {@link Configuration} instance.
     *
     * @param config {@link Configuration} instance.
     * @return {@link DefaultConfiguration} for the {@link TreeWalker}
     *     based on the given {@link Configuration} instance.
     */
    protected static DefaultConfiguration createTreeWalkerConfig(Configuration config) {
        final DefaultConfiguration twConf = createModuleConfig(TreeWalker.class);
        twConf.addChild(config);
        return twConf;
    }

    /**
     * Creates {@link DefaultConfiguration} for the given {@link Configuration} instance.
     *
     * @param config {@link Configuration} instance.
     * @return {@link DefaultConfiguration} for the given {@link Configuration} instance.
     */
    protected static DefaultConfiguration createRootConfig(Configuration config) {
        final DefaultConfiguration rootConfig = new DefaultConfiguration(ROOT_MODULE_NAME);
        rootConfig.addChild(config);
        return rootConfig;
    }

    /**
     * Returns canonical path for the file with the given file name.
     * The path is formed base on the non-compilable resources location.
     *
     * @param filename file name.
     * @return canonical path for the file with the given file name.
     * @throws IOException if I/O exception occurs while forming the path.
     */
    protected final String getNonCompilablePath(String filename) throws IOException {
        return new File("src/" + getResourceLocation() + "/resources-noncompilable/"
                + getPackageLocation() + "/" + filename).getCanonicalPath();
    }

    /**
     * Performs verification of the file with the given file name. Uses specified configuration.
     * Expected messages are represented by the array of strings, warning line numbers are
     * represented by the array of integers.
     * This implementation uses overloaded
     * {@link AbstractItModuleTestSupport#verify(Checker, File[], String, String[], Integer...)}
     * method inside.
     *
     * @param config configuration.
     * @param fileName file name to verify.
     * @param expected an array of expected messages.
     * @param warnsExpected an array of expected warning numbers.
     * @throws Exception if exception occurs during verification process.
     */
    protected final void verify(Configuration config, String fileName, String[] expected,
            Integer... warnsExpected) throws Exception {
        verify(createChecker(config),
                new File[] {new File(fileName)},
                fileName, expected, warnsExpected);
    }

    /**
     * Performs verification of files.
     * Uses provided {@link Checker} instance.
     *
     * @param checker {@link Checker} instance.
     * @param processedFiles files to process.
     * @param messageFileName message file name.
     * @param expected an array of expected messages.
     * @param warnsExpected an array of expected warning line numbers.
     * @throws Exception if exception occurs during verification process.
     */
    protected final void verify(Checker checker,
            File[] processedFiles,
            String messageFileName,
            String[] expected,
            Integer... warnsExpected)
            throws Exception {
        stream.flush();
        stream.reset();
        final List<File> theFiles = new ArrayList<>();
        Collections.addAll(theFiles, processedFiles);
        final List<Integer> theWarnings = new ArrayList<>();
        Collections.addAll(theWarnings, warnsExpected);
        final int errs = checker.process(theFiles);

        // process each of the lines
        try (ByteArrayInputStream inputStream =
                new ByteArrayInputStream(stream.toByteArray());
            LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int previousLineNumber = 0;
            for (int index = 0; index < expected.length; index++) {
                final String expectedResult = messageFileName + ":" + expected[index];
                final String actual = lnr.readLine();
                assertWithMessage("Error message at position %s of 'expected' does "
                        + "not match actual message", index)
                    .that(actual)
                    .isEqualTo(expectedResult);

                String parseInt = removeDeviceFromPathOnWindows(actual);
                parseInt = parseInt.substring(parseInt.indexOf(':') + 1);
                parseInt = parseInt.substring(0, parseInt.indexOf(':'));
                final int lineNumber = Integer.parseInt(parseInt);
                assertWithMessage(
                        "input file is expected to have a warning comment on line number %s",
                        lineNumber)
                    .that(previousLineNumber == lineNumber
                            || theWarnings.remove((Integer) lineNumber))
                    .isTrue();
                previousLineNumber = lineNumber;
            }

            assertWithMessage("unexpected output: %s", lnr.readLine())
                .that(errs)
                .isEqualTo(expected.length);
            assertWithMessage("unexpected warnings %s", theWarnings)
                .that(theWarnings)
                .isEmpty();
        }

        checker.destroy();
    }

    /**
     * Gets the check message 'as is' from appropriate 'messages.properties'
     * file.
     *
     * @param aClass the package the message is located in.
     * @param messageKey the key of message in 'messages.properties' file.
     * @param arguments  the arguments of message in 'messages.properties' file.
     * @return The message of the check with the arguments applied.
     * @throws IOException if there is a problem loading the property file.
     */
    protected static String getCheckMessage(Class<? extends AbstractViolationReporter> aClass,
            String messageKey, Object... arguments) throws IOException {
        final Properties pr = new Properties();
        pr.load(aClass.getResourceAsStream("messages.properties"));
        final MessageFormat formatter = new MessageFormat(pr.getProperty(messageKey),
                Locale.ROOT);
        return formatter.format(arguments);
    }

    /**
     * Gets the check message 'as is' from appropriate 'messages.properties' file.
     *
     * @param messages the map of messages to scan.
     * @param messageKey the key of message in 'messages.properties' file.
     * @param arguments the arguments of message in 'messages.properties' file.
     * @return The message of the check with the arguments applied.
     */
    protected static String getCheckMessage(Map<String, String> messages, String messageKey,
            Object... arguments) {
        String checkMessage = null;
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            if (messageKey.equals(entry.getKey())) {
                final MessageFormat formatter = new MessageFormat(entry.getValue(), Locale.ROOT);
                checkMessage = formatter.format(arguments);
                break;
            }
        }
        return checkMessage;
    }

    /**
     * Remove device from path string for windows path.
     *
     * @param path path to correct.
     * @return Path without device name.
     */
    private static String removeDeviceFromPathOnWindows(String path) {
        String fixedPath = path;
        final String os = System.getProperty("os.name", "Unix");
        if (os.startsWith("Windows")) {
            fixedPath = path.substring(path.indexOf(':') + 1);
        }
        return fixedPath;
    }

    /**
     * Returns an array of integers which represents the warning line numbers in the file
     * with the given file name.
     *
     * @param fileName file name.
     * @return an array of integers which represents the warning line numbers.
     * @throws IOException if I/O exception occurs while reading the file.
     */
    protected Integer[] getLinesWithWarn(String fileName) throws IOException {
        final List<Integer> result = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(fileName), StandardCharsets.UTF_8)) {
            int lineNumber = 1;
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (WARN_PATTERN.matcher(line).find()) {
                    result.add(lineNumber);
                }
                lineNumber++;
            }
        }
        return result.toArray(new Integer[0]);
    }

    @Override
    protected String getResourceLocation() {
        return "it";
    }

}
