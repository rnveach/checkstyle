package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class IllegalTokenParentCheck extends AbstractCheck {
    public static final String MSG_KEY = "illegal.token";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
                violateToken
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

    private int violateToken;
    private int violateParentToken;

    public void setViolateToken(String violateToken) {
        this.violateToken = TokenUtil.getTokenId(violateToken);
    }

    public void setViolateParentToken(String violateParentToken) {
        this.violateParentToken = TokenUtil.getTokenId(violateParentToken);
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getParent().getType() == violateParentToken) {
            log(ast, MSG_KEY, ast.getText());
        }
    }
}
