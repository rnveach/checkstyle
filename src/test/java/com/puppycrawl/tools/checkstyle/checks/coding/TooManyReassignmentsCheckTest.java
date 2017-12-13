package com.puppycrawl.tools.checkstyle.checks.coding;

import static com.puppycrawl.tools.checkstyle.checks.coding.TooManyReassignmentsCheck.MSG_KEY;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;

public class TooManyReassignmentsCheckTest extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/coding/toomanyreassignments";
    }

    @Test
    public void testValid() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(TooManyReassignmentsCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("InputTooManyReassignmentsValid.java"), expected);
    }

    @Test
    public void testInvalid() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(TooManyReassignmentsCheck.class);
        final String[] expected = {
            "5:13: " + getCheckMessage(MSG_KEY, "a", 4, 3),
            "13:13: " + getCheckMessage(MSG_KEY, "a", 4, 3),
            "19:9: " + getCheckMessage(MSG_KEY, "field", 4, 3),
        };
        verify(checkConfig, getPath("InputTooManyReassignmentsInvalid.java"), expected);
    }
}
