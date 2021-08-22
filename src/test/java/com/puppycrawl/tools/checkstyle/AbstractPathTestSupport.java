////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
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

package com.puppycrawl.tools.checkstyle;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;

public abstract class AbstractPathTestSupport {

    // we are using positive lookahead here, to convert \r\n to \n
    // and \\r\\n to \\n (for parse tree dump files),
    // by replacing the full match with the empty string
    private static final String CR_FOLLOWED_BY_LF_REGEX = "(?x)\\\\r(?=\\\\n)|\\r(?=\\n)";

    private static final String EOL = System.lineSeparator();

    /**
     * The stack size used in {@link #executeWithLimitedStackSizeAndTimeout(Executable)}.
     * This value should be as small as possible. Some JVM requires this value to be
     * at least 144k.
     *
     * @see <a href="https://www.baeldung.com/jvm-configure-stack-sizes">
     *      Configuring Stack Sizes in the JVM</a>
     */
    private static final int MINIMAL_STACK_SIZE = 147456;

    /**
     * Returns the exact location for the package where the file is present.
     *
     * @return path for the package name for the file.
     */
    protected abstract String getPackageLocation();

    /**
     * Retrieves the name of the folder location for resources.
     *
     * @return The name of the folder.
     */
    protected String getResourceLocation() {
        return "test";
    }

    /**
     * Returns canonical path for the file with the given file name.
     * The path is formed base on the root location.
     * This implementation uses 'src/test/resources/'
     * as a root location.
     *
     * @param filename file name.
     * @return canonical path for the file name.
     * @throws IOException if I/O exception occurs while forming the path.
     */
    protected final String getPath(String filename) throws IOException {
        return new File("src/" + getResourceLocation() + "/resources/" + getPackageLocation() + "/"
                + filename).getCanonicalPath();
    }

    protected final String getResourcePath(String filename) {
        return "/" + getPackageLocation() + "/" + filename;
    }

    /**
     * Reads the contents of a file.
     *
     * @param filename the name of the file whose contents are to be read
     * @return contents of the file with all {@code \r\n} replaced by {@code \n}
     * @throws IOException if I/O exception occurs while reading
     */
    protected static String readFile(String filename) throws IOException {
        return toLfLineEnding(new String(Files.readAllBytes(
                Paths.get(filename)), StandardCharsets.UTF_8));
    }

    /**
     * Join given strings with {@link #EOL} delimiter and add EOL at the end.
     *
     * @param strings strings to join
     * @return joined strings
     */
    public static String addEndOfLine(String... strings) {
        return Stream.of(strings).collect(Collectors.joining(EOL, "", EOL));
    }

    protected static String toLfLineEnding(String text) {
        return text.replaceAll(CR_FOLLOWED_BY_LF_REGEX, "");
    }

    /**
     * Executes a test method in a thread with limited stack size.
     *
     * @param executable the method to execute
     * @throws Exception 
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-2.html#jvms-2.5.2">
     *      JVMS &sect;2.5.2</a>
     */
    public static void executeWithLimitedStackSizeAndTimeout(final Executable executable) throws Exception {
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        Void result = null;
        // We return null here, which gives us a result to make an assertion about
        result = getResultWithLimitedResources(() -> {
            try {
                executable.execute();
            }
            catch (Throwable e) {
                exception.set(e);
            }
            return null;
        });
        assertWithMessage("Verify should complete successfully.")
                .that(result)
                .isNull();

        final Throwable ex = exception.get();
        if (ex != null) {
            if (ex instanceof Exception)
                throw (Exception) ex;

            throw new IllegalStateException(ex);
        }

//        final Thread thread = new Thread(null, () -> {
//            assertDoesNotThrow(executable, "No exception expected");
//        }, "LimitedStackThread", MINIMAL_STACK_SIZE);
//        assertDoesNotThrow(() -> thread.join(EXECUTION_TIMEOUT),
//            "The worker thread should finish in the time alloted time");
    }

    /**
     * Runs a given task with limited stack size and time duration, then
     * returns the result. See AbstractModuleTestSupport#verifyWithLimitedResources
     * for an example of how to use this method when task does not return a result, i.e.
     * the given method's return type is {@code void}.
     *
     * @param callable the task to execute
     * @param <V> return type of task - {@code Void} if task does not return result
     * @return result
     * @throws Exception if getting result fails
     */
    public static <V> V getResultWithLimitedResources(Callable<V> callable) throws Exception {
        final FutureTask<V> futureTask = new FutureTask<>(callable);
        final Thread thread = new Thread(null, futureTask,
                "LimitedStackSizeThread", MINIMAL_STACK_SIZE);
        thread.start();
        return futureTask.get(10, TimeUnit.SECONDS);
    }

}
