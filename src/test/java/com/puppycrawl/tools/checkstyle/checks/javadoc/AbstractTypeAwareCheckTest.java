////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.checks.javadoc;

import static com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck.MSG_CLASS_INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public class AbstractTypeAwareCheckTest extends AbstractModuleTestSupport {

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/javadoc/abstracttypeaware";
    }

    @Test
    public void testIsSubclassWithNulls() throws Exception {
        final JavadocMethodCheck check = new JavadocMethodCheck();
        assertFalse("Should return false if at least one of the params is null",
            (boolean) Whitebox.invokeMethod(check, "isSubclass", (Class<?>) null,
                (Class<?>) null));
    }

    @Test
    public void testTokenToString() throws Exception {
        final Class<?> tokenType = Class.forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                + "AbstractTypeAwareCheck$Token");
        final Constructor<?> tokenConstructor = tokenType.getDeclaredConstructor(String.class,
                int.class, int.class);
        final Object token = tokenConstructor.newInstance("blablabla", 1, 1);
        final String result = (String) Whitebox.invokeMethod(token, "toString");
        assertEquals("Invalid toString result", "Token[blablabla(1x1)]", result);
    }

    @Test
    public void testClassRegularClass() throws Exception {
        final Class<?> tokenType = Class.forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                + "AbstractTypeAwareCheck$Token");

        final Class<?> regularClassType = Class
                .forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                        + "AbstractTypeAwareCheck$RegularClass");
        final Constructor<?> regularClassConstructor = regularClassType.getDeclaredConstructor(
                tokenType, String.class, AbstractTypeAwareCheck.class);
        regularClassConstructor.setAccessible(true);

        try {
            regularClassConstructor.newInstance(null, "", new JavadocMethodCheck());
            fail("Exception is expected");
        }
        catch (InvocationTargetException ex) {
            assertTrue("Invalid exception class, expected: IllegalArgumentException.class",
                ex.getCause() instanceof IllegalArgumentException);
            assertEquals("Invalid exception message",
                "ClassInfo's name should be non-null", ex.getCause().getMessage());
        }

        final Constructor<?> tokenConstructor = tokenType.getDeclaredConstructor(String.class,
                int.class, int.class);
        final Object token = tokenConstructor.newInstance("blablabla", 1, 1);

        final JavadocMethodCheck methodCheck = new JavadocMethodCheck();
        final Object regularClass = regularClassConstructor.newInstance(token, "sur",
                methodCheck);

        final String result = (String) Whitebox.invokeMethod(regularClass, "toString");
        final String expected = "RegularClass[name=Token[blablabla(1x1)], in class='sur', check="
                + methodCheck.hashCode() + "," + " loadable=true, class=null]";

        assertEquals("Invalid toString result", expected, result);

        Whitebox.invokeMethod(regularClass, "setClazz", (Class<?>) null);

        assertNull("Expected null", Whitebox.invokeMethod(regularClass, "getClazz"));
    }

    @Test
    public void testClassAliasToString() throws Exception {
        final Class<?> tokenType = Class.forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                + "AbstractTypeAwareCheck$Token");

        final Class<?> regularClassType = Class
                .forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                        + "AbstractTypeAwareCheck$RegularClass");
        final Constructor<?> regularClassConstructor = regularClassType.getDeclaredConstructor(
                tokenType, String.class, AbstractTypeAwareCheck.class);
        regularClassConstructor.setAccessible(true);

        final Constructor<?> tokenConstructor = tokenType.getDeclaredConstructor(String.class,
                int.class, int.class);
        final Object token = tokenConstructor.newInstance("blablabla", 1, 1);

        final Object regularClass = regularClassConstructor.newInstance(token, "sur",
                new JavadocMethodCheck());

        final Class<?> classAliasType = Class.forName(
                "com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractTypeAwareCheck$ClassAlias");
        final Class<?> abstractTypeInfoType = Class
                .forName("com.puppycrawl.tools.checkstyle.checks.javadoc."
                        + "AbstractTypeAwareCheck$AbstractClassInfo");

        final Constructor<?> classAliasConstructor = classAliasType
                .getDeclaredConstructor(tokenType, abstractTypeInfoType);
        classAliasConstructor.setAccessible(true);

        final Object classAlias = classAliasConstructor.newInstance(token, regularClass);
        final String result = (String) Whitebox.invokeMethod(classAlias, "toString");
        assertEquals("Invalid toString result",
            "ClassAlias[alias Token[blablabla(1x1)] for Token[blablabla(1x1)]]", result);
    }

    @Test
    public void testWithoutLogErrors() throws Exception {
        final DefaultConfiguration config = createModuleConfig(JavadocMethodCheck.class);
        config.addAttribute("logLoadErrors", "false");
        config.addAttribute("allowUndeclaredRTE", "true");
        final String[] expected = {
            "7:8: " + getCheckMessage(MSG_CLASS_INFO, "@throws", "InvalidExceptionName"),
        };
        try {
            verify(config, getPath("InputAbstractTypeAwareLoadErrors.java"), expected);
            fail("Exception is expected");
        }
        catch (CheckstyleException ex) {
            final IllegalStateException cause = (IllegalStateException) ex.getCause();
            assertEquals("Invalid exception message",
                getCheckMessage(MSG_CLASS_INFO, "@throws", "InvalidExceptionName"),
                cause.getMessage());
        }
    }

    @Test
    public void testWithSuppressLoadErrors() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(JavadocMethodCheck.class);
        checkConfig.addAttribute("suppressLoadErrors", "true");
        checkConfig.addAttribute("allowUndeclaredRTE", "true");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(checkConfig, getPath("InputAbstractTypeAwareLoadErrors.java"), expected);
    }

}
