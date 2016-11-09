package com.puppycrawl.tools.checkstyle.checks.javadoc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.Main;

public class MultiJavadocCheckTest extends BaseCheckTestSupport {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Rule
    public final SystemErrRule systemErr = new SystemErrRule().enableLog().mute();
    @Rule
    public final SystemOutRule systemOut = new SystemOutRule().enableLog().mute();

    @Override
    protected String getPath(String filename) throws IOException {
        return super.getPath("checks" + File.separator + "javadoc" + File.separator + filename);
    }

    @Test
    public void testExcludeOption() throws Exception {
        final String input = getPath("InputMultiJavadocCheck.java");

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertEquals(
                    "Starting audit..." + System.lineSeparator() + "[ERROR] " + input
                            + ":7: Javadoc comment at column 38 has parse error. "
                            + "Details: no viable alternative at input '<tom.hombergs@' "
                            + "while parsing HTML_ELEMENT [AtclauseOrder]"
                            + System.lineSeparator() + "Audit done." + System.lineSeparator()
                            + "Checkstyle ends with 1 errors." + System.lineSeparator(),
                    systemOut.getLog());
            assertEquals("", systemErr.getLog());
        });
        Main.main("-c", getPath("multi-javadoc-config.xml"), input);
    }
}
