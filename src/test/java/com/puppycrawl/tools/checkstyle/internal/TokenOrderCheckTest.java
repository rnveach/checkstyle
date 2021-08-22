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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class TokenOrderCheckTest extends AbstractModuleTestSupport {
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

    private static void checkFile(File file) throws Exception {
        final String path = file.getCanonicalPath();
        if (
        // TODO
        // until ...
        false
                // file is too big and slow
                || path.contains(
                        "InputAvoidEscapedUnicodeCharactersAllEscapedUnicodeCharacters.java")) {
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

    private static void checkTree(File file, DetailAST root) throws Exception {
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

    private static void checkNode(File file, DetailAST node) throws Exception {
        final int nodeType = node.getType();
        if (
        // TODO
        // until (this looks to be true error)
        nodeType == TokenTypes.DOT //
                // until https://github.com/checkstyle/checkstyle/issues/10699
                || nodeType == TokenTypes.COMMENT_CONTENT //
                // until (this looks to be true error)
                || nodeType == TokenTypes.LABELED_STAT //
                // until ...
                || nodeType == TokenTypes.STAR //
                || nodeType == TokenTypes.PLUS //
                || nodeType == TokenTypes.MINUS //
                || nodeType == TokenTypes.DIV //
                || nodeType == TokenTypes.MOD //
                || nodeType == TokenTypes.POST_DEC //
                || nodeType == TokenTypes.POST_INC //
                || nodeType == TokenTypes.EQUAL //
                || nodeType == TokenTypes.NOT_EQUAL //
                || nodeType == TokenTypes.GT //
                || nodeType == TokenTypes.GE //
                || nodeType == TokenTypes.LT //
                || nodeType == TokenTypes.LE //
                || nodeType == TokenTypes.LAND //
                || nodeType == TokenTypes.LOR //
                || nodeType == TokenTypes.BAND //
                || nodeType == TokenTypes.BOR //
                || nodeType == TokenTypes.BXOR //
                || nodeType == TokenTypes.INDEX_OP //
                || nodeType == TokenTypes.SR //
                || nodeType == TokenTypes.BSR //
                || nodeType == TokenTypes.SL //
                || nodeType == TokenTypes.QUESTION //
                || nodeType == TokenTypes.LAMBDA //
                || nodeType == TokenTypes.METHOD_CALL //
                || nodeType == TokenTypes.SUPER_CTOR_CALL //
                || nodeType == TokenTypes.METHOD_REF //
                || nodeType == TokenTypes.LITERAL_INSTANCEOF //
                || nodeType == TokenTypes.ASSIGN //
                || nodeType == TokenTypes.BAND_ASSIGN //
                || nodeType == TokenTypes.BOR_ASSIGN //
                || nodeType == TokenTypes.BSR_ASSIGN //
                || nodeType == TokenTypes.SL_ASSIGN //
                || nodeType == TokenTypes.SR_ASSIGN //
                || nodeType == TokenTypes.BXOR_ASSIGN //
                || nodeType == TokenTypes.DIV_ASSIGN //
                || nodeType == TokenTypes.MOD_ASSIGN //
                || nodeType == TokenTypes.PLUS_ASSIGN //
                || nodeType == TokenTypes.MINUS_ASSIGN //
                || nodeType == TokenTypes.STAR_ASSIGN //
        //
        ) {
            return;
        }

        final int nodeLine = node.getLineNo();
        final int nodeColumn = node.getColumnNo();

        final DetailAST sibling = node.getNextSibling();
        if (sibling != null) {
            final int siblingType = sibling.getType();
            if (
            // TODO
            // until ...
            siblingType == TokenTypes.BLOCK_COMMENT_BEGIN //
                    || siblingType == TokenTypes.SINGLE_LINE_COMMENT //
            //
            ) {
                return;
            }
            if (
            // TODO
            // until https://github.com/checkstyle/checkstyle/issues/3151
            siblingType == TokenTypes.VARIABLE_DEF && nodeType == TokenTypes.COMMA //
            //
            ) {
                return;
            }
            final int siblingLine = sibling.getLineNo();
            final int siblingColumn = sibling.getColumnNo();

            if ((siblingLine < nodeLine)
                    || ((siblingLine == nodeLine) && (siblingColumn < nodeColumn))) {
                Assert.fail("tokens out of order for '" + file.getCanonicalPath() + "' at line "
                        + nodeLine + ", column " + nodeColumn + " for type "
                        + TokenUtil.getTokenName(node.getType()) + " with sibling type "
                        + TokenUtil.getTokenName(sibling.getType()));
            }
        }

        final DetailAST child = node.getFirstChild();
        if (child != null) {
            final int childType = child.getType();
            if (
            // TODO
            // until ...
            childType == TokenTypes.BLOCK_COMMENT_BEGIN //
                    || childType == TokenTypes.SINGLE_LINE_COMMENT //
                    || childType == TokenTypes.ANNOTATIONS //
            //
            ) {
                return;
            }

            final int childLine = child.getLineNo();
            final int childColumn = child.getColumnNo();

            if ((childLine < nodeLine) || ((childLine == nodeLine) && (childColumn < nodeColumn))) {
                Assert.fail("tokens out of order for '" + file.getCanonicalPath() + "' at line "
                        + nodeLine + ", column " + nodeColumn + " for type "
                        + TokenUtil.getTokenName(node.getType()) + " with child type "
                        + TokenUtil.getTokenName(child.getType()));
            }
        }
    }
}
