////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.checks.javadoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 * Checks the Javadoc of a type.
 *
 * <p>Does not perform checks for author and version tags for inner classes, as
 * they should be redundant because of outer class.
 *
 */
@StatelessCheck
public class JavadocTypeCheck
    extends AbstractJavadocCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_UNKNOWN_TAG = "javadoc.unknownTag";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_TAG_FORMAT = "type.tagFormat";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_MISSING_TAG = "type.missingTag";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_UNUSED_TAG = "javadoc.unusedTag";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_UNUSED_TAG_GENERAL = "javadoc.unusedTagGeneral";

    /** Open angle bracket literal. */
    private static final String OPEN_ANGLE_BRACKET = "<";

    /** Close angle bracket literal. */
    private static final String CLOSE_ANGLE_BRACKET = ">";

    /** Author tag literal. */
    private static final String AUTHOR_TAG = "@author";

    /** Version tag literal. */
    private static final String VERSION_TAG = "@version";

    /** The scope to check for. */
    private Scope scope = Scope.PRIVATE;
    /** The visibility scope where Javadoc comments shouldn't be checked. **/
    private Scope excludeScope;
    /** Compiled regexp to match author tag content. **/
    private Pattern authorFormat;
    /** Compiled regexp to match version tag content. **/
    private Pattern versionFormat;
    /**
     * Controls whether to ignore errors when a method has type parameters but
     * does not have matching param tags in the javadoc. Defaults to false.
     */
    private boolean allowMissingParamTags;
    /** Controls whether to flag errors for unknown tags. Defaults to false. */
    private boolean allowUnknownTags;

    /** List of annotations that allow missed documentation. */
    private List<String> allowedAnnotations = Collections.singletonList("Generated");

    /** List of AST targets of this check. */
    private List<Integer> target = Arrays.asList(
        TokenTypes.INTERFACE_DEF,
        TokenTypes.CLASS_DEF,
        TokenTypes.ENUM_DEF,
        TokenTypes.ANNOTATION_DEF
    );

    /**
     * Sets the scope to check.
     * @param scope a scope.
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * Set the excludeScope.
     * @param excludeScope a scope.
     */
    public void setExcludeScope(Scope excludeScope) {
        this.excludeScope = excludeScope;
    }

    /**
     * Set the author tag pattern.
     * @param pattern a pattern.
     */
    public void setAuthorFormat(Pattern pattern) {
        authorFormat = pattern;
    }

    /**
     * Set the version format pattern.
     * @param pattern a pattern.
     */
    public void setVersionFormat(Pattern pattern) {
        versionFormat = pattern;
    }

    /**
     * Controls whether to allow a type which has type parameters to
     * omit matching param tags in the javadoc. Defaults to false.
     *
     * @param flag a {@code Boolean} value
     */
    public void setAllowMissingParamTags(boolean flag) {
        allowMissingParamTags = flag;
    }

    /**
     * Controls whether to flag errors for unknown tags. Defaults to false.
     * @param flag a {@code Boolean} value
     */
    public void setAllowUnknownTags(boolean flag) {
        allowUnknownTags = flag;
    }

    /**
     * Sets list of annotations.
     * @param userAnnotations user's value.
     */
    public void setAllowedAnnotations(String... userAnnotations) {
        allowedAnnotations = Arrays.asList(userAnnotations);
    }

    /**
     * Sets list of targets.
     * @param targets user's value.
     */
    public void setTarget(String... targets) {
        target = new ArrayList<>();
        for (String temp : targets) {
            target.add(TokenUtil.getTokenId(temp));
        }
    }

    @Override
    public int[] getDefaultJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public int[] getAcceptableJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return new int[] {
            JavadocTokenTypes.JAVADOC,
        };
    }

    @Override
    public void visitJavadocToken(DetailNode ast) {
        final DetailAST targetAst = JavadocUtil.getTarget(getBlockCommentAst());

        if (isAllowedTarget(targetAst) && shouldCheck(targetAst)) {
            validateJavadoc(targetAst, ast);
        }
    }

    private void validateJavadoc(DetailAST targetAst, DetailNode ast) {
        final boolean isOuterMostType = ScopeUtil.isOuterMostType(targetAst);
        final List<String> typeParamNames =
                CheckUtil.getTypeParameterNames(targetAst);
        boolean hasAuthor = false;
        boolean hasVersion = false;

        for (DetailNode child : ast.getChildren()) {
            if (child.getType() == JavadocTokenTypes.JAVADOC_TAG) {
                switch (JavadocUtil.getFirstChild(child).getType()) {
                    case JavadocTokenTypes.PARAM_LITERAL:
                        validateParameter(child, typeParamNames);
                        break;
                    case JavadocTokenTypes.AUTHOR_LITERAL:
                        if (isOuterMostType) {
                            hasAuthor = true;

                            checkFormat(targetAst.getLineNo(), AUTHOR_TAG, authorFormat,
                                    getTagDescription(JavadocUtil.findFirstToken(child,
                                            JavadocTokenTypes.DESCRIPTION)));
                        }
                        break;
                    case JavadocTokenTypes.VERSION_LITERAL:
                        if (isOuterMostType) {
                            hasVersion = true;

                            checkFormat(targetAst.getLineNo(), VERSION_TAG, versionFormat,
                                    getTagDescription(JavadocUtil.findFirstToken(child,
                                            JavadocTokenTypes.DESCRIPTION)));
                        }
                        break;
                    default:
                        if (!allowUnknownTags) {
                            log(child.getLineNumber(), child.getColumnNumber(), MSG_UNKNOWN_TAG,
                                JavadocUtil.findFirstToken(child, JavadocTokenTypes.CUSTOM_NAME)
                                    .getText().substring(1));
                        }
                        break;
                }
            }
        }

        finalizeValidation(targetAst.getLineNo(), isOuterMostType, hasAuthor, hasVersion,
                typeParamNames);
    }

    private void validateParameter(DetailNode child, List<String> typeParamNames) {
        String name = JavadocUtil.findFirstToken(child, JavadocTokenTypes.PARAMETER_NAME).getText();

        if (name.startsWith(OPEN_ANGLE_BRACKET) && name.endsWith(CLOSE_ANGLE_BRACKET)) {
            name = name.substring(1, name.length() - 1);
        }

        if (!typeParamNames.remove(name)) {
            log(child.getLineNumber(), child.getColumnNumber(), MSG_UNUSED_TAG, "@param",
                    OPEN_ANGLE_BRACKET + name + CLOSE_ANGLE_BRACKET);
        }
    }

    private void finalizeValidation(int lineNo, boolean isOuterMostType, boolean hasAuthor,
            boolean hasVersion, List<String> typeParamNames) {
        if (isOuterMostType) {
            if (!hasAuthor && authorFormat != null) {
                log(lineNo, MSG_MISSING_TAG, AUTHOR_TAG);
            }
            if (!hasVersion && versionFormat != null) {
                log(lineNo, MSG_MISSING_TAG, VERSION_TAG);
            }
        }

        for (String typeParamName : typeParamNames) {
            if (!allowMissingParamTags) {
                log(lineNo, MSG_MISSING_TAG,
                        "@param " + OPEN_ANGLE_BRACKET + typeParamName + CLOSE_ANGLE_BRACKET);
            }
        }
    }

    private boolean isAllowedTarget(DetailAST ast) {
        return target.contains(ast.getType());
    }

    /**
     * Whether we should check this node.
     * @param ast a given node.
     * @return whether we should check a given node.
     */
    private boolean shouldCheck(final DetailAST ast) {
        final Scope customScope;

        if (ScopeUtil.isInInterfaceOrAnnotationBlock(ast)) {
            customScope = Scope.PUBLIC;
        }
        else {
            final DetailAST mods = ast.findFirstToken(TokenTypes.MODIFIERS);
            customScope = ScopeUtil.getScopeFromMods(mods);
        }
        final Scope surroundingScope = ScopeUtil.getSurroundingScope(ast);

        return customScope.isIn(scope)
            && (surroundingScope == null || surroundingScope.isIn(scope))
            && (excludeScope == null
                || !customScope.isIn(excludeScope)
                || surroundingScope != null
                && !surroundingScope.isIn(excludeScope))
            && !AnnotationUtil.containsAnnotation(ast, allowedAnnotations);
    }

    private String getTagDescription(DetailNode ast) {
        final StringBuilder result = new StringBuilder();

        if (ast.getChildren().length == 0) {
            result.append(ast.getText());
        }
        else {
            for (DetailNode child : ast.getChildren()) {
                result.append(getTagDescription(child));
            }
        }

        return result.toString();
    }

    private void checkFormat(int lineNumber, String tagName, Pattern formatPattern, String text) {
        if (formatPattern != null && !formatPattern.matcher(text).find()) {
            log(lineNumber, MSG_TAG_FORMAT, tagName, formatPattern.pattern());
        }
    }

}
