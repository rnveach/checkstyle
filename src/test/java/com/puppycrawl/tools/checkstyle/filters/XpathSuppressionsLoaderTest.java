////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
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
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.filters;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.puppycrawl.tools.checkstyle.AbstractPathTestSupport;
import com.puppycrawl.tools.checkstyle.TreeWalkerFilter;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.internal.utils.TestUtil;

/**
 * Tests XpathSuppressionsLoader.
 */
public class XpathSuppressionsLoaderTest extends AbstractPathTestSupport {

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/filters/xpathsuppressionsloader";
    }

    @Test
    public void testXpathSuppressions() throws Exception {
        final String fn = getPath("InputXpathSuppressionsLoaderCorrect.xml");
        final Set<TreeWalkerFilter> filterSet = XpathSuppressionsLoader.load(fn);

        final Set<TreeWalkerFilter> expectedFilterSet = new HashSet<>();
        final XpathFilterElement xf0 =
                new XpathFilterElement("file1", "test", null, "id1", "//CLASS_DEF");
        expectedFilterSet.add(xf0);
        final XpathFilterElement xf1 =
                new XpathFilterElement(null, null, "message1", null, "//CLASS_DEF");
        expectedFilterSet.add(xf1);
        assertWithMessage("Multiple xpath suppressions were loaded incorrectly")
            .that(filterSet)
            .isEqualTo(expectedFilterSet);
    }

    @Test
    public void testUnableToFindSuppressions() {
        final String sourceName = "InputXpathSuppressionsLoaderNone.xml";

        try {
            TestUtil.invokeStaticMethod(XpathSuppressionsLoader.class, "load",
                    new InputSource(sourceName), sourceName);
            assertWithMessage("InvocationTargetException is expected").fail();
        }
        catch (ReflectiveOperationException ex) {
            assertWithMessage("Invalid exception cause message")
                .that(ex)
                    .hasCauseThat()
                        .hasMessageThat()
                        .isEqualTo("Unable to find: " + sourceName);
        }
    }

    @Test
    public void testUnableToReadSuppressions() {
        final String sourceName = "InputXpathSuppressionsLoaderNone.xml";

        try {
            TestUtil.invokeStaticMethod(XpathSuppressionsLoader.class, "load",
                    new InputSource(), sourceName);
            assertWithMessage("InvocationTargetException is expected").fail();
        }
        catch (ReflectiveOperationException ex) {
            assertWithMessage("Invalid exception cause message")
                .that(ex)
                    .hasCauseThat()
                        .hasMessageThat()
                        .isEqualTo("Unable to read " + sourceName);
        }
    }

    @Test
    public void testXpathInvalidFileFormat() throws IOException {
        final String fn = getPath("InputXpathSuppressionsLoaderInvalidFile.xml");
        try {
            XpathSuppressionsLoader.load(fn);
            assertWithMessage("Exception should be thrown").fail();
        }
        catch (CheckstyleException ex) {
            assertWithMessage("Invalid error message")
                .that(ex.getMessage())
                .isEqualTo("Unable to parse " + fn
                        + " - invalid files or checks or message format for suppress-xpath");
        }
    }

    @Test
    public void testXpathNoCheckNoId() throws IOException {
        final String fn =
                getPath("InputXpathSuppressionsLoaderNoCheckAndId.xml");
        try {
            XpathSuppressionsLoader.load(fn);
            assertWithMessage("Exception should be thrown").fail();
        }
        catch (CheckstyleException ex) {
            assertWithMessage("Invalid error message")
                .that(ex.getMessage())
                .isEqualTo("Unable to parse " + fn
                        + " - missing checks or id or message attribute for suppress-xpath");
        }
    }

    @Test
    public void testXpathNoCheckYesId() throws Exception {
        final String fn = getPath("InputXpathSuppressionsLoaderId.xml");
        final Set<TreeWalkerFilter> filterSet = XpathSuppressionsLoader.load(fn);

        assertWithMessage("Invalid number of filters")
            .that(filterSet)
            .hasSize(1);
    }

}
