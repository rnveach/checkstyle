package com.puppycrawl.tools.checkstyle.checks.coding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class TruthReplacerCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
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

    private TreeSet<ChangeInfo> methods = new TreeSet<ChangeInfo>();

    @Override
    public void beginTree(DetailAST rootAST) {
        this.methods.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getParent().getType() != TokenTypes.EXPR)
            return;
        if (ast.getParent().getParent().getType() != TokenTypes.SLIST)
            return;

        final DetailAST methodNameAst = ast.findFirstToken(TokenTypes.IDENT);

        // usually because of dots
        if (methodNameAst == null)
            return;

        final String methodName = methodNameAst.getText();

        if (!"assertEquals".equals(methodName) && !"assertArrayEquals".equals(methodName)
                && !"assertNotNull".equals(methodName)) {
            return;
        }

        final DetailAST eList = ast.findFirstToken(TokenTypes.ELIST);
        List<Position> parameters = new ArrayList<>();
        Position parameterPositionStart = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Position parameterPositionEnd = new Position(0, 0);

        for (DetailAST child = eList.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getType() == TokenTypes.COMMA) {
                parameters.add(parameterPositionStart);
                parameters.add(parameterPositionEnd);

                parameterPositionStart = new Position(child.getLineNo(), child.getColumnNo() + 1);
                parameterPositionEnd = parameterPositionStart.copy();
            }
            else {
                iterateNoSiblings(child, parameterPositionStart, parameterPositionEnd);
            }
        }

        // TODO: does not support empty methods correctly
        parameters.add(parameterPositionStart);
        parameters.add(parameterPositionEnd);

        while (parameters.size() < 6)
            parameters.add(null);

        final DetailAST rParenAst = ast.findFirstToken(TokenTypes.RPAREN);

        this.methods.add(new ChangeInfo(methodName, parameters,
                new Position(methodNameAst.getLineNo(), methodNameAst.getColumnNo()),
                new Position(rParenAst.getLineNo(), maxColumn(rParenAst))));
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        if (this.methods.isEmpty())
            return;

        final FileText file = getFileContents().getText();
        final int[] lineBreaks = file.getLineBreaks();
        String fileText = file.getFullText().toString();
        StringBuilder sb = new StringBuilder(fileText);

        for (ChangeInfo change : this.methods.descendingSet()) {
            String newText;

            String cutOne = fileText.substring(change.firstParameterStart.toPosition(lineBreaks),
                    change.firstParameterEnd.toPosition(lineBreaks)).trim();
            String cutTwo = fileText.substring(change.secondParameterStart.toPosition(lineBreaks),
                    change.secondParameterEnd.toPosition(lineBreaks)).trim();
            String cutThree = null;

            if (change.thirdParameterStart != null) {
                cutThree = fileText.substring(change.thirdParameterStart.toPosition(lineBreaks),
                        change.thirdParameterEnd.toPosition(lineBreaks)).trim();
            }

            if ("assertEquals".equals(change.methodName)
                    || "assertArrayEquals".equals(change.methodName)) {
                if (cutThree == null)
                    throw new IllegalStateException();

                newText = "assertWithMessage(" + cutThree + ").that(" + cutTwo + ").isEqualTo("
                        + cutOne + ")";
            }
            else if ("assertNotNull".equals(change.methodName)) {
                if (cutThree != null)
                    throw new IllegalStateException();

                newText = "assertWithMessage(" + cutTwo + ").that(" + cutOne + ").isNotNull()";
            }
            else {
                throw new IllegalStateException(change.methodName);
            }

            sb.replace(change.methodStart.toPosition(lineBreaks),
                    change.methodEnd.toPosition(lineBreaks), newText.toString());
        }

        try {
            writeFile(file.getFile(), sb.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void iterateNoSiblings(DetailAST child, Position parameterPositionStart,
            Position parameterPositionEnd) {
        parameterPositionStart.updateMin(child.getLineNo(), child.getColumnNo());
        parameterPositionEnd.updateMax(child.getLineNo(), maxColumn(child));

        if (child.getFirstChild() != null)
            iterate(child.getFirstChild(), parameterPositionStart, parameterPositionEnd);
    }

    private static void iterate(DetailAST child, Position parameterPositionStart,
            Position parameterPositionEnd) {
        parameterPositionStart.updateMin(child.getLineNo(), child.getColumnNo());
        parameterPositionEnd.updateMax(child.getLineNo(), maxColumn(child));

        if (child.getFirstChild() != null)
            iterate(child.getFirstChild(), parameterPositionStart, parameterPositionEnd);
        if (child.getNextSibling() != null)
            iterate(child.getNextSibling(), parameterPositionStart, parameterPositionEnd);
    }

    private static int maxColumn(DetailAST child) {
        if (child.getText().equals(TokenUtil.getTokenName(child.getType()))) {
            return child.getColumnNo();
        }

        return child.getColumnNo() + child.getText().length();
    }

    private static void writeFile(File file, String contents) throws IOException {
        Files.write(file.toPath(), contents.getBytes());
    }

    private class ChangeInfo implements Comparable<ChangeInfo> {
        private String methodName;
        private final Position methodStart;
        private final Position methodEnd;
        private final Position firstParameterStart;
        private final Position firstParameterEnd;
        private final Position secondParameterStart;
        private final Position secondParameterEnd;
        private final Position thirdParameterStart;
        private final Position thirdParameterEnd;

        public ChangeInfo(String methodName, List<Position> parameters, Position methodStart,
                Position methodEnd) {
            this(methodName, methodStart, methodEnd, parameters.get(0), parameters.get(1),
                    parameters.get(2), parameters.get(3), parameters.get(4), parameters.get(5));
        }

        public ChangeInfo(String methodName, Position methodStart, Position methodEnd,
                Position firstParameterStart, Position firstParameterEnd,
                Position secondParameterStart, Position secondParameterEnd,
                Position thirdParameterStart, Position thirdParameterEnd) {
            this.methodName = methodName;
            this.methodStart = methodStart;
            this.methodEnd = methodEnd;
            this.firstParameterStart = firstParameterStart;
            this.firstParameterEnd = firstParameterEnd;
            this.secondParameterStart = secondParameterStart;
            this.secondParameterEnd = secondParameterEnd;
            this.thirdParameterStart = thirdParameterStart;
            this.thirdParameterEnd = thirdParameterEnd;
        }

        @Override
        public int compareTo(ChangeInfo o) {
            int d;

            d = this.methodStart.compareTo(o.methodStart);
            if (d != 0)
                return d;

            d = this.methodEnd.compareTo(o.methodEnd);
            if (d != 0)
                return d;

            return 0;
        }

        @Override
        public String toString() {
            return "ChangeInfo [methodName=" + methodName + ", methodStart=" + methodStart
                    + ", methodEnd=" + methodEnd + ", firstParameterStart=" + firstParameterStart
                    + ", firstParameterEnd=" + firstParameterEnd + ", secondParameterStart="
                    + secondParameterStart + ", secondParameterEnd=" + secondParameterEnd
                    + ", thirdParameterStart=" + thirdParameterStart + ", thirdParameterEnd="
                    + thirdParameterEnd + "]";
        }
    }

    private class Position implements Comparable<Position> {
        private int line;
        private int column;

        public Position(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int toPosition(int[] lineBreaks) {
            // TODO: probably doesn't support TAB correctly
            return lineBreaks[line - 1] + column;
        }

        public void updateMin(int lineNo, int columnNo) {
            if (lineNo < line) {
                set(lineNo, columnNo);
            }
            else if (lineNo == line) {
                if (columnNo < column)
                    set(lineNo, columnNo);
            }
        }

        public void updateMax(int lineNo, int columnNo) {
            if (lineNo > line) {
                set(lineNo, columnNo);
            }
            else if (lineNo == line) {
                if (columnNo > column)
                    set(lineNo, columnNo);
            }
        }

        public void set(int lineNo, int columnNo) {
            this.line = lineNo;
            this.column = columnNo;
        }

        public Position copy() {
            return new Position(line, column);
        }

        @Override
        public int compareTo(Position o) {
            int d;

            d = Integer.compare(this.line, o.line);
            if (d != 0)
                return d;

            d = Integer.compare(this.column, o.column);
            if (d != 0)
                return d;

            return 0;
        }

        @Override
        public String toString() {
            return "Position [line=" + line + ", column=" + column + "]";
        }
    }
}
