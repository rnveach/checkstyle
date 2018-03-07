
package com.puppycrawl.tools.checkstyle.checks.design;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.AbstractFrame;

public class ClassMethodCallOrderCheck extends AbstractCheck {
    public static final String MSG_KEY = "method.layout";

    private static final String PACKAGE_SEPARATOR = ".";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    /** Used to track variable/field frames. */
    private final FrameTrackingUtil frameTracker = new FrameTrackingUtil();

    private String currentClassPath;
    private final Deque<String> classPaths = new ArrayDeque<String>();

    private final Map<String, List<String>> classFields = new HashMap<String, List<String>>();

    private final List<String> imports = new ArrayList<String>();

    @Override
    public void beginTree(DetailAST rootAST) {
        frameTracker.reset(rootAST);
        currentClassPath = "";
        classPaths.clear();
        classFields.clear();
        imports.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.PACKAGE_DEF) {
            currentClassPath = extractQualifiedName(ast);
        }
        else if (ast.getType() == TokenTypes.IMPORT) {
            imports.add(FullIdent.createFullIdentBelow(ast).getText());
        }
        else if (ast.getType() == TokenTypes.METHOD_CALL) {
            if (ast.getParent().getType() != TokenTypes.DOT
                    && ast.getFirstChild().getType() == TokenTypes.DOT
                    && ast.getFirstChild().getFirstChild().getType() == TokenTypes.IDENT) {
                visitMethodCall(ast);
            }
        }
        else {
            classPaths.push(currentClassPath);
            currentClassPath = currentClassPath + "."
                    + ast.findFirstToken(TokenTypes.IDENT).getText();

            List<String> fields = classFields.get(currentClassPath);

            if (fields == null) {
                fields = new ArrayList<String>();
                classFields.put(currentClassPath, fields);
            }

            DetailAST child = ast.findFirstToken(TokenTypes.OBJBLOCK).getFirstChild();

            while (child != null) {
                if (child.getType() == TokenTypes.VARIABLE_DEF) {
                    fields.add(child.findFirstToken(TokenTypes.TYPE).getText());
                }

                child = child.getNextSibling();
            }
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() != TokenTypes.PACKAGE_DEF //
                && ast.getType() != TokenTypes.IMPORT//
                && ast.getType() != TokenTypes.METHOD_CALL) {
            currentClassPath = classPaths.pop();
        }
    }

    private void visitMethodCall(DetailAST ast) {
        final DetailAST variableIdent = ast.getFirstChild().getFirstChild();
        final AbstractFrame frame = frameTracker.findFrame(variableIdent);

        if (frame != null) {
            // TODO
        }
    }

    /**
     * Get name of class(with qualified package if specified) in extend clause.
     * 
     * @param classExtend
     *        extend clause to extract class name
     * @return super class name
     */
    private static String extractQualifiedName(DetailAST classExtend) {
        final String className;

        if (classExtend.findFirstToken(TokenTypes.IDENT) == null) {
            // Name specified with packages, have to traverse DOT
            final DetailAST firstChild = classExtend.findFirstToken(TokenTypes.DOT);
            final List<String> qualifiedNameParts = new LinkedList<String>();

            qualifiedNameParts.add(0, firstChild.findFirstToken(TokenTypes.IDENT).getText());
            DetailAST traverse = firstChild.findFirstToken(TokenTypes.DOT);
            while (traverse != null) {
                qualifiedNameParts.add(0, traverse.findFirstToken(TokenTypes.IDENT).getText());
                traverse = traverse.findFirstToken(TokenTypes.DOT);
            }
            className = Joiner.on(PACKAGE_SEPARATOR).join(qualifiedNameParts);
        }
        else {
            className = classExtend.findFirstToken(TokenTypes.IDENT).getText();
        }

        return className;
    }
}
