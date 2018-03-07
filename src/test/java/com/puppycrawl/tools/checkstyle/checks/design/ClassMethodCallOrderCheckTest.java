
package com.puppycrawl.tools.checkstyle.checks.design;

import static com.puppycrawl.tools.checkstyle.checks.coding.TooManyReassignmentsCheck.MSG_KEY;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.checks.coding.TooManyReassignmentsCheck;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class ClassMethodCallOrderCheckTest extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/design/classmethodcallorder";
    }

    @Test
    public void testValid() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(ClassMethodCallOrderCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("ClassMethodCallOrderValid.java"), expected);
    }

    @Test
    public void testInvalid() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(ClassMethodCallOrderCheck.class);
        final String[] expected = {
        };
        verify(checkConfig, getPath("ClassMethodCallOrderInvalid.java"), expected);
    }
}
