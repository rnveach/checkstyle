package com.puppycrawl.tools.checkstyle.checks.javadoc;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

public class EmptyJavadocDescriptionCheck extends AbstractJavadocCheck {

    /** Message property key for the Empty Javadoc message. */
    public static final String MSG_EMPTY = "javadoc.empty";

    /** Specify the visibility scope where Javadoc comments are checked. */
    private Scope scope = Scope.PRIVATE;

    /** Specify the visibility scope where Javadoc comments are not checked. */
    private Scope excludeScope;
    
    // TODO: specify acceptable ast tokens

    @Override
    public int[] getDefaultJavadocTokens() {
        return new int[] {
            JavadocTokenTypes.JAVADOC,
        };
    }

    @Override
    public void visitJavadocToken(DetailNode ast) {
        if (shouldCheck(getJavadocTargetAst())) {
            checkJavadocDescriptionIsNotEmpty(ast);
        }
    }

    /**
     * Whether we should check this node.
     *
     * @param ast a given node.
     * @return whether we should check a given node.
     */
    private boolean shouldCheck(final DetailAST ast) {
        boolean check = false;

        if (ast.getType() == TokenTypes.PACKAGE_DEF) {
            check = CheckUtil.isPackageInfo(getFilePath());
        }
        else if (!ScopeUtil.isInCodeBlock(ast)) {
            final Scope customScope = ScopeUtil.getScope(ast);
            final Scope surroundingScope = ScopeUtil.getSurroundingScope(ast);

            check = customScope.isIn(scope)
                    && (surroundingScope == null || surroundingScope.isIn(scope))
                    && (excludeScope == null
                        || !customScope.isIn(excludeScope)
                        || surroundingScope != null
                            && !surroundingScope.isIn(excludeScope));
        }
        return check;
    }

    /**
     * Checks that the Javadoc description is not empty.
     *
     * @param ast the source lines that make up the Javadoc comment.
     */
    private void checkJavadocDescriptionIsNotEmpty(DetailNode ast) {
        if (!hasDescription(ast)) {
            log(ast.getLineNumber(), MSG_EMPTY);
        }
    }

    private static boolean hasDescription(DetailNode ast) {
        boolean result = false;

        for (DetailNode child : ast.getChildren()) {
            int type = child.getType();

            if (type == JavadocTokenTypes.TEXT) {
                final String text = child.getText().trim(); 
                if (!text.isEmpty() && !Character.isWhitespace(text.charAt(0))) {
                    result = true;
                    break;
                }
            } else if (type == JavadocTokenTypes.JAVADOC_TAG) {
                break;
            } else if (type != JavadocTokenTypes.EOF
                    && type != JavadocTokenTypes.LEADING_ASTERISK
                    && type != JavadocTokenTypes.NEWLINE
                    && type != JavadocTokenTypes.WS) { 
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Setter to specify the visibility scope where Javadoc comments are checked.
     *
     * @param scope a scope.
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * Setter to specify the visibility scope where Javadoc comments are not checked.
     *
     * @param excludeScope a scope.
     */
    public void setExcludeScope(Scope excludeScope) {
        this.excludeScope = excludeScope;
    }

}
