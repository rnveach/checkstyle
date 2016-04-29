////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.api;

import java.util.BitSet;

import antlr.CommonASTWithHiddenTokens;
import antlr.Token;
import antlr.collections.AST;
import com.puppycrawl.tools.checkstyle.utils.TokenUtils;

/**
 * An extension of the CommonAST that records the line and column number.
 *
 * @author Oliver Burn
 * @author lkuehne
 * @see <a href="http://www.antlr.org/">ANTLR Website</a>
 */
public final class DetailAST extends CommonASTWithHiddenTokens {
    private static final long serialVersionUID = -2580884815577559874L;

    /** Constant to indicate if not calculated the child count. */
    private static final int NOT_INITIALIZED = Integer.MIN_VALUE;

    /** The line number. **/
    private int lineNo = NOT_INITIALIZED;
    /** The column number. **/
    private int columnNo = NOT_INITIALIZED;

    private int minLineNo = NOT_INITIALIZED;
    private int minColumnNo = NOT_INITIALIZED;

    /** Number of children. */
    private int childCount = NOT_INITIALIZED;
    /** The parent token. */
    private DetailAST parent;
    /** Previous sibling. */
    private DetailAST previousSibling;

    /**
     * All token types in this branch. Token 'x' (where x is an int) is in this branch if
     * branchTokenTypes.get(x) is true.
     */
    private BitSet branchTokenTypes;

    @Override
    public void initialize(Token tok) {
        super.initialize(tok);
        lineNo = tok.getLine();

        // expect columns to start @ 0
        columnNo = tok.getColumn() - 1;
    }

    @Override
    public void initialize(AST ast) {
        final DetailAST detailAst = (DetailAST) ast;
        setText(detailAst.getText());
        setType(detailAst.getType());
        lineNo = detailAst.lineNo;
        columnNo = detailAst.columnNo;
        hiddenAfter = detailAst.getHiddenAfter();
        hiddenBefore = detailAst.getHiddenBefore();
    }

    @Override
    public void setFirstChild(AST ast) {
        childCount = NOT_INITIALIZED;
        super.setFirstChild(ast);
        if (ast != null) {
            ((DetailAST) ast).setParent(this);
        }
    }

    @Override
    public void setNextSibling(AST ast) {
        super.setNextSibling(ast);
        if (ast != null && parent != null) {
            ((DetailAST) ast).setParent(parent);
        }
        if (ast != null) {
            ((DetailAST) ast).previousSibling = this;
        }
    }

    /**
     * Add previous sibling.
     * 
     * @param ast
     *            DetailAST object.
     */
    public void addPreviousSibling(DetailAST ast) {
        if (ast != null) {
            ast.setParent(parent);
            final DetailAST previousSiblingNode = previousSibling;

            if (previousSiblingNode != null) {
                ast.previousSibling = previousSiblingNode;
                previousSiblingNode.setNextSibling(ast);
            }
            else if (parent != null) {
                parent.setFirstChild(ast);
            }

            ast.setNextSibling(this);
            previousSibling = ast;
        }
    }

    /**
     * Add next sibling.
     * 
     * @param ast
     *            DetailAST object.
     */
    public void addNextSibling(DetailAST ast) {
        if (ast != null) {
            ast.setParent(parent);
            final DetailAST nextSibling = getNextSibling();

            if (nextSibling != null) {
                ast.setNextSibling(nextSibling);
                nextSibling.previousSibling = ast;
            }

            ast.previousSibling = this;
            setNextSibling(ast);
        }
    }

    @Override
    public void addChild(AST ast) {
        super.addChild(ast);
        if (ast != null) {
            ((DetailAST) ast).setParent(this);
            getFirstChild().setParent(this);
        }
    }

    /**
     * Returns the number of child nodes one level below this node. That is is does not recurse down
     * the tree.
     * 
     * @return the number of child nodes
     */
    public int getChildCount() {
        // lazy init
        if (childCount == NOT_INITIALIZED) {
            childCount = 0;
            AST child = getFirstChild();

            while (child != null) {
                childCount += 1;
                child = child.getNextSibling();
            }
        }
        return childCount;
    }

    /**
     * Returns the number of direct child tokens that have the specified type.
     * 
     * @param type
     *            the token type to match
     * @return the number of matching token
     */
    public int getChildCount(int type) {
        int count = 0;
        for (AST ast = getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == type) {
                count++;
            }
        }
        return count;
    }

    /**
     * Set the parent token.
     * 
     * @param parent
     *            the parent token
     */
    private void setParent(DetailAST parent) {
        this.parent = parent;
        final DetailAST nextSibling = getNextSibling();
        if (nextSibling != null) {
            nextSibling.setParent(parent);
            nextSibling.previousSibling = this;
        }
    }

    /**
     * Returns the parent token.
     * 
     * @return the parent token
     */
    public DetailAST getParent() {
        return parent;
    }

    /**
     * Gets line number.
     * 
     * @return the line number
     */
    public int getLineNo() {
        if (lineNo == NOT_INITIALIZED) {
            generateLineColumnNo();
        }
        return lineNo;
    }

    /**
     * Set line number.
     * 
     * @param lineNo
     *            line number.
     */
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    /**
     * Gets column number.
     * 
     * @return the column number
     */
    public int getColumnNo() {
        if (columnNo == NOT_INITIALIZED) {
            generateLineColumnNo();
        }
        return columnNo;
    }

    /**
     * Set column number.
     * 
     * @param columnNo
     *            column number.
     */
    public void setColumnNo(int columnNo) {
        this.columnNo = columnNo;
    }

    public int getMinLineNo() {
        if (minLineNo == NOT_INITIALIZED) {
            generateMinLineColumnNo();
        }
        return minLineNo;
    }

    public int getMinColumnNo() {
        if (minColumnNo == NOT_INITIALIZED) {
            generateMinLineColumnNo();
        }
        return minColumnNo;
    }

    /**
     * Gets the last child node.
     * 
     * @return the last child node
     */
    public DetailAST getLastChild() {
        DetailAST ast = getFirstChild();
        while (ast != null && ast.getNextSibling() != null) {
            ast = ast.getNextSibling();
        }
        return ast;
    }

    private void generateLineColumnNo() {
        lineNo = getMinLineNo();
        columnNo = getMinColumnNo();
    }

    private void generateMinLineColumnNo() {
        int line = Integer.MAX_VALUE;
        int column = Integer.MAX_VALUE;
        DetailAST node = getFirstChild();

        if (node == null || lineNo != NOT_INITIALIZED) {
            line = lineNo;
            column = columnNo;
        }

        // examine the children for the lowest line/column numbers

        while (node != null) {
                if (!TokenUtils.isCommentType(node.getType())) {
                    if (node.getMinLineNo() < line
                            || (node.getMinLineNo() == line && node.getColumnNo() < column)) {
                        line = node.getMinLineNo();
                        column = node.getMinColumnNo();
                    }
                }

                node = node.getNextSibling();
            }

        // if there are no line numbers in the siblings, use the next siblings
        if (line == Integer.MAX_VALUE || line == NOT_INITIALIZED) {
            line = getNextSibling().getMinLineNo();
            column = getNextSibling().getMinColumnNo();
        }

        minLineNo = line;
        minColumnNo = column;
    }

    /**
     * @return the token types that occur in the branch as a sorted set.
     */
    private BitSet getBranchTokenTypes() {
        // lazy init
        if (branchTokenTypes == null) {

            branchTokenTypes = new BitSet();
            branchTokenTypes.set(getType());

            // add union of all children
            DetailAST child = getFirstChild();
            while (child != null) {
                final BitSet childTypes = child.getBranchTokenTypes();
                branchTokenTypes.or(childTypes);

                child = child.getNextSibling();
            }
        }
        return branchTokenTypes;
    }

    /**
     * Checks if this branch of the parse tree contains a token of the provided type.
     * 
     * @param type
     *            a TokenType
     * @return true if and only if this branch (including this node) contains a token of type
     *         {@code type}.
     */
    public boolean branchContains(int type) {
        return getBranchTokenTypes().get(type);
    }

    /**
     * Returns the previous sibling or null if no such sibling exists.
     * 
     * @return the previous sibling or null if no such sibling exists.
     */
    public DetailAST getPreviousSibling() {
        return previousSibling;
    }

    /**
     * Returns the first child token that makes a specified type.
     * 
     * @param type
     *            the token type to match
     * @return the matching token, or null if no match
     */
    public DetailAST findFirstToken(int type) {
        DetailAST returnValue = null;
        for (DetailAST ast = getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == type) {
                returnValue = ast;
                break;
            }
        }
        return returnValue;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + lineNo + "x" + columnNo + "]";
    }

    @Override
    public DetailAST getNextSibling() {
        return (DetailAST) super.getNextSibling();
    }

    @Override
    public DetailAST getFirstChild() {
        return (DetailAST) super.getFirstChild();
    }

}
