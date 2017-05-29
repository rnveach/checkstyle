package com.puppycrawl.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;

public class JavadocTreeCheck extends AbstractJavadocCheck {
    public static String MSG_KEY = "JavadocTreeCheck.msg";

    private int fileLine;

    @Override
    public int[] getDefaultJavadocTokens() {
        return new int[] {};
    }

    @Override
    public void visitJavadocToken(DetailNode ast) {
        // no code
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        fileLine = 0;
    }

    @Override
    public void beginJavadocTree(DetailNode rootAst) {
        final String[] lines = DetailNodeTreeStringPrinter.printTree(rootAst, "", "")
                .replaceAll("\\\\r\\\\n", "\\\\n").split(System.lineSeparator());
        int lineNo = fileLine * 1000;

        for (String line : lines) {
            log(lineNo, MSG_KEY, line);

            lineNo++;
        }

        fileLine++;
    }
}
