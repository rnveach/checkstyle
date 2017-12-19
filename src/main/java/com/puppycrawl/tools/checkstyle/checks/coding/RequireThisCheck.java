////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.checks.coding;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtils;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.AbstractFrame;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.AnonymousClassFrame;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.ClassFrame;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.FrameType;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtils;

/**
 * <p>Checks that code doesn't rely on the &quot;this&quot; default.
 * That is references to instance variables and methods of the present
 * object are explicitly of the form &quot;this.varName&quot; or
 * &quot;this.methodName(args)&quot;.
 * </p>
 * Check has the following options:
 * <p><b>checkFields</b> - whether to check references to fields. Default value is <b>true</b>.</p>
 * <p><b>checkMethods</b> - whether to check references to methods.
 * Default value is <b>true</b>.</p>
 * <p><b>validateOnlyOverlapping</b> - whether to check only overlapping by variables or
 * arguments. Default value is <b>true</b>.</p>
 *
 * <p>Warning: the Check is very controversial if 'validateOnlyOverlapping' option is set to 'false'
 * and not that actual nowadays.</p>
 *
 * <p>Examples of use:
 * <pre>
 * &lt;module name=&quot;RequireThis&quot;/&gt;
 * </pre>
 * An example of how to configure to check {@code this} qualifier for
 * methods only:
 * <pre>
 * &lt;module name=&quot;RequireThis&quot;&gt;
 *   &lt;property name=&quot;checkFields&quot; value=&quot;false&quot;/&gt;
 *   &lt;property name=&quot;checkMethods&quot; value=&quot;true&quot;/&gt;
 * &lt;/module&gt;
 * </pre>
 *
 * <p>Rationale:</p>
 * <ol>
 *   <li>
 *     The same notation/habit for C++ and Java (C++ have global methods, so having
 *     &quot;this.&quot; do make sense in it to distinguish call of method of class
 *     instead of global).
 *   </li>
 *   <li>
 *     Non-IDE development (ease of refactoring, some clearness to distinguish
 *     static and non-static methods).
 *   </li>
 * </ol>
 *
 * <p>Limitations: Nothing is currently done about static variables
 * or catch-blocks.  Static methods invoked on a class name seem to be OK;
 * both the class name and the method name have a DOT parent.
 * Non-static methods invoked on either this or a variable name seem to be
 * OK, likewise.</p>
 *
 * @author Stephen Bloch
 * @author o_sukhodolsky
 * @author Andrei Selkin
 */
// -@cs[ClassDataAbstractionCoupling] This check requires to work with and identify many frames.
@FileStatefulCheck
public class RequireThisCheck extends AbstractCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_METHOD = "require.this.method";
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_VARIABLE = "require.this.variable";

    /** Set of all declaration tokens. */
    private static final Set<Integer> DECLARATION_TOKENS = Collections.unmodifiableSet(
        Arrays.stream(new Integer[] {
            TokenTypes.VARIABLE_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.CLASS_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.PARAMETER_DEF,
            TokenTypes.TYPE_ARGUMENT,
        }).collect(Collectors.toSet()));
    /** Set of all assign tokens. */
    private static final Set<Integer> ASSIGN_TOKENS = Collections.unmodifiableSet(
        Arrays.stream(new Integer[] {
            TokenTypes.ASSIGN,
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.STAR_ASSIGN,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.SR_ASSIGN,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.SL_ASSIGN,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BXOR_ASSIGN,
        }).collect(Collectors.toSet()));
    /** Set of all compound assign tokens. */
    private static final Set<Integer> COMPOUND_ASSIGN_TOKENS = Collections.unmodifiableSet(
        Arrays.stream(new Integer[] {
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.STAR_ASSIGN,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.SR_ASSIGN,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.SL_ASSIGN,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BXOR_ASSIGN,
        }).collect(Collectors.toSet()));

    /** Frame for the currently processed AST. */
    private final Deque<AbstractFrame> current = new ArrayDeque<>();

    /** Used to track field and method frames. */
    private FrameTrackingUtil frameTracker = new FrameTrackingUtil();

    /** Whether we should check fields usage. */
    private boolean checkFields = true;
    /** Whether we should check methods usage. */
    private boolean checkMethods = true;
    /** Whether we should check only overlapping by variables or arguments. */
    private boolean validateOnlyOverlapping = true;

    /**
     * Setter for checkFields property.
     * @param checkFields should we check fields usage or not.
     */
    public void setCheckFields(boolean checkFields) {
        this.checkFields = checkFields;
    }

    /**
     * Setter for checkMethods property.
     * @param checkMethods should we check methods usage or not.
     */
    public void setCheckMethods(boolean checkMethods) {
        this.checkMethods = checkMethods;
    }

    /**
     * Setter for validateOnlyOverlapping property.
     * @param validateOnlyOverlapping should we check only overlapping by variables or arguments.
     */
    public void setValidateOnlyOverlapping(boolean validateOnlyOverlapping) {
        this.validateOnlyOverlapping = validateOnlyOverlapping;
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.LAMBDA,
            TokenTypes.SLIST,
            TokenTypes.IDENT,
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        frameTracker.reset(rootAST);
        current.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IDENT :
                processIdent(ast);
                break;
            case TokenTypes.CLASS_DEF :
            case TokenTypes.INTERFACE_DEF :
            case TokenTypes.ENUM_DEF :
            case TokenTypes.ANNOTATION_DEF :
            case TokenTypes.SLIST :
            case TokenTypes.METHOD_DEF :
            case TokenTypes.CTOR_DEF :
            case TokenTypes.LITERAL_FOR :
            case TokenTypes.LITERAL_CATCH:
            case TokenTypes.LAMBDA:
                current.push(frameTracker.getFrame(ast));
                break;
            default :
                // do nothing
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF :
            case TokenTypes.INTERFACE_DEF :
            case TokenTypes.ENUM_DEF :
            case TokenTypes.ANNOTATION_DEF :
            case TokenTypes.SLIST :
            case TokenTypes.METHOD_DEF :
            case TokenTypes.CTOR_DEF :
            case TokenTypes.LITERAL_FOR:
            case TokenTypes.LITERAL_CATCH:
            case TokenTypes.LAMBDA:
                current.pop();
                break;
            default :
                // do nothing
        }
    }

    /**
     * Checks if a given IDENT is method call or field name which
     * requires explicit {@code this} qualifier.
     * @param ast IDENT to check.
     */
    private void processIdent(DetailAST ast) {
        int parentType = ast.getParent().getType();
        if (parentType == TokenTypes.EXPR
                && ast.getParent().getParent().getParent().getType()
                    == TokenTypes.ANNOTATION_FIELD_DEF) {
            parentType = TokenTypes.ANNOTATION_FIELD_DEF;
        }
        switch (parentType) {
            case TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
            case TokenTypes.ANNOTATION:
            case TokenTypes.ANNOTATION_FIELD_DEF:
                // no need to check annotations content
                break;
            case TokenTypes.METHOD_CALL:
                if (checkMethods) {
                    final AbstractFrame frame = getMethodWithoutThis(ast);
                    if (frame != null) {
                        logViolation(MSG_METHOD, ast, frame);
                    }
                }
                break;
            default:
                if (checkFields) {
                    final AbstractFrame frame = getFieldWithoutThis(ast, parentType);
                    if (frame != null) {
                        logViolation(MSG_VARIABLE, ast, frame);
                    }
                }
                break;
        }
    }

    /**
     * Helper method to log a LocalizedMessage.
     * @param ast a node to get line id column numbers associated with the message.
     * @param msgKey key to locale message format.
     * @param frame the class frame where the violation is found.
     */
    private void logViolation(String msgKey, DetailAST ast, AbstractFrame frame) {
        if (frame.getFrameName().equals(getNearestClassFrameName())) {
            log(ast, msgKey, ast.getText(), "");
        }
        else if (!(frame instanceof AnonymousClassFrame)) {
            log(ast, msgKey, ast.getText(), frame.getFrameName() + '.');
        }
    }

    /**
     * Returns the frame where the field is declared, if the given field is used without
     * 'this', and null otherwise.
     * @param ast field definition ast token.
     * @param parentType type of the parent.
     * @return the frame where the field is declared, if the given field is used without
     *         'this' and null otherwise.
     */
    private AbstractFrame getFieldWithoutThis(DetailAST ast, int parentType) {
        final boolean importOrPackage = ScopeUtils.getSurroundingScope(ast) == null;
        final boolean methodNameInMethodCall = parentType == TokenTypes.DOT
                && ast.getPreviousSibling() != null;
        final boolean typeName = parentType == TokenTypes.TYPE
                || parentType == TokenTypes.LITERAL_NEW;
        AbstractFrame frame = null;

        if (!importOrPackage
                && !methodNameInMethodCall
                && !typeName
                && !isDeclarationToken(parentType)
                && !CheckUtils.isLambdaParameter(ast)) {
            final AbstractFrame fieldFrame = findClassFrame(ast, false);

            if (fieldFrame != null && ((ClassFrame) fieldFrame).hasInstanceMember(ast)) {
                frame = getClassFrameWhereViolationIsFound(ast);
            }
        }
        return frame;
    }

    /**
     * Returns the class frame where violation is found (where the field is used without 'this')
     * or null otherwise.
     * @param ast IDENT ast to check.
     * @return the class frame where violation is found or null otherwise.
     * @noinspection IfStatementWithIdenticalBranches
     */
    // -@cs[CyclomaticComplexity] Method already invokes too many methods that fully explain
    // a logic, additional abstraction will not make logic/algorithm more readable.
    private AbstractFrame getClassFrameWhereViolationIsFound(DetailAST ast) {
        AbstractFrame frameWhereViolationIsFound = null;
        final AbstractFrame variableDeclarationFrame = findFrame(ast, false);
        final FrameType variableDeclarationFrameType = variableDeclarationFrame.getType();
        final DetailAST prevSibling = ast.getPreviousSibling();
        if (variableDeclarationFrameType == FrameType.CLASS_FRAME
                && !validateOnlyOverlapping
                && prevSibling == null
                && canBeReferencedFromStaticContext(ast)) {
            frameWhereViolationIsFound = variableDeclarationFrame;
        }
        else if (variableDeclarationFrameType == FrameType.METHOD_FRAME) {
            if (isOverlappingByArgument(ast)) {
                if (!isUserDefinedArrangementOfThis(variableDeclarationFrame, ast)
                        && !isReturnedVariable(variableDeclarationFrame, ast)
                        && canBeReferencedFromStaticContext(ast)
                        && canAssignValueToClassField(ast)) {
                    frameWhereViolationIsFound = findFrame(ast, true);
                }
            }
            else if (!validateOnlyOverlapping
                     && prevSibling == null
                     && isAssignToken(ast.getParent().getType())
                     && !isUserDefinedArrangementOfThis(variableDeclarationFrame, ast)
                     && canBeReferencedFromStaticContext(ast)
                     && canAssignValueToClassField(ast)) {
                frameWhereViolationIsFound = findFrame(ast, true);
            }
        }
        else if (variableDeclarationFrameType == FrameType.CTOR_FRAME
                 && isOverlappingByArgument(ast)
                 && !isUserDefinedArrangementOfThis(variableDeclarationFrame, ast)) {
            frameWhereViolationIsFound = findFrame(ast, true);
        }
        else if (variableDeclarationFrameType == FrameType.BLOCK_FRAME
                    && isOverlappingByLocalVariable(ast)
                    && canAssignValueToClassField(ast)
                    && !isUserDefinedArrangementOfThis(variableDeclarationFrame, ast)
                    && !isReturnedVariable(variableDeclarationFrame, ast)
                    && canBeReferencedFromStaticContext(ast)) {
            frameWhereViolationIsFound = findFrame(ast, true);
        }
        return frameWhereViolationIsFound;
    }

    /**
     * Checks whether user arranges 'this' for variable in method, constructor, or block on his own.
     * @param currentFrame current frame.
     * @param ident ident token.
     * @return true if user arranges 'this' for variable in method, constructor,
     *         or block on his own.
     */
    private static boolean isUserDefinedArrangementOfThis(AbstractFrame currentFrame,
                                                          DetailAST ident) {
        final DetailAST blockFrameNameIdent = currentFrame.getFrameNameIdent();
        final DetailAST definitionToken = blockFrameNameIdent.getParent();
        final DetailAST blockStartToken = definitionToken.findFirstToken(TokenTypes.SLIST);
        final DetailAST blockEndToken = getBlockEndToken(blockFrameNameIdent, blockStartToken);

        boolean userDefinedArrangementOfThis = false;

        final Set<DetailAST> variableUsagesInsideBlock =
            getAllTokensWhichAreEqualToCurrent(definitionToken, ident,
                blockEndToken.getLineNo());

        for (DetailAST variableUsage : variableUsagesInsideBlock) {
            final DetailAST prevSibling = variableUsage.getPreviousSibling();
            if (prevSibling != null
                    && prevSibling.getType() == TokenTypes.LITERAL_THIS) {
                userDefinedArrangementOfThis = true;
                break;
            }
        }
        return userDefinedArrangementOfThis;
    }

    /**
     * Returns the token which ends the code block.
     * @param blockNameIdent block name identifier.
     * @param blockStartToken token which starts the block.
     * @return the token which ends the code block.
     */
    private static DetailAST getBlockEndToken(DetailAST blockNameIdent, DetailAST blockStartToken) {
        DetailAST blockEndToken = null;
        final DetailAST blockNameIdentParent = blockNameIdent.getParent();
        if (blockNameIdentParent.getType() == TokenTypes.CASE_GROUP) {
            blockEndToken = blockNameIdentParent.getNextSibling();
        }
        else {
            final Set<DetailAST> rcurlyTokens = getAllTokensOfType(blockNameIdent,
                    TokenTypes.RCURLY);
            for (DetailAST currentRcurly : rcurlyTokens) {
                final DetailAST parent = currentRcurly.getParent();
                if (blockStartToken.getLineNo() == parent.getLineNo()) {
                    blockEndToken = currentRcurly;
                }
            }
        }
        return blockEndToken;
    }

    /**
     * Checks whether the current variable is returned from the method.
     * @param currentFrame current frame.
     * @param ident variable ident token.
     * @return true if the current variable is returned from the method.
     */
    private static boolean isReturnedVariable(AbstractFrame currentFrame, DetailAST ident) {
        final DetailAST blockFrameNameIdent = currentFrame.getFrameNameIdent();
        final DetailAST definitionToken = blockFrameNameIdent.getParent();
        final DetailAST blockStartToken = definitionToken.findFirstToken(TokenTypes.SLIST);
        final DetailAST blockEndToken = getBlockEndToken(blockFrameNameIdent, blockStartToken);

        final Set<DetailAST> returnsInsideBlock = getAllTokensOfType(definitionToken,
            TokenTypes.LITERAL_RETURN, blockEndToken.getLineNo());

        boolean returnedVariable = false;
        for (DetailAST returnToken : returnsInsideBlock) {
            returnedVariable = returnToken.findAll(ident).hasMoreNodes();
            if (returnedVariable) {
                break;
            }
        }
        return returnedVariable;
    }

    /**
     * Checks whether a field can be referenced from a static context.
     * @param ident ident token.
     * @return true if field can be referenced from a static context.
     */
    private boolean canBeReferencedFromStaticContext(DetailAST ident) {
        AbstractFrame variableDeclarationFrame = findFrame(ident, false);
        boolean staticInitializationBlock = false;
        while (variableDeclarationFrame.getType() == FrameType.BLOCK_FRAME
                || variableDeclarationFrame.getType() == FrameType.FOR_FRAME) {
            final DetailAST blockFrameNameIdent = variableDeclarationFrame.getFrameNameIdent();
            final DetailAST definitionToken = blockFrameNameIdent.getParent();
            if (definitionToken.getType() == TokenTypes.STATIC_INIT) {
                staticInitializationBlock = true;
                break;
            }
            variableDeclarationFrame = variableDeclarationFrame.getParent();
        }

        boolean staticContext = false;
        if (staticInitializationBlock) {
            staticContext = true;
        }
        else {
            if (variableDeclarationFrame.getType() == FrameType.CLASS_FRAME) {
                final DetailAST codeBlockDefinition = getCodeBlockDefinitionToken(ident);
                if (codeBlockDefinition != null) {
                    final DetailAST modifiers = codeBlockDefinition.getFirstChild();
                    staticContext = codeBlockDefinition.getType() == TokenTypes.STATIC_INIT
                        || modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
                }
            }
            else {
                final DetailAST frameNameIdent = variableDeclarationFrame.getFrameNameIdent();
                final DetailAST definitionToken = frameNameIdent.getParent();
                staticContext = definitionToken.findFirstToken(TokenTypes.MODIFIERS)
                        .findFirstToken(TokenTypes.LITERAL_STATIC) != null;
            }
        }
        return !staticContext;
    }

    /**
     * Returns code block definition token for current identifier.
     * @param ident ident token.
     * @return code block definition token for current identifier or null if code block
     *         definition was not found.
     */
    private static DetailAST getCodeBlockDefinitionToken(DetailAST ident) {
        DetailAST parent = ident.getParent();
        while (parent != null
               && parent.getType() != TokenTypes.METHOD_DEF
               && parent.getType() != TokenTypes.CTOR_DEF
               && parent.getType() != TokenTypes.STATIC_INIT) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Checks whether a value can be assigned to a field.
     * A value can be assigned to a final field only in constructor block. If there is a method
     * block, value assignment can be performed only to non final field.
     * @param ast an identifier token.
     * @return true if a value can be assigned to a field.
     */
    private boolean canAssignValueToClassField(DetailAST ast) {
        final AbstractFrame fieldUsageFrame = findFrame(ast, false);
        final boolean fieldUsageInConstructor = isInsideConstructorFrame(fieldUsageFrame);

        final AbstractFrame declarationFrame = findFrame(ast, true);
        final boolean finalField = ((ClassFrame) declarationFrame).hasFinalField(ast);

        return fieldUsageInConstructor || !finalField;
    }

    /**
     * Checks whether a field usage frame is inside constructor frame.
     * @param frame frame, where field is used.
     * @return true if the field usage frame is inside constructor frame.
     */
    private static boolean isInsideConstructorFrame(AbstractFrame frame) {
        boolean assignmentInConstructor = false;
        AbstractFrame fieldUsageFrame = frame;
        if (fieldUsageFrame.getType() == FrameType.BLOCK_FRAME) {
            while (fieldUsageFrame.getType() == FrameType.BLOCK_FRAME) {
                fieldUsageFrame = fieldUsageFrame.getParent();
            }
            if (fieldUsageFrame.getType() == FrameType.CTOR_FRAME) {
                assignmentInConstructor = true;
            }
        }
        return assignmentInConstructor;
    }

    /**
     * Checks whether an overlapping by method or constructor argument takes place.
     * @param ast an identifier.
     * @return true if an overlapping by method or constructor argument takes place.
     */
    private boolean isOverlappingByArgument(DetailAST ast) {
        boolean overlapping = false;
        final DetailAST parent = ast.getParent();
        final DetailAST sibling = ast.getNextSibling();
        if (sibling != null && isAssignToken(parent.getType())) {
            if (isCompoundAssignToken(parent.getType())) {
                overlapping = true;
            }
            else {
                final ClassFrame classFrame = (ClassFrame) findFrame(ast, true);
                final Set<DetailAST> exprIdents = getAllTokensOfType(sibling, TokenTypes.IDENT);
                overlapping = classFrame.containsFieldOrVariableDef(exprIdents, ast);
            }
        }
        return overlapping;
    }

    /**
     * Checks whether an overlapping by local variable takes place.
     * @param ast an identifier.
     * @return true if an overlapping by local variable takes place.
     */
    private boolean isOverlappingByLocalVariable(DetailAST ast) {
        boolean overlapping = false;
        final DetailAST parent = ast.getParent();
        final DetailAST sibling = ast.getNextSibling();
        if (sibling != null && isAssignToken(parent.getType())) {
            final ClassFrame classFrame = (ClassFrame) findFrame(ast, true);
            final Set<DetailAST> exprIdents = getAllTokensOfType(sibling, TokenTypes.IDENT);
            overlapping = classFrame.containsFieldOrVariableDef(exprIdents, ast);
        }
        return overlapping;
    }

    /**
     * Collects all tokens of specific type starting with the current ast node.
     * @param ast ast node.
     * @param tokenType token type.
     * @return a set of all tokens of specific type starting with the current ast node.
     */
    private static Set<DetailAST> getAllTokensOfType(DetailAST ast, int tokenType) {
        DetailAST vertex = ast;
        final Set<DetailAST> result = new HashSet<>();
        final Deque<DetailAST> stack = new ArrayDeque<>();
        while (vertex != null || !stack.isEmpty()) {
            if (!stack.isEmpty()) {
                vertex = stack.pop();
            }
            while (vertex != null) {
                if (vertex.getType() == tokenType) {
                    result.add(vertex);
                }
                if (vertex.getNextSibling() != null) {
                    stack.push(vertex.getNextSibling());
                }
                vertex = vertex.getFirstChild();
            }
        }
        return result;
    }

    /**
     * Collects all tokens of specific type starting with the current ast node and which line
     * number is lower or equal to the end line number.
     * @param ast ast node.
     * @param tokenType token type.
     * @param endLineNumber end line number.
     * @return a set of all tokens of specific type starting with the current ast node and which
     *         line number is lower or equal to the end line number.
     */
    private static Set<DetailAST> getAllTokensOfType(DetailAST ast, int tokenType,
                                                     int endLineNumber) {
        DetailAST vertex = ast;
        final Set<DetailAST> result = new HashSet<>();
        final Deque<DetailAST> stack = new ArrayDeque<>();
        while (vertex != null || !stack.isEmpty()) {
            if (!stack.isEmpty()) {
                vertex = stack.pop();
            }
            while (vertex != null) {
                if (tokenType == vertex.getType()
                    && vertex.getLineNo() <= endLineNumber) {
                    result.add(vertex);
                }
                if (vertex.getNextSibling() != null) {
                    stack.push(vertex.getNextSibling());
                }
                vertex = vertex.getFirstChild();
            }
        }
        return result;
    }

    /**
     * Collects all tokens which are equal to current token starting with the current ast node and
     * which line number is lower or equal to the end line number.
     * @param ast ast node.
     * @param token token.
     * @param endLineNumber end line number.
     * @return a set of tokens which are equal to current token starting with the current ast node
     *         and which line number is lower or equal to the end line number.
     */
    private static Set<DetailAST> getAllTokensWhichAreEqualToCurrent(DetailAST ast, DetailAST token,
                                                                     int endLineNumber) {
        DetailAST vertex = ast;
        final Set<DetailAST> result = new HashSet<>();
        final Deque<DetailAST> stack = new ArrayDeque<>();
        while (vertex != null || !stack.isEmpty()) {
            if (!stack.isEmpty()) {
                vertex = stack.pop();
            }
            while (vertex != null) {
                if (token.equals(vertex)
                        && vertex.getLineNo() <= endLineNumber) {
                    result.add(vertex);
                }
                if (vertex.getNextSibling() != null) {
                    stack.push(vertex.getNextSibling());
                }
                vertex = vertex.getFirstChild();
            }
        }
        return result;
    }

    /**
     * Returns the frame where the method is declared, if the given method is used without
     * 'this' and null otherwise.
     * @param ast the IDENT ast of the name to check.
     * @return the frame where the method is declared, if the given method is used without
     *         'this' and null otherwise.
     */
    private AbstractFrame getMethodWithoutThis(DetailAST ast) {
        AbstractFrame result = null;
        if (!validateOnlyOverlapping) {
            final AbstractFrame frame = findFrame(ast, true);
            if (frame != null
                    && ((ClassFrame) frame).hasInstanceMethod(ast)
                    && !((ClassFrame) frame).hasStaticMethod(ast)) {
                result = frame;
            }
        }
        return result;
    }

    /**
     * Find the class frame containing declaration.
     * @param name IDENT ast of the declaration to find.
     * @param lookForMethod whether we are looking for a method name.
     * @return AbstractFrame containing declaration or null.
     */
    private AbstractFrame findClassFrame(DetailAST name, boolean lookForMethod) {
        AbstractFrame frame = current.peek();

        while (true) {
            frame = findFrame(frame, name, lookForMethod);

            if (frame == null || frame instanceof ClassFrame) {
                break;
            }

            frame = frame.getParent();
        }

        return frame;
    }

    /**
     * Find frame containing declaration.
     * @param name IDENT ast of the declaration to find.
     * @param lookForMethod whether we are looking for a method name.
     * @return AbstractFrame containing declaration or null.
     */
    private AbstractFrame findFrame(DetailAST name, boolean lookForMethod) {
        return findFrame(current.peek(), name, lookForMethod);
    }

    /**
     * Find frame containing declaration.
     * @param frame The parent frame to searching in.
     * @param name IDENT ast of the declaration to find.
     * @param lookForMethod whether we are looking for a method name.
     * @return AbstractFrame containing declaration or null.
     */
    private static AbstractFrame findFrame(AbstractFrame frame, DetailAST name,
            boolean lookForMethod) {
        return frame.getIfContains(name, lookForMethod);
    }

    /**
     * Check that token is related to Definition tokens.
     * @param parentType token Type.
     * @return true if token is related to Definition Tokens.
     */
    private static boolean isDeclarationToken(int parentType) {
        return DECLARATION_TOKENS.contains(parentType);
    }

    /**
     * Check that token is related to assign tokens.
     * @param tokenType token type.
     * @return true if token is related to assign tokens.
     */
    private static boolean isAssignToken(int tokenType) {
        return ASSIGN_TOKENS.contains(tokenType);
    }

    /**
     * Check that token is related to compound assign tokens.
     * @param tokenType token type.
     * @return true if token is related to compound assign tokens.
     */
    private static boolean isCompoundAssignToken(int tokenType) {
        return COMPOUND_ASSIGN_TOKENS.contains(tokenType);
    }

    /**
     * Gets the name of the nearest parent ClassFrame.
     * @return the name of the nearest parent ClassFrame.
     */
    private String getNearestClassFrameName() {
        AbstractFrame frame = current.peek();
        while (frame.getType() != FrameType.CLASS_FRAME) {
            frame = frame.getParent();
        }
        return frame.getFrameName();
    }

}
