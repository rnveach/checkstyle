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

package com.puppycrawl.tools.checkstyle;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.AbstractPathTestSupport.addEndOfLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class XpathTreeStringPrinterTest {

    @TempDir
    public File tempFolder;

    @Test
    public void testPrintXpathNotComment() throws Exception {
        final String fileContent = "class Test { public void method() {int a = 5;}}";
        final File file = File.createTempFile("junit", null, tempFolder);
        Files.write(file.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        final String expected = addEndOfLine(
            "COMPILATION_UNIT -> COMPILATION_UNIT [1:0]",
            "`--CLASS_DEF -> CLASS_DEF [1:0]",
            "    `--OBJBLOCK -> OBJBLOCK [1:11]",
            "        |--METHOD_DEF -> METHOD_DEF [1:13]",
            "        |   `--SLIST -> { [1:34]",
            "        |       |--VARIABLE_DEF -> VARIABLE_DEF [1:35]",
            "        |       |   |--IDENT -> a [1:39]");
        final String result = XpathTreeStringPrinter.printXpathBranch(
            "//CLASS_DEF//METHOD_DEF//VARIABLE_DEF//IDENT", file);
        assertWithMessage("Branch string is different")
            .that(result)
            .isEqualTo(expected);
    }

    @Test
    public void testPrintXpathComment() throws Exception {
        final String fileContent = "class Test { /* comment */ }";
        final File file = File.createTempFile("junit", null, tempFolder);
        Files.write(file.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        final String expected = addEndOfLine(
            "COMPILATION_UNIT -> COMPILATION_UNIT [1:0]",
            "`--CLASS_DEF -> CLASS_DEF [1:0]",
            "    `--OBJBLOCK -> OBJBLOCK [1:11]",
            "        |--BLOCK_COMMENT_BEGIN -> /* [1:13]");
        final String result = XpathTreeStringPrinter.printXpathBranch(
            "//CLASS_DEF//BLOCK_COMMENT_BEGIN", file);
        assertWithMessage("Branch string is different")
            .that(result)
            .isEqualTo(expected);
    }

    @Test
    public void testPrintXpathTwo() throws Exception {
        final String fileContent = "class Test { public void method() {int a = 5; int b = 5;}}";
        final File file = File.createTempFile("junit", null, tempFolder);
        Files.write(file.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        final String expected = addEndOfLine(
            "COMPILATION_UNIT -> COMPILATION_UNIT [1:0]",
            "`--CLASS_DEF -> CLASS_DEF [1:0]",
            "    `--OBJBLOCK -> OBJBLOCK [1:11]",
            "        |--METHOD_DEF -> METHOD_DEF [1:13]",
            "        |   `--SLIST -> { [1:34]",
            "        |       |--VARIABLE_DEF -> VARIABLE_DEF [1:35]",
            "        |       |   |--IDENT -> a [1:39]",
            "---------",
            "COMPILATION_UNIT -> COMPILATION_UNIT [1:0]",
            "`--CLASS_DEF -> CLASS_DEF [1:0]",
            "    `--OBJBLOCK -> OBJBLOCK [1:11]",
            "        |--METHOD_DEF -> METHOD_DEF [1:13]",
            "        |   `--SLIST -> { [1:34]",
            "        |       |--VARIABLE_DEF -> VARIABLE_DEF [1:46]",
            "        |       |   |--IDENT -> b [1:50]");
        final String result = XpathTreeStringPrinter.printXpathBranch(
            "//CLASS_DEF//METHOD_DEF//VARIABLE_DEF//IDENT", file);
        assertWithMessage("Branch string is different")
            .that(result)
            .isEqualTo(expected);
    }

    @Test
    public void testInvalidXpath() throws IOException {
        final String fileContent = "class Test { public void method() {int a = 5; int b = 5;}}";
        final File file = File.createTempFile("junit", null, tempFolder);
        Files.write(file.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        final String invalidXpath = "\\//CLASS_DEF"
                + "//METHOD_DEF//VARIABLE_DEF//IDENT";
        try {
            XpathTreeStringPrinter.printXpathBranch(invalidXpath, file);
            assertWithMessage("Should end with exception").fail();
        }
        catch (CheckstyleException ex) {
            final String expectedMessage =
                "Error during evaluation for xpath: " + invalidXpath
                    + ", file: " + file.getCanonicalPath();
            assertWithMessage("Exception message is different")
                .that(ex.getMessage())
                .isEqualTo(expectedMessage);
        }
    }

}
