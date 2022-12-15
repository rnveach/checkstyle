///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Utility class that has methods to check javadoc comment position in java file.
 *
 */
public final class BlockCommentPosition {

    /**
     * Forbid new instances.
     */
    private BlockCommentPosition() {
    }

    /**
     * Node is on type definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before class, interface, enum or annotation.
     */
    public static DetailAST getOnType(DetailAST blockComment) {
        DetailAST result = getOnClass(blockComment);
        if (result == null) {
            result = getOnInterface(blockComment);
            if (result == null) {
                result = getOnEnum(blockComment);
                if (result == null) {
                    result = getOnType2(blockComment);
                }
            }
        }

        return result;
    }

    /**
     * Node is on type definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before class, interface, enum or annotation.
     */
    private static DetailAST getOnType2(DetailAST blockComment) {
        DetailAST result = getOnAnnotationDef(blockComment);
        if (result == null) {
            result = getOnRecord(blockComment);
        }

        return result;
    }

    /**
     * Node is on class definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before class
     */
    public static DetailAST getOnClass(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.CLASS_DEF,
                TokenTypes.LITERAL_CLASS);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.CLASS_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.CLASS_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on record definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before class
     */
    public static DetailAST getOnRecord(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.RECORD_DEF,
                TokenTypes.LITERAL_RECORD);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.RECORD_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.RECORD_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on package definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before package
     */
    public static DetailAST getOnPackage(DetailAST blockComment) {
        DetailAST result = getOnTokenWithAnnotation(blockComment, TokenTypes.PACKAGE_DEF);

        if (result == null) {
            DetailAST nextSibling = blockComment.getNextSibling();

            while (nextSibling != null
                    && nextSibling.getType() == TokenTypes.SINGLE_LINE_COMMENT) {
                nextSibling = nextSibling.getNextSibling();
            }

            if (nextSibling != null && nextSibling.getType() == TokenTypes.PACKAGE_DEF) {
                result = nextSibling;
            }
        }

        return result;
    }

    /**
     * Node is on interface definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before interface
     */
    public static DetailAST getOnInterface(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.INTERFACE_DEF,
                TokenTypes.LITERAL_INTERFACE);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.INTERFACE_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.INTERFACE_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on enum definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before enum
     */
    public static DetailAST getOnEnum(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.ENUM_DEF, TokenTypes.ENUM);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.ENUM_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.ENUM_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on annotation definition.
     *
     * @param blockComment DetailAST
     * @return true if node is before annotation
     */
    public static DetailAST getOnAnnotationDef(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.ANNOTATION_DEF, TokenTypes.AT);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.ANNOTATION_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.ANNOTATION_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on type member declaration.
     *
     * @param blockComment DetailAST
     * @return true if node is before method, field, constructor, enum constant
     *     or annotation field
     */
    public static DetailAST getOnMember(DetailAST blockComment) {
        DetailAST result = getOnMethod(blockComment);
        if (result == null) {
            result = getOnField(blockComment);
            if (result == null) {
                result = getOnField(blockComment);
                if (result == null) {
                    result = getOnMember2(blockComment);
                }
            }
        }

        return result;
    }

    /**
     * Node is on type member declaration.
     *
     * @param blockComment DetailAST
     * @return true if node is before enum constant or annotation field
     */
    private static DetailAST getOnMember2(DetailAST blockComment) {
        DetailAST result = getOnConstructor(blockComment);
        if (result == null) {
            result = getOnEnumConstant(blockComment);
            if (result == null) {
                result = getOnAnnotationField(blockComment);
                if (result == null) {
                    result = getOnCompactConstructor(blockComment);
                }
            }
        }

        return result;
    }

    /**
     * Node is on method declaration.
     *
     * @param blockComment DetailAST
     * @return true if node is before method
     */
    public static DetailAST getOnMethod(DetailAST blockComment) {
        DetailAST result = getOnPlainClassMember(blockComment, TokenTypes.METHOD_DEF);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.METHOD_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.METHOD_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on field declaration.
     *
     * @param blockComment DetailAST
     * @return true if node is before field
     */
    public static DetailAST getOnField(DetailAST blockComment) {
        DetailAST result = getOnPlainClassMember(blockComment, TokenTypes.VARIABLE_DEF);
        if (result == null && blockComment.getParent().getParent() != null) {
            if (blockComment.getParent().getParent().getParent() != null && blockComment.getParent()
                    .getParent().getParent().getType() == TokenTypes.OBJBLOCK) {
                result = getOnTokenWithModifiers(blockComment, TokenTypes.VARIABLE_DEF);
            }
            if (result == null
                    && blockComment.getParent().getParent().getParent() != null
                    && blockComment.getParent().getParent().getParent().getParent() != null
                    && blockComment.getParent().getParent().getParent().getParent()
                            .getType() == TokenTypes.OBJBLOCK) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.VARIABLE_DEF);
            }
        }

        return result;
    }

    /**
     * Node is on constructor.
     *
     * @param blockComment DetailAST
     * @return true if node is before constructor
     */
    public static DetailAST getOnConstructor(DetailAST blockComment) {
        DetailAST result = getOnPlainToken(blockComment, TokenTypes.CTOR_DEF, TokenTypes.IDENT);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.CTOR_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.CTOR_DEF);
                if (result == null) {
                    result = getOnPlainClassMember(blockComment, TokenTypes.CTOR_DEF);
                }
            }
        }

        return result;
    }

    /**
     * Node is on compact constructor, note that we don't need to check for a plain
     * token here, since a compact constructor must be public.
     *
     * @param blockComment DetailAST
     * @return true if node is before compact constructor
     */
    public static DetailAST getOnCompactConstructor(DetailAST blockComment) {
        DetailAST result = getOnTokenWithModifiers(blockComment, TokenTypes.COMPACT_CTOR_DEF);
        if (result == null) {
            result = getOnTokenWithAnnotation(blockComment, TokenTypes.COMPACT_CTOR_DEF);
        }
        return result;
    }

    /**
     * Node is on enum constant.
     *
     * @param blockComment DetailAST
     * @return true if node is before enum constant
     */
    public static DetailAST getOnEnumConstant(DetailAST blockComment) {
        final DetailAST parent = blockComment.getParent();
        DetailAST result = null;
        if (parent.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
            final DetailAST prevSibling = getPrevSiblingSkipComments(blockComment);
            if (prevSibling.getType() == TokenTypes.ANNOTATIONS && !prevSibling.hasChildren()) {
                result = parent;
            }
        }
        else if (parent.getType() == TokenTypes.ANNOTATION
                && parent.getParent().getParent().getType() == TokenTypes.ENUM_CONSTANT_DEF) {
            result = parent.getParent().getParent();
        }

        return result;
    }

    /**
     * Node is on annotation field declaration.
     *
     * @param blockComment DetailAST
     * @return true if node is before annotation field
     */
    public static DetailAST getOnAnnotationField(DetailAST blockComment) {
        DetailAST result = getOnPlainClassMember(blockComment, TokenTypes.ANNOTATION_FIELD_DEF);
        if (result == null) {
            result = getOnTokenWithModifiers(blockComment, TokenTypes.ANNOTATION_FIELD_DEF);
            if (result == null) {
                result = getOnTokenWithAnnotation(blockComment, TokenTypes.ANNOTATION_FIELD_DEF);
            }
        }

        return result;
    }

    /**
     * Checks that block comment is on specified token without any modifiers.
     *
     * @param blockComment block comment start DetailAST
     * @param parentTokenType parent token type
     * @param nextTokenType next token type
     * @return true if block comment is on specified token without modifiers
     */
    private static DetailAST getOnPlainToken(DetailAST blockComment,
            int parentTokenType, int nextTokenType) {
        DetailAST result = null;
        if (blockComment.getParent().getType() == parentTokenType
                && !getPrevSiblingSkipComments(blockComment).hasChildren()
                && getNextSiblingSkipComments(blockComment).getType() == nextTokenType) {
            result = blockComment.getParent();
        }
        return result;
    }

    /**
     * Checks that block comment is on specified token with modifiers.
     *
     * @param blockComment block comment start DetailAST
     * @param tokenType parent token type
     * @return true if block comment is on specified token with modifiers
     */
    private static DetailAST getOnTokenWithModifiers(DetailAST blockComment, int tokenType) {
        DetailAST result = null;
        if (blockComment.getParent().getType() == TokenTypes.MODIFIERS
                && blockComment.getParent().getParent().getType() == tokenType
                && getPrevSiblingSkipComments(blockComment) == null) {
            result = blockComment.getParent().getParent();
        }
        return result;
    }

    /**
     * Checks that block comment is on specified token with annotation.
     *
     * @param blockComment block comment start DetailAST
     * @param tokenType parent token type
     * @return true if block comment is on specified token with annotation
     */
    private static DetailAST getOnTokenWithAnnotation(DetailAST blockComment, int tokenType) {
        DetailAST result = null;
        if (blockComment.getParent().getType() == TokenTypes.ANNOTATION
                && getPrevSiblingSkipComments(blockComment.getParent()) == null
                && blockComment.getParent().getParent().getParent().getType() == tokenType
                && getPrevSiblingSkipComments(blockComment) == null) {
            result = blockComment.getParent().getParent().getParent();
        }
        return result;
    }

    /**
     * Checks that block comment is on specified class member without any modifiers.
     *
     * @param blockComment block comment start DetailAST
     * @param memberType parent token type
     * @return true if block comment is on specified token without modifiers
     */
    private static DetailAST getOnPlainClassMember(DetailAST blockComment, int memberType) {
        DetailAST result = null;
        DetailAST parent = blockComment.getParent();
        // type could be in fully qualified form, so we go up to Type token
        while (parent.getType() == TokenTypes.DOT) {
            parent = parent.getParent();
        }
        if ((parent.getType() == TokenTypes.TYPE
                    || parent.getType() == TokenTypes.TYPE_PARAMETERS)
                && parent.getParent().getType() == memberType
                // previous parent sibling is always TokenTypes.MODIFIERS
                && !parent.getPreviousSibling().hasChildren()
                && parent.getParent().getParent().getType() == TokenTypes.OBJBLOCK) {
            result = parent.getParent();
        }
        return result;
    }

    /**
     * Get next sibling node skipping any comment nodes.
     *
     * @param node current node
     * @return next sibling
     */
    private static DetailAST getNextSiblingSkipComments(DetailAST node) {
        DetailAST result = node.getNextSibling();
        while (result.getType() == TokenTypes.SINGLE_LINE_COMMENT
                || result.getType() == TokenTypes.BLOCK_COMMENT_BEGIN) {
            result = result.getNextSibling();
        }
        return result;
    }

    /**
     * Get previous sibling node skipping any comments.
     *
     * @param node current node
     * @return previous sibling
     */
    private static DetailAST getPrevSiblingSkipComments(DetailAST node) {
        DetailAST result = node.getPreviousSibling();
        while (result != null
                && (result.getType() == TokenTypes.SINGLE_LINE_COMMENT
                || result.getType() == TokenTypes.BLOCK_COMMENT_BEGIN)) {
            result = result.getPreviousSibling();
        }
        return result;
    }

}
