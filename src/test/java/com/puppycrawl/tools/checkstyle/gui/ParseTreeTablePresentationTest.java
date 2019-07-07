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

package com.puppycrawl.tools.checkstyle.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractPathTestSupport;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.gui.MainFrameModel.ParseMode;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class ParseTreeTablePresentationTest extends AbstractPathTestSupport {

    private DetailAST tree;

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/gui/parsetreetablepresentation";
    }

    @Before
    public void loadTree() throws Exception {
        tree = JavaParser.parseFile(new File(getPath("InputParseTreeTablePresentation.java")),
            JavaParser.Options.WITH_COMMENTS).getNextSibling();
    }

    @Test
    public void testRoot() {
        final Object root = new ParseTreeTablePresentation(tree).getRoot();
        final int childCount = new ParseTreeTablePresentation(null).getChildCount(root);
        assertEquals("Invalid child count", 1, childCount);
    }

    @Test
    public void testChildCount() {
        final int childCount = new ParseTreeTablePresentation(null).getChildCount(tree);
        assertEquals("Invalid child count", 5, childCount);
    }

    @Test
    public void testChildCountInJavaAndJavadocMode() {
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final int childCount = parseTree.getChildCount(tree);
        assertEquals("Invalid child count", 5, childCount);
    }

    @Test
    public void testChild() {
        final Object child = new ParseTreeTablePresentation(null).getChild(tree, 1);
        assertTrue("Invalid child type", child instanceof DetailAST);
        assertEquals("Invalid child token type",
                TokenTypes.BLOCK_COMMENT_BEGIN, ((DetailAST) child).getType());
    }

    @Test
    public void testChildInJavaAndJavadocMode() {
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final Object child = parseTree.getChild(tree, 1);
        assertTrue("Invalid child type", child instanceof DetailAST);
        assertEquals("Invalid child token type",
                TokenTypes.BLOCK_COMMENT_BEGIN, ((DetailAST) child).getType());
    }

    @Test
    public void testCommentChildCount() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_COMMENTS);
        final int javadocCommentChildCount = parseTree.getChildCount(commentContentNode);
        assertEquals("Invalid child count", 0, javadocCommentChildCount);
    }

    @Test
    public void testCommentChildCountInJavaAndJavadocMode() {
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final DetailAST commentContentNode = tree.getLastChild().getLastChild()
                .getPreviousSibling().getLastChild().getFirstChild().getFirstChild();
        final int commentChildCount = parseTree.getChildCount(commentContentNode);
        assertEquals("Invalid child count", 0, commentChildCount);
    }

    @Test
    public void testCommentChildInJavaAndJavadocMode() {
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final DetailAST commentContentNode = tree.getLastChild().getLastChild()
                .getPreviousSibling().getLastChild().getFirstChild().getFirstChild();
        final Object commentChild = parseTree.getChild(commentContentNode, 0);
        assertNull("Child must be null", commentChild);
    }

    @Test
    public void testJavadocCommentChildCount() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        final int commentChildCount = parseTree.getChildCount(commentContentNode);
        assertEquals("Invalid child count", 0, commentChildCount);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final int javadocCommentChildCount = parseTree.getChildCount(commentContentNode);
        assertEquals("Invalid child count", 1, javadocCommentChildCount);
    }

    @Test
    public void testJavadocCommentChild() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final Object child = parseTree.getChild(commentContentNode, 0);
        assertTrue("Invalid child type", child instanceof DetailNode);
        assertEquals("Invalid child token type",
                JavadocTokenTypes.JAVADOC, ((DetailNode) child).getType());
        // get Child one more time to test cache of PModel
        final Object childSame = parseTree.getChild(commentContentNode, 0);
        assertTrue("Invalid child type", childSame instanceof DetailNode);
        assertEquals("Invalid child token type",
                JavadocTokenTypes.JAVADOC, ((DetailNode) childSame).getType());
    }

    @Test
    public void testJavadocChildCount() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final Object javadoc = parseTree.getChild(commentContentNode, 0);
        assertTrue("Invalid child type", javadoc instanceof DetailNode);
        assertEquals("Invalid child token type",
                JavadocTokenTypes.JAVADOC, ((DetailNode) javadoc).getType());
        final int javadocChildCount = parseTree.getChildCount(javadoc);
        assertEquals("Invalid child count", 5, javadocChildCount);
    }

    @Test
    public void testJavadocChild() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final Object javadoc = parseTree.getChild(commentContentNode, 0);
        assertTrue("Invalid child type", javadoc instanceof DetailNode);
        assertEquals("Invalid child token type",
                JavadocTokenTypes.JAVADOC, ((DetailNode) javadoc).getType());
        final Object javadocChild = parseTree.getChild(javadoc, 2);
        assertTrue("Invalid child type", javadocChild instanceof DetailNode);
        assertEquals("Invalid child token type",
                JavadocTokenTypes.TEXT, ((DetailNode) javadocChild).getType());
    }

    @Test
    public void testGetIndexOfChild() {
        DetailAST ithChild = tree.getFirstChild();
        assertNotNull("Child must not be null", ithChild);
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        int index = 0;
        while (ithChild != null) {
            assertEquals("Invalid child index",
                    index, parseTree.getIndexOfChild(tree, ithChild));
            ithChild = ithChild.getNextSibling();
            index++;
        }

        assertEquals("Invalid child index",
                -1, parseTree.getIndexOfChild(tree, new DetailAstImpl()));
    }

    /**
     * The path to class name in InputJavadocAttributesAndMethods.java.
     * <pre>
     * CLASS_DEF
     *  - MODIFIERS
     *  - Comment node
     *  - LITERAL_CLASS
     *  - IDENT -> this is the node that holds the class name
     *  Line number 4 - first three lines are taken by javadoc
     *  Column 6 - first five columns taken by 'class '
     *  </pre>
     */
    @Test
    public void testGetValueAt() {
        final DetailAST node = tree.getFirstChild()
            .getNextSibling()
            .getNextSibling()
            .getNextSibling();

        assertNotNull("Expected a non-null identifier node here", node);
        assertEquals("Expected identifier token",
            TokenTypes.IDENT, node.getType());

        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        final Object treeModel = parseTree.getValueAt(node, 0);
        final String type = (String) parseTree.getValueAt(node, 1);
        final int line = (int) parseTree.getValueAt(node, 2);
        final int column = (int) parseTree.getValueAt(node, 3);
        final String text = (String) parseTree.getValueAt(node, 4);

        assertEquals("Node should be an Identifier", "IDENT", type);
        assertEquals("Class identifier should start on line 6", 6, line);
        assertEquals("Class name should start from column 6", 6, column);
        assertEquals("Wrong class name", "InputParseTreeTablePresentation", text);
        assertNull("Root node should have null value", treeModel);

        try {
            parseTree.getValueAt(node, parseTree.getColumnCount());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            assertEquals("Invalid error message", "Unknown column", ex.getMessage());
        }
    }

    @Test
    public void testGetValueAtDetailNode() {
        final DetailAST commentContentNode = tree.getFirstChild().getNextSibling().getFirstChild();
        assertNotNull("Comment node cannot be null", commentContentNode);
        final int nodeType = commentContentNode.getType();
        assertTrue("Comment node should be a comment type",
            TokenUtil.isCommentType(nodeType));
        assertEquals("This should be a javadoc comment",
            "/*", commentContentNode.getParent().getText());
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        parseTree.setParseMode(ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS);
        final Object child = parseTree.getChild(commentContentNode, 0);

        assertFalse("Child has not to be leaf", parseTree.isLeaf(child));
        assertTrue("Child has to be leaf", parseTree.isLeaf(tree.getFirstChild()));

        final Object treeModel = parseTree.getValueAt(child, 0);
        final String type = (String) parseTree.getValueAt(child, 1);
        final int line = (int) parseTree.getValueAt(child, 2);
        final int column = (int) parseTree.getValueAt(child, 3);
        final String text = (String) parseTree.getValueAt(child, 4);
        final String expectedText = "JAVADOC";

        assertNull("Tree model must be null", treeModel);
        assertEquals("Invalid type", "JAVADOC", type);
        assertEquals("Invalid line", 3, line);
        assertEquals("Invalid column", 3, column);
        assertEquals("Invalid text", expectedText, text);

        try {
            parseTree.getValueAt(child, parseTree.getColumnCount());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            assertEquals("Invalid error message", "Unknown column", ex.getMessage());
        }
    }

    @Test
    public void testColumnMethods() {
        final ParseTreeTablePresentation parseTree = new ParseTreeTablePresentation(null);
        assertSame("Invalid type", ParseTreeTableModel.class, parseTree.getColumnClass(0));
        assertSame("Invalid type", String.class, parseTree.getColumnClass(1));
        assertSame("Invalid type", Integer.class, parseTree.getColumnClass(2));
        assertSame("Invalid type", Integer.class, parseTree.getColumnClass(3));
        assertSame("Invalid type", String.class, parseTree.getColumnClass(4));

        try {
            parseTree.getColumnClass(parseTree.getColumnCount());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            assertEquals("Invalid error message", "Unknown column", ex.getMessage());
        }

        assertFalse("Invalid cell editable status", parseTree.isCellEditable(1));

        assertEquals("Invalid column count", 5, parseTree.getColumnCount());
        assertEquals("Invalid column name", "Tree", parseTree.getColumnName(0));
        assertEquals("Invalid column name", "Type", parseTree.getColumnName(1));
        assertEquals("Invalid column name", "Line", parseTree.getColumnName(2));
        assertEquals("Invalid column name", "Column", parseTree.getColumnName(3));
        assertEquals("Invalid column name", "Text", parseTree.getColumnName(4));
    }

}
