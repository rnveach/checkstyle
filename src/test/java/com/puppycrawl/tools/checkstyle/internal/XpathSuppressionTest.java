////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.SuppressionsStringPrinter;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.filters.SuppressionXpathFilter;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class XpathSuppressionTest extends AbstractModuleTestSupport {
    private static final int DEFAULT_TAB_WIDTH = 4;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/internal";
    }

    @Test
    public void testAllChecks() throws Exception {
        checkDir(new File("src/test/resources/com/puppycrawl/tools/checkstyle"));
    }

    private void checkDir(File dir) throws Exception {
        final File[] files = dir.listFiles(file -> {
            return (file.getName().endsWith(".java") || file.isDirectory())
                    && !file.getName().endsWith("InputGrammar.java");
        });
        for (File file : files) {
            if (file.isFile()) {
                checkFile(file);
            }
            else if (file.isDirectory()) {
                checkDir(file);
            }
        }
    }

    private void checkFile(File file) throws Exception {
        final String path = file.getCanonicalPath();
        // TODO: fix removed files
        if (
        // until https://github.com/checkstyle/checkstyle/issues/5820
        path.contains("InputDetailASTJustToMakeStackoverflowError.java")
                // until https://github.com/checkstyle/checkstyle/issues/5821
                || path.contains("InputAstTreeStringPrinterFullOfSinglelineComments.java")
                || path.contains("InputAnnotationOnSameLineCheck.java")
                || path.contains("InputAnnotationOnSameLineCheck3.java")
                || path.contains("InputAnnotationOnSameLineCheckOnDifferentTokens.java")
                // until ...
                || path.contains("InputNeedBracesNoBodyLoops.java")
                || path.contains("InputRightCurlyAloneLambda.java")
                || path.contains("InputRightCurlyAloneOrSingleline.java")
                || path.contains("InputRightCurlyAloneOrSinglelineLambda.java")
                || path.contains("InputRightCurlyAnnotations.java")
                || path.contains("InputRightCurlySameLambda.java")
                || path.contains("InputAvoidEscapedUnicodeCharacters.java")
                || path.contains("InputAvoidEscapedUnicodeCharacters1.java")
                || path.contains("InputExplicitInitialization.java")
                || path.contains("InputIllegalTokens.java")
                || path.contains("InputIllegalTokenTextTokens.java")
                || path.contains("InputMatchXpathNoStackoverflowError.java")
                || path.contains("InputVariableDeclarationUsageDistance.java")
                || path.contains("InputIndentationChainedMethodWithBracketOnNewLine.java")
                || path.contains("InputVariableDeclarationUsageDistanceDefault.java")
                || path.contains("InputVariableDeclarationUsageDistanceFinal.java")
                || path.contains("InputVariableDeclarationUsageDistanceGeneral.java")
                || path.contains("InputVariableDeclarationUsageDistanceRegExp.java")
                || path.contains("InputVariableDeclarationUsageDistanceScopes.java")
                || path.contains("InputIndentationLambda1.java")
                || path.contains("InputClassFanOutComplexityAnnotations.java")
                || path.contains("InputLineLengthUnicodeChars.java")
                || path.contains("InputParenPadLeftRightAndNoSpace.java")
                // file is too big and slow
                || path.contains(
                        "InputAvoidEscapedUnicodeCharactersAllEscapedUnicodeCharacters.java")) {
            return;
        }

        // TODO: need to calculate actual position in file
        final String input = new String(Files.readAllBytes(file.toPath()), UTF_8);
        if (input.contains("\t")) {
            return;
        }

        System.out.println("**************");
        System.out.println("File: " + file.getCanonicalPath());

        final DetailAST rootAST = JavaParser.parseFile(new File(file.getCanonicalPath()),
                JavaParser.Options.WITH_COMMENTS);
        if (rootAST != null) {
            checkTree(file, rootAST);
        }
    }

    private void checkTree(File file, DetailAST root) throws Exception {
        DetailAST curNode = root;
        while (curNode != null) {
            checkNode(file, curNode);
            DetailAST toVisit = curNode.getFirstChild();
            if (toVisit == null) {
                while (curNode != null && toVisit == null) {
                    toVisit = curNode.getNextSibling();
                    if (toVisit == null) {
                        curNode = curNode.getParent();
                    }
                    else {
                        curNode = toVisit;
                    }
                }
            }
            else {
                curNode = toVisit;
            }
        }
    }

    private void checkNode(File file, DetailAST node) throws Exception {
        if (
        // until https://github.com/checkstyle/checkstyle/issues/5819
        node.getType() == TokenTypes.BLOCK_COMMENT_BEGIN
                || node.getType() == TokenTypes.COMMENT_CONTENT
                || node.getType() == TokenTypes.BLOCK_COMMENT_END
                || node.getType() == TokenTypes.SINGLE_LINE_COMMENT) {
            return;
        }

        final int line = node.getLineNo();
        final int column = node.getColumnNo();

        final String[] xpathQueries = SuppressionsStringPrinter
                .printSuppressions(file, line + ":" + (column + 1), DEFAULT_TAB_WIDTH)
                .split(LINE_SEPARATOR);

        // System.out.println("---------");
        System.out.println("Node: " + node.toString());
        // System.out.println("Line: " + line);
        // System.out.println("Column: " + column);

        Assert.assertTrue("expecting xpaths for '" + file.getCanonicalPath() + "' at line " + line
                + ", column " + column + " for type " + TokenUtil.getTokenName(node.getType()),
                xpathQueries.length > 0);

        boolean atleastOneWorks = false;

        for (String xpathQuery : xpathQueries) {
            // System.out.println("Query: " + xpathQuery);
            final DefaultConfiguration checkConfig = createModuleConfig(TestCheck.class);
            checkConfig.addAttribute("tokens", TokenUtil.getTokenName(node.getType()));
            checkConfig.addAttribute("line", String.valueOf(line));
            checkConfig.addAttribute("column", String.valueOf(column));
            checkConfig.addAttribute("query", xpathQuery);

            final DefaultConfiguration filterConfig = createModuleConfig(
                    SuppressionXpathFilter.class);
            filterConfig.addAttribute("file", createSuppressionsXpathConfigFile(xpathQuery));

            final DefaultConfiguration treeWalkerConfig = createModuleConfig(TreeWalker.class);
            treeWalkerConfig.addChild(checkConfig);
            treeWalkerConfig.addChild(filterConfig);

            try {
                verify(treeWalkerConfig, file.getCanonicalPath(), CommonUtil.EMPTY_STRING_ARRAY);
                atleastOneWorks = true;
            }
            catch (AssertionError ex) {
                // ignore, only 1 suppression must work
            }
        }

        Assert.assertTrue("one of the xpath suppressions must work for '" + file.getCanonicalPath()
                + "' at line " + line + ", column " + column + " for type "
                + TokenUtil.getTokenName(node.getType()), atleastOneWorks);
    }

    private String createSuppressionsXpathConfigFile(String xpathQuery) throws Exception {

        final File suppressionsXpathConfigFile = temporaryFolder.newFile();
        try (Writer bw = Files.newBufferedWriter(suppressionsXpathConfigFile.toPath(),
                StandardCharsets.UTF_8)) {
            bw.write("<?xml version=\"1.0\"?>\n");
            bw.write("<!DOCTYPE suppressions PUBLIC\n");
            bw.write("    \"-//Puppy Crawl//DTD Suppressions Xpath Experimental 1.2//EN\"\n");
            bw.write("    \"http://checkstyle.sourceforge.net/dtds/");
            bw.write("suppressions_1_2_xpath_experimental.dtd\">\n");
            bw.write("<suppressions>\n");
            bw.write("   <suppress-xpath\n");
            bw.write("       checks=\".\"\n");
            bw.write("       query=\"");
            bw.write(xpathQuery);
            bw.write("\"/>\n");
            bw.write("</suppressions>");
        }

        return suppressionsXpathConfigFile.getPath();
    }

    public static final class TestCheck extends AbstractCheck {
        private int line;
        private int column;
        private int count;
        private String query;

        public TestCheck() {
            count = 0;
        }

        @Override
        public boolean isCommentNodesRequired() {
            return true;
        }

        @Override
        public int[] getAcceptableTokens() {
            return TokenUtil.getAllTokenIds();
        }

        @Override
        public int[] getDefaultTokens() {
            return CommonUtil.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] getRequiredTokens() {
            return getDefaultTokens();
        }

        public void setLine(int line) {
            this.line = line;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public void visitToken(DetailAST ast) {
            if (ast.getLineNo() == line && ast.getColumnNo() == column) {
                final String message = "violation " + line + "," + column + ","
                        + TokenUtil.getTokenName(ast.getType()) + "," + ast.getText() + " using: "
                        + query;
                log(ast, message.replace("'", "''").replace("{", "'{'").replace("}", "'}'"));
                count++;
            }
        }

        @Override
        public void finishTree(DetailAST rootAST) {
            // System.out.println("Violation Count: " + count);
            Assert.assertTrue("expected atleast 1 violation", count > 0);
        }
    }
}
