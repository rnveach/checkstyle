package com.puppycrawl.tools.checkstyle.checks.coding;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.AbstractFrame;

public class TooManyReassignmentsCheck extends AbstractCheck {
    public static final String MSG_KEY = "toomany.reassignments";

    /** Used to track variable/field frames. */
    private FrameTrackingUtil frameTracker = new FrameTrackingUtil();

    private Map<DetailAST, AtomicLong> variableAssignmentCounts = new HashMap<DetailAST, AtomicLong>();

    private long max = 3;

    public void setMax(long max) {
        this.max = max;
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] { TokenTypes.ASSIGN };
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        frameTracker.reset(rootAST);
        variableAssignmentCounts.clear();
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        for (Entry<DetailAST, AtomicLong> entry : variableAssignmentCounts.entrySet()) {
            if (entry.getValue().longValue() > max) {
                log(entry.getKey(), MSG_KEY, entry.getKey().getText(),
                        entry.getValue().longValue(), max);
            }
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        final DetailAST leftHand = getLeftHandAssignment(ast);
        Boolean instance = null;
        DetailAST variableIdent = null;

        if (leftHand.getType() == TokenTypes.IDENT) {
            // Type variable = X;
            if (ast.getParent().getType() == TokenTypes.VARIABLE_DEF) {
                instance = false;
            }
            // variable = X;
            else {
                instance = null;
            }
            variableIdent = leftHand;
        } else
        // this.field = X;
        if (leftHand.getType() == TokenTypes.DOT
                && leftHand.getFirstChild().getType() == TokenTypes.LITERAL_THIS) {
            instance = true;
            variableIdent = leftHand.getLastChild();
        } else {
            // TODO: can we handle weird stuff?
            // this.field1.field2 = X;
            // or Class.field = X;
            // or getInstance().field = X;
        }

        if (variableIdent != null) {
            final AbstractFrame frame = frameTracker.findFrame(ast);

            if (frame != null) {
                final DetailAST declaration = frame
                        .findVariableDeclaration(variableIdent, instance);

                if (declaration != null) {
                    AtomicLong count = variableAssignmentCounts.get(declaration);

                    // TODO: don't count switches as separate as long as in base
                    // case of same switch?
                    // TODO: ignore class fields?

                    if (count == null) {
                        count = new AtomicLong(1);
                        variableAssignmentCounts.put(declaration, count);
                    } else {
                        count.incrementAndGet();
                    }
                }
            }
        }
    }

    private static DetailAST getLeftHandAssignment(DetailAST assign) {
        final DetailAST parent = assign.getParent();

        if (parent.getType() == TokenTypes.EXPR) {
            // variable = X;
            // or this.field = X;
            // or this.field1.field2 = X;
            // or Class.field = X;
            // or getInstance().field = X;
            return assign.getFirstChild();
        }

        // Type variable = X;
        return assign.getPreviousSibling();
    }
}
