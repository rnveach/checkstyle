package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil;

public class AvoidDirectReassignment extends AbstractCheck {
    public static final String MSG_KEY = "directReassignment";

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.VARIABLE_DEF };
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.VARIABLE_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] { TokenTypes.VARIABLE_DEF };
    }

    /** Used to track variable/field frames. */
    private FrameTrackingUtil frameTracker = new FrameTrackingUtil();

    @Override
    public void beginTree(DetailAST rootAST) {
        frameTracker.reset(rootAST);
    }

    @Override
    public void visitToken(DetailAST ast) {
        final DetailAST assignment = ast.findFirstToken(TokenTypes.ASSIGN);

        // TODO: ignore class variables

        if (assignment != null && assignment.getFirstChild().getType() == TokenTypes.EXPR
                && assignment.getFirstChild().getFirstChild().getType() == TokenTypes.IDENT) {
            final DetailAST ident = assignment.getFirstChild().getFirstChild();
            DetailAST identDeclaration = frameTracker.findFrame(ast).findVariableDeclaration(ident,
                    null);
            // TODO: variables must be in same method scope

            if (identDeclaration == null) {
                // TODO
                // log(ast, MSG_KEY);
            } else {
                identDeclaration = identDeclaration.getParent();

                if (identDeclaration.getType() != TokenTypes.PARAMETER_DEF && //
                        ast.getParent().getType() != TokenTypes.FOR_INIT && //
                        (!isFinal(ast) //
                        || isFinal(identDeclaration))) {
                    log(ast, MSG_KEY);
                }
            }
        }
    }

    private static boolean isFinal(DetailAST ast) {
        // TODO: secretly defined `final`
        return ast.getType() == TokenTypes.ENUM_CONSTANT_DEF
                || ast.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.FINAL) != null;
    }
}
