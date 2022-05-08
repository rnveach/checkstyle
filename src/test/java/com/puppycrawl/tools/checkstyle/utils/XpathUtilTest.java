///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.utils;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.internal.utils.TestUtil.isUtilsClassHasPrivateConstructor;
import static com.puppycrawl.tools.checkstyle.utils.XpathUtil.getTextAttributeValue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.xpath.AbstractNode;
import com.puppycrawl.tools.checkstyle.xpath.RootNode;

public class XpathUtilTest {

    @TempDir
    public File tempFolder;

    @Test
    public void testIsProperUtilsClass() throws ReflectiveOperationException {
        assertWithMessage("Constructor is not private")
                .that(isUtilsClassHasPrivateConstructor(XpathUtil.class))
                .isTrue();
    }

    @Test
    public void testSupportsTextAttribute() {
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.IDENT)))
                .isTrue();
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.NUM_INT)))
                .isTrue();
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.STRING_LITERAL)))
                .isTrue();
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.CHAR_LITERAL)))
                .isTrue();
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.NUM_DOUBLE)))
                .isTrue();
        assertWithMessage("Should return false for unsupported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.VARIABLE_DEF)))
                .isFalse();
        assertWithMessage("Should return false for unsupported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.OBJBLOCK)))
                .isFalse();
        assertWithMessage("Should return true for supported token types")
                .that(XpathUtil.supportsTextAttribute(createDetailAST(TokenTypes.LITERAL_CHAR)))
                .isFalse();
    }

    @Test
    public void testGetValue() {
        assertWithMessage("Returned value differs from expected")
            .that(getTextAttributeValue(
                createDetailAST(TokenTypes.STRING_LITERAL, "\"HELLO WORLD\"")))
            .isEqualTo("HELLO WORLD");
        assertWithMessage("Returned value differs from expected")
            .that(getTextAttributeValue(createDetailAST(TokenTypes.NUM_INT, "123")))
            .isEqualTo("123");
        assertWithMessage("Returned value differs from expected")
            .that(getTextAttributeValue(createDetailAST(TokenTypes.IDENT, "HELLO WORLD")))
            .isEqualTo("HELLO WORLD");
        assertWithMessage("Returned value differs from expected")
            .that(getTextAttributeValue(createDetailAST(TokenTypes.STRING_LITERAL, "HELLO WORLD")))
            .isNotEqualTo("HELLO WORLD");
    }

    @Test
    public void testCreateChildren() {
        final DetailAstImpl rootAst = new DetailAstImpl();
        final DetailAstImpl elementAst = new DetailAstImpl();
        rootAst.addChild(elementAst);
        final RootNode rootNode = new RootNode(rootAst);
        final List<AbstractNode> children =
                XpathUtil.createChildren(rootNode, rootNode, elementAst);

        assertWithMessage("Expected one child node")
                .that(children)
                .hasSize(1);
        assertWithMessage("Node depth should be 1")
                .that(children.get(0).getDepth())
                .isEqualTo(1);
    }

    private static DetailAST createDetailAST(int type) {
        final DetailAstImpl detailAST = new DetailAstImpl();
        detailAST.setType(type);
        return detailAST;
    }

    private static DetailAST createDetailAST(int type, String text) {
        final DetailAstImpl detailAST = new DetailAstImpl();
        detailAST.setType(type);
        detailAST.setText(text);
        return detailAST;
    }
}
