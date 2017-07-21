package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.utils.AnnotationUtility;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

public class ModifierViolationCheck extends AbstractCheck {
    public static final String MSG_KEY = "area.violation";

    private static final String OVERRIDE = "Override";
    private static final String FQ_OVERRIDE = "java.lang." + OVERRIDE;

    private static final String TEST = "Test";
    private static final String FQ_TEST = "org.junit." + TEST;

    private static final String BEFORECLASS = "BeforeClass";
    private static final String FQ_BEFORECLASS = "org.junit." + BEFORECLASS;

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
                TokenTypes.LITERAL_PUBLIC, TokenTypes.LITERAL_PROTECTED
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

    @Override
    public void visitToken(DetailAST ast) {
        final DetailAST methodDef = ast.getParent().getParent();

        if (methodDef.getType() == TokenTypes.METHOD_DEF) {
            if (AnnotationUtility.containsAnnotation(methodDef, OVERRIDE)
                    || AnnotationUtility.containsAnnotation(methodDef, FQ_OVERRIDE)
                    || AnnotationUtility.containsAnnotation(methodDef, TEST)
                    || AnnotationUtility.containsAnnotation(methodDef, FQ_TEST)
                    || AnnotationUtility.containsAnnotation(methodDef, BEFORECLASS)
                    || AnnotationUtility.containsAnnotation(methodDef, FQ_BEFORECLASS)) {
                return;
            }
        }

        log(ast, MSG_KEY, "" + ast.getLineNo(), "" + ast.getColumnNo(), "" + ast.getLineNo(), ""
                + (ast.getColumnNo() + ast.getText().length()));
    }
}
