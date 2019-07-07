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

package com.puppycrawl.tools.checkstyle.grammar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * GeneratedJavaTokenTypesTest.
 * @noinspection ClassIndependentOfModule
 */
public class GeneratedJavaTokenTypesTest {

    /**
     * <p>
     * New tokens must be added onto the end of the list with new numbers, and
     * old tokens must remain and keep their current numbering. Old token
     * numberings are not allowed to change.
     * </p>
     *
     * <p>
     * The reason behind this is Java inlines static final field values directly
     * into the compiled Java code. This loses all connections with the original
     * class, GeneratedJavaTokenTypes, and so numbering updates are not picked
     * up in user-created checks and causes conflicts.
     * </p>
     *
     * <p>
     * Issue: https://github.com/checkstyle/checkstyle/issues/505
     * </p>
     */
    @Test
    public void testTokenNumbering() {
        final String message = "A token's number has changed. Please open"
                + " 'GeneratedJavaTokenTypesTest' and confirm which token is at fault.\n"
                + "Token numbers must not change or else they will create a conflict"
                + " with users.\n\n"
                + "See Issue: https://github.com/checkstyle/checkstyle/issues/505";

        // Read JavaDoc before changing
        assertEquals(message, 1, GeneratedJavaTokenTypes.EOF);
        assertEquals(message, 3, GeneratedJavaTokenTypes.NULL_TREE_LOOKAHEAD);
        assertEquals(message, 4, GeneratedJavaTokenTypes.BLOCK);
        assertEquals(message, 5, GeneratedJavaTokenTypes.MODIFIERS);
        assertEquals(message, 6, GeneratedJavaTokenTypes.OBJBLOCK);
        assertEquals(message, 7, GeneratedJavaTokenTypes.SLIST);
        assertEquals(message, 8, GeneratedJavaTokenTypes.CTOR_DEF);
        assertEquals(message, 9, GeneratedJavaTokenTypes.METHOD_DEF);
        assertEquals(message, 10, GeneratedJavaTokenTypes.VARIABLE_DEF);
        assertEquals(message, 11, GeneratedJavaTokenTypes.INSTANCE_INIT);
        assertEquals(message, 12, GeneratedJavaTokenTypes.STATIC_INIT);
        assertEquals(message, 13, GeneratedJavaTokenTypes.TYPE);
        assertEquals(message, 14, GeneratedJavaTokenTypes.CLASS_DEF);
        assertEquals(message, 15, GeneratedJavaTokenTypes.INTERFACE_DEF);
        assertEquals(message, 16, GeneratedJavaTokenTypes.PACKAGE_DEF);
        assertEquals(message, 17, GeneratedJavaTokenTypes.ARRAY_DECLARATOR);
        assertEquals(message, 18, GeneratedJavaTokenTypes.EXTENDS_CLAUSE);
        assertEquals(message, 19, GeneratedJavaTokenTypes.IMPLEMENTS_CLAUSE);
        assertEquals(message, 20, GeneratedJavaTokenTypes.PARAMETERS);
        assertEquals(message, 21, GeneratedJavaTokenTypes.PARAMETER_DEF);
        assertEquals(message, 22, GeneratedJavaTokenTypes.LABELED_STAT);
        assertEquals(message, 23, GeneratedJavaTokenTypes.TYPECAST);
        assertEquals(message, 24, GeneratedJavaTokenTypes.INDEX_OP);
        assertEquals(message, 25, GeneratedJavaTokenTypes.POST_INC);
        assertEquals(message, 26, GeneratedJavaTokenTypes.POST_DEC);
        assertEquals(message, 27, GeneratedJavaTokenTypes.METHOD_CALL);
        assertEquals(message, 28, GeneratedJavaTokenTypes.EXPR);
        assertEquals(message, 29, GeneratedJavaTokenTypes.ARRAY_INIT);
        assertEquals(message, 30, GeneratedJavaTokenTypes.IMPORT);
        assertEquals(message, 31, GeneratedJavaTokenTypes.UNARY_MINUS);
        assertEquals(message, 32, GeneratedJavaTokenTypes.UNARY_PLUS);
        assertEquals(message, 33, GeneratedJavaTokenTypes.CASE_GROUP);
        assertEquals(message, 34, GeneratedJavaTokenTypes.ELIST);
        assertEquals(message, 35, GeneratedJavaTokenTypes.FOR_INIT);
        assertEquals(message, 36, GeneratedJavaTokenTypes.FOR_CONDITION);
        assertEquals(message, 37, GeneratedJavaTokenTypes.FOR_ITERATOR);
        assertEquals(message, 38, GeneratedJavaTokenTypes.EMPTY_STAT);
        assertEquals(message, 39, GeneratedJavaTokenTypes.FINAL);
        assertEquals(message, 40, GeneratedJavaTokenTypes.ABSTRACT);
        assertEquals(message, 41, GeneratedJavaTokenTypes.STRICTFP);
        assertEquals(message, 42, GeneratedJavaTokenTypes.SUPER_CTOR_CALL);
        assertEquals(message, 43, GeneratedJavaTokenTypes.CTOR_CALL);
        assertEquals(message, 44, GeneratedJavaTokenTypes.LITERAL_package);
        assertEquals(message, 45, GeneratedJavaTokenTypes.SEMI);
        assertEquals(message, 46, GeneratedJavaTokenTypes.LITERAL_import);
        assertEquals(message, 47, GeneratedJavaTokenTypes.LBRACK);
        assertEquals(message, 48, GeneratedJavaTokenTypes.RBRACK);
        assertEquals(message, 49, GeneratedJavaTokenTypes.LITERAL_void);
        assertEquals(message, 50, GeneratedJavaTokenTypes.LITERAL_boolean);
        assertEquals(message, 51, GeneratedJavaTokenTypes.LITERAL_byte);
        assertEquals(message, 52, GeneratedJavaTokenTypes.LITERAL_char);
        assertEquals(message, 53, GeneratedJavaTokenTypes.LITERAL_short);
        assertEquals(message, 54, GeneratedJavaTokenTypes.LITERAL_int);
        assertEquals(message, 55, GeneratedJavaTokenTypes.LITERAL_float);
        assertEquals(message, 56, GeneratedJavaTokenTypes.LITERAL_long);
        assertEquals(message, 57, GeneratedJavaTokenTypes.LITERAL_double);
        assertEquals(message, 58, GeneratedJavaTokenTypes.IDENT);
        assertEquals(message, 59, GeneratedJavaTokenTypes.DOT);
        assertEquals(message, 60, GeneratedJavaTokenTypes.STAR);
        assertEquals(message, 61, GeneratedJavaTokenTypes.LITERAL_private);
        assertEquals(message, 62, GeneratedJavaTokenTypes.LITERAL_public);
        assertEquals(message, 63, GeneratedJavaTokenTypes.LITERAL_protected);
        assertEquals(message, 64, GeneratedJavaTokenTypes.LITERAL_static);
        assertEquals(message, 65, GeneratedJavaTokenTypes.LITERAL_transient);
        assertEquals(message, 66, GeneratedJavaTokenTypes.LITERAL_native);
        assertEquals(message, 67, GeneratedJavaTokenTypes.LITERAL_synchronized);
        assertEquals(message, 68, GeneratedJavaTokenTypes.LITERAL_volatile);
        assertEquals(message, 69, GeneratedJavaTokenTypes.LITERAL_class);
        assertEquals(message, 70, GeneratedJavaTokenTypes.LITERAL_extends);
        assertEquals(message, 71, GeneratedJavaTokenTypes.LITERAL_interface);
        assertEquals(message, 72, GeneratedJavaTokenTypes.LCURLY);
        assertEquals(message, 73, GeneratedJavaTokenTypes.RCURLY);
        assertEquals(message, 74, GeneratedJavaTokenTypes.COMMA);
        assertEquals(message, 75, GeneratedJavaTokenTypes.LITERAL_implements);
        assertEquals(message, 76, GeneratedJavaTokenTypes.LPAREN);
        assertEquals(message, 77, GeneratedJavaTokenTypes.RPAREN);
        assertEquals(message, 78, GeneratedJavaTokenTypes.LITERAL_this);
        assertEquals(message, 79, GeneratedJavaTokenTypes.LITERAL_super);
        assertEquals(message, 80, GeneratedJavaTokenTypes.ASSIGN);
        assertEquals(message, 81, GeneratedJavaTokenTypes.LITERAL_throws);
        assertEquals(message, 82, GeneratedJavaTokenTypes.COLON);
        assertEquals(message, 83, GeneratedJavaTokenTypes.LITERAL_if);
        assertEquals(message, 84, GeneratedJavaTokenTypes.LITERAL_while);
        assertEquals(message, 85, GeneratedJavaTokenTypes.LITERAL_do);
        assertEquals(message, 86, GeneratedJavaTokenTypes.LITERAL_break);
        assertEquals(message, 87, GeneratedJavaTokenTypes.LITERAL_continue);
        assertEquals(message, 88, GeneratedJavaTokenTypes.LITERAL_return);
        assertEquals(message, 89, GeneratedJavaTokenTypes.LITERAL_switch);
        assertEquals(message, 90, GeneratedJavaTokenTypes.LITERAL_throw);
        assertEquals(message, 91, GeneratedJavaTokenTypes.LITERAL_for);
        assertEquals(message, 92, GeneratedJavaTokenTypes.LITERAL_else);
        assertEquals(message, 93, GeneratedJavaTokenTypes.LITERAL_case);
        assertEquals(message, 94, GeneratedJavaTokenTypes.LITERAL_default);
        assertEquals(message, 95, GeneratedJavaTokenTypes.LITERAL_try);
        assertEquals(message, 96, GeneratedJavaTokenTypes.LITERAL_catch);
        assertEquals(message, 97, GeneratedJavaTokenTypes.LITERAL_finally);
        assertEquals(message, 98, GeneratedJavaTokenTypes.PLUS_ASSIGN);
        assertEquals(message, 99, GeneratedJavaTokenTypes.MINUS_ASSIGN);
        assertEquals(message, 100, GeneratedJavaTokenTypes.STAR_ASSIGN);
        assertEquals(message, 101, GeneratedJavaTokenTypes.DIV_ASSIGN);
        assertEquals(message, 102, GeneratedJavaTokenTypes.MOD_ASSIGN);
        assertEquals(message, 103, GeneratedJavaTokenTypes.SR_ASSIGN);
        assertEquals(message, 104, GeneratedJavaTokenTypes.BSR_ASSIGN);
        assertEquals(message, 105, GeneratedJavaTokenTypes.SL_ASSIGN);
        assertEquals(message, 106, GeneratedJavaTokenTypes.BAND_ASSIGN);
        assertEquals(message, 107, GeneratedJavaTokenTypes.BXOR_ASSIGN);
        assertEquals(message, 108, GeneratedJavaTokenTypes.BOR_ASSIGN);
        assertEquals(message, 109, GeneratedJavaTokenTypes.QUESTION);
        assertEquals(message, 110, GeneratedJavaTokenTypes.LOR);
        assertEquals(message, 111, GeneratedJavaTokenTypes.LAND);
        assertEquals(message, 112, GeneratedJavaTokenTypes.BOR);
        assertEquals(message, 113, GeneratedJavaTokenTypes.BXOR);
        assertEquals(message, 114, GeneratedJavaTokenTypes.BAND);
        assertEquals(message, 115, GeneratedJavaTokenTypes.NOT_EQUAL);
        assertEquals(message, 116, GeneratedJavaTokenTypes.EQUAL);
        assertEquals(message, 117, GeneratedJavaTokenTypes.LT);
        assertEquals(message, 118, GeneratedJavaTokenTypes.GT);
        assertEquals(message, 119, GeneratedJavaTokenTypes.LE);
        assertEquals(message, 120, GeneratedJavaTokenTypes.GE);
        assertEquals(message, 121, GeneratedJavaTokenTypes.LITERAL_instanceof);
        assertEquals(message, 122, GeneratedJavaTokenTypes.SL);
        assertEquals(message, 123, GeneratedJavaTokenTypes.SR);
        assertEquals(message, 124, GeneratedJavaTokenTypes.BSR);
        assertEquals(message, 125, GeneratedJavaTokenTypes.PLUS);
        assertEquals(message, 126, GeneratedJavaTokenTypes.MINUS);
        assertEquals(message, 127, GeneratedJavaTokenTypes.DIV);
        assertEquals(message, 128, GeneratedJavaTokenTypes.MOD);
        assertEquals(message, 129, GeneratedJavaTokenTypes.INC);
        assertEquals(message, 130, GeneratedJavaTokenTypes.DEC);
        assertEquals(message, 131, GeneratedJavaTokenTypes.BNOT);
        assertEquals(message, 132, GeneratedJavaTokenTypes.LNOT);
        assertEquals(message, 133, GeneratedJavaTokenTypes.LITERAL_true);
        assertEquals(message, 134, GeneratedJavaTokenTypes.LITERAL_false);
        assertEquals(message, 135, GeneratedJavaTokenTypes.LITERAL_null);
        assertEquals(message, 136, GeneratedJavaTokenTypes.LITERAL_new);
        assertEquals(message, 137, GeneratedJavaTokenTypes.NUM_INT);
        assertEquals(message, 138, GeneratedJavaTokenTypes.CHAR_LITERAL);
        assertEquals(message, 139, GeneratedJavaTokenTypes.STRING_LITERAL);
        assertEquals(message, 140, GeneratedJavaTokenTypes.NUM_FLOAT);
        assertEquals(message, 141, GeneratedJavaTokenTypes.NUM_LONG);
        assertEquals(message, 142, GeneratedJavaTokenTypes.NUM_DOUBLE);
        assertEquals(message, 143, GeneratedJavaTokenTypes.WS);
        assertEquals(message, 144, GeneratedJavaTokenTypes.SINGLE_LINE_COMMENT);
        assertEquals(message, 145, GeneratedJavaTokenTypes.BLOCK_COMMENT_BEGIN);
        assertEquals(message, 146, GeneratedJavaTokenTypes.ESC);
        assertEquals(message, 147, GeneratedJavaTokenTypes.HEX_DIGIT);
        assertEquals(message, 148, GeneratedJavaTokenTypes.VOCAB);
        assertEquals(message, 149, GeneratedJavaTokenTypes.EXPONENT);
        assertEquals(message, 150, GeneratedJavaTokenTypes.FLOAT_SUFFIX);
        assertEquals(message, 151, GeneratedJavaTokenTypes.ASSERT);
        assertEquals(message, 152, GeneratedJavaTokenTypes.STATIC_IMPORT);
        assertEquals(message, 153, GeneratedJavaTokenTypes.ENUM);
        assertEquals(message, 154, GeneratedJavaTokenTypes.ENUM_DEF);
        assertEquals(message, 155, GeneratedJavaTokenTypes.ENUM_CONSTANT_DEF);
        assertEquals(message, 156, GeneratedJavaTokenTypes.FOR_EACH_CLAUSE);
        assertEquals(message, 157, GeneratedJavaTokenTypes.ANNOTATION_DEF);
        assertEquals(message, 158, GeneratedJavaTokenTypes.ANNOTATIONS);
        assertEquals(message, 159, GeneratedJavaTokenTypes.ANNOTATION);
        assertEquals(message, 160, GeneratedJavaTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        assertEquals(message, 161, GeneratedJavaTokenTypes.ANNOTATION_FIELD_DEF);
        assertEquals(message, 162, GeneratedJavaTokenTypes.ANNOTATION_ARRAY_INIT);
        assertEquals(message, 163, GeneratedJavaTokenTypes.TYPE_ARGUMENTS);
        assertEquals(message, 164, GeneratedJavaTokenTypes.TYPE_ARGUMENT);
        assertEquals(message, 165, GeneratedJavaTokenTypes.TYPE_PARAMETERS);
        assertEquals(message, 166, GeneratedJavaTokenTypes.TYPE_PARAMETER);
        assertEquals(message, 167, GeneratedJavaTokenTypes.WILDCARD_TYPE);
        assertEquals(message, 168, GeneratedJavaTokenTypes.TYPE_UPPER_BOUNDS);
        assertEquals(message, 169, GeneratedJavaTokenTypes.TYPE_LOWER_BOUNDS);
        assertEquals(message, 170, GeneratedJavaTokenTypes.AT);
        assertEquals(message, 171, GeneratedJavaTokenTypes.ELLIPSIS);
        assertEquals(message, 172, GeneratedJavaTokenTypes.GENERIC_START);
        assertEquals(message, 173, GeneratedJavaTokenTypes.GENERIC_END);
        assertEquals(message, 174, GeneratedJavaTokenTypes.TYPE_EXTENSION_AND);
        assertEquals(message, 175, GeneratedJavaTokenTypes.DO_WHILE);
        assertEquals(message, 176, GeneratedJavaTokenTypes.RESOURCE_SPECIFICATION);
        assertEquals(message, 177, GeneratedJavaTokenTypes.RESOURCES);
        assertEquals(message, 178, GeneratedJavaTokenTypes.RESOURCE);
        assertEquals(message, 179, GeneratedJavaTokenTypes.DOUBLE_COLON);
        assertEquals(message, 180, GeneratedJavaTokenTypes.METHOD_REF);
        assertEquals(message, 181, GeneratedJavaTokenTypes.LAMBDA);
        assertEquals(message, 182, GeneratedJavaTokenTypes.BLOCK_COMMENT_END);
        assertEquals(message, 183, GeneratedJavaTokenTypes.COMMENT_CONTENT);
        assertEquals(message, 184, GeneratedJavaTokenTypes.SINGLE_LINE_COMMENT_CONTENT);
        assertEquals(message, 185, GeneratedJavaTokenTypes.BLOCK_COMMENT_CONTENT);
        assertEquals(message, 186, GeneratedJavaTokenTypes.STD_ESC);
        assertEquals(message, 187, GeneratedJavaTokenTypes.BINARY_DIGIT);
        assertEquals(message, 188, GeneratedJavaTokenTypes.ID_START);
        assertEquals(message, 189, GeneratedJavaTokenTypes.ID_PART);
        assertEquals(message, 190, GeneratedJavaTokenTypes.INT_LITERAL);
        assertEquals(message, 191, GeneratedJavaTokenTypes.LONG_LITERAL);
        assertEquals(message, 192, GeneratedJavaTokenTypes.FLOAT_LITERAL);
        assertEquals(message, 193, GeneratedJavaTokenTypes.DOUBLE_LITERAL);
        assertEquals(message, 194, GeneratedJavaTokenTypes.HEX_FLOAT_LITERAL);
        assertEquals(message, 195, GeneratedJavaTokenTypes.HEX_DOUBLE_LITERAL);
        assertEquals(message, 196, GeneratedJavaTokenTypes.SIGNED_INTEGER);
        assertEquals(message, 197, GeneratedJavaTokenTypes.BINARY_EXPONENT);
        // Read JavaDoc before changing
        assertEquals("all tokens must be added to list in"
                + " 'GeneratedJavaTokenTypesTest' and verified"
                + " that their old numbering didn't change", 196,
                GeneratedJavaTokenTypes.class.getDeclaredFields().length);
    }

}
