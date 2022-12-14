package com.puppycrawl.tools.checkstyle.checks.javadoc;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public class EmptyJavadocDescriptionCheckTest extends AbstractModuleTestSupport {

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/javadoc/emptyjavadocdescription";
    }

    @Test
    public void testJavadocStyleDefaultSettingsOne() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWithInlineConfigParser(getPath("InputEmptyJavadocDescription.java"), expected);
    }

}
