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

package com.puppycrawl.tools.checkstyle;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import antlr.CommonHiddenStreamToken;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import antlr.TokenStreamHiddenTokenFilter;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.grammar.GeneratedJavaLexer;
import com.puppycrawl.tools.checkstyle.grammar.GeneratedJavaRecognizer;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Helper methods to parse java source files.
 *
 */
public final class JavaParser {

    /**
     * Parse error.
     */
    public static final String MSG_JAVA_PARSE_ERROR = "java.parse.error";

    /**
     * Enum to be used for test if comments should be used.
     */
    public enum Options {

        /**
         * Comments nodes should be processed.
         */
        WITH_COMMENTS,

        /**
         * Comments nodes should be ignored.
         */
        WITHOUT_COMMENTS,

    }

    /** Stop instances being created. **/
    private JavaParser() {
    }

    /**
     * Static helper method to parses a Java source file.
     * @param contents contains the contents of the file
     * @return Parse status of Java file
     */
    public static ParseStatus parse(FileContents contents) {
        final String fullText = contents.getText().getFullText().toString();
        final Reader reader = new StringReader(fullText);
        final GeneratedJavaLexer lexer = new GeneratedJavaLexer(reader);
        lexer.setCommentListener(contents);
        lexer.setTokenObjectClass("antlr.CommonHiddenStreamToken");

        final TokenStreamHiddenTokenFilter filter = new TokenStreamHiddenTokenFilter(lexer);
        filter.hide(TokenTypes.SINGLE_LINE_COMMENT);
        filter.hide(TokenTypes.BLOCK_COMMENT_BEGIN);

        final ParseStatus result = new ParseStatus();

        final GeneratedJavaRecognizer parser = new GeneratedJavaRecognizer(filter);
        parser.setFilename(contents.getFileName());
        parser.setASTNodeClass(DetailAST.class.getName());
        try {
            parser.compilationUnit();

            result.setTree((DetailAST) parser.getAST());
        }
        catch (RecognitionException | TokenStreamException ex) {
            final ParseErrorMessage parseErrorMessage = new ParseErrorMessage(MSG_JAVA_PARSE_ERROR,
                    ex.getClass().getSimpleName());

            result.setParseErrorMessage(parseErrorMessage);
        }

        return result;
    }

    /**
     * Parse a text and return the parse tree.
     * @param text the text to parse
     * @param options {@link Options} to control inclusion of comment nodes
     * @return Parse status of Java file
     */
    public static ParseStatus parseFileText(FileText text, Options options) {
        final FileContents contents = new FileContents(text);
        final ParseStatus result = parse(contents);
        if (options == Options.WITH_COMMENTS) {
            result.setTree(appendHiddenCommentNodes(result.getTree()));
        }
        return result;
    }

    /**
     * Parses Java source file.
     * @param file the file to parse
     * @param options {@link Options} to control inclusion of comment nodes
     * @return DetailAST tree
     * @throws IOException if the file could not be read
     */
    public static ParseStatus parseFile(File file, Options options)
            throws IOException {
        final FileText text = new FileText(file.getAbsoluteFile(),
            System.getProperty("file.encoding", StandardCharsets.UTF_8.name()));
        return parseFileText(text, options);
    }

    /**
     * Appends comment nodes to existing AST.
     * It traverses each node in AST, looks for hidden comment tokens
     * and appends found comment tokens as nodes in AST.
     * @param root of AST
     * @return root of AST with comment nodes
     */
    public static DetailAST appendHiddenCommentNodes(DetailAST root) {
        DetailAST result = root;
        DetailAST curNode = root;
        DetailAST lastNode = root;

        while (curNode != null) {
            lastNode = curNode;

            CommonHiddenStreamToken tokenBefore = curNode.getHiddenBefore();
            DetailAST currentSibling = curNode;
            while (tokenBefore != null) {
                final DetailAST newCommentNode =
                         createCommentAstFromToken(tokenBefore);

                currentSibling.addPreviousSibling(newCommentNode);

                if (currentSibling == result) {
                    result = newCommentNode;
                }

                currentSibling = newCommentNode;
                tokenBefore = tokenBefore.getHiddenBefore();
            }

            DetailAST toVisit = curNode.getFirstChild();
            while (curNode != null && toVisit == null) {
                toVisit = curNode.getNextSibling();
                curNode = curNode.getParent();
            }
            curNode = toVisit;
        }
        if (lastNode != null) {
            CommonHiddenStreamToken tokenAfter = lastNode.getHiddenAfter();
            DetailAST currentSibling = lastNode;
            while (tokenAfter != null) {
                final DetailAST newCommentNode =
                        createCommentAstFromToken(tokenAfter);

                currentSibling.addNextSibling(newCommentNode);

                currentSibling = newCommentNode;
                tokenAfter = tokenAfter.getHiddenAfter();
            }
        }
        return result;
    }

    /**
     * Create comment AST from token. Depending on token type
     * SINGLE_LINE_COMMENT or BLOCK_COMMENT_BEGIN is created.
     * @param token to create the AST
     * @return DetailAST of comment node
     */
    private static DetailAST createCommentAstFromToken(Token token) {
        final DetailAST commentAst;
        if (token.getType() == TokenTypes.SINGLE_LINE_COMMENT) {
            commentAst = createSlCommentNode(token);
        }
        else {
            commentAst = CommonUtil.createBlockCommentNode(token);
        }
        return commentAst;
    }

    /**
     * Create single-line comment from token.
     * @param token to create the AST
     * @return DetailAST with SINGLE_LINE_COMMENT type
     */
    private static DetailAST createSlCommentNode(Token token) {
        final DetailAST slComment = new DetailAST();
        slComment.setType(TokenTypes.SINGLE_LINE_COMMENT);
        slComment.setText("//");

        // column counting begins from 0
        slComment.setColumnNo(token.getColumn() - 1);
        slComment.setLineNo(token.getLine());

        final DetailAST slCommentContent = new DetailAST();
        slCommentContent.setType(TokenTypes.COMMENT_CONTENT);

        // column counting begins from 0
        // plus length of '//'
        slCommentContent.setColumnNo(token.getColumn() - 1 + 2);
        slCommentContent.setLineNo(token.getLine());
        slCommentContent.setText(token.getText());

        slComment.addChild(slCommentContent);
        return slComment;
    }



    /**
     * Contains result of parsing java: DetailAST tree and parse
     * error message.
     */
    public static class ParseStatus {

        /**
         * DetailAST tree (is null if parsing fails).
         */
        private DetailAST tree;

        /**
         * Parse error message (is null if parsing is successful).
         */
        private ParseErrorMessage parseErrorMessage;

        /**
         * Getter for DetailAST tree.
         * @return DetailAST tree if parsing was successful, null otherwise.
         */
        public DetailAST getTree() {
            return tree;
        }

        /**
         * Sets DetailAST tree.
         * @param tree DetailNode tree.
         */
        public void setTree(DetailAST tree) {
            this.tree = tree;
        }

        /**
         * Getter for error message during parsing.
         * @return Error message if parsing was unsuccessful, null otherwise.
         */
        public ParseErrorMessage getParseErrorMessage() {
            return parseErrorMessage;
        }

        /**
         * Sets parse error message.
         * @param parseErrorMessage Parse error message.
         */
        public void setParseErrorMessage(ParseErrorMessage parseErrorMessage) {
            this.parseErrorMessage = parseErrorMessage;
        }

    }

    /**
     * Contains information about parse error message.
     */
    public static class ParseErrorMessage {

        /**
         * Key for error message.
         */
        private final String messageKey;

        /**
         * Error message arguments.
         */
        private final Object[] messageArguments;

        /**
         * Initializes parse error message.
         *
         * @param messageKey message key
         * @param messageArguments message arguments
         */
        ParseErrorMessage(String messageKey, Object... messageArguments) {
            this.messageKey = messageKey;
            this.messageArguments = messageArguments.clone();
        }

        /**
         * Getter for key for error message.
         * @return Key for error message.
         */
        public String getMessageKey() {
            return messageKey;
        }

        /**
         * Getter for error message arguments.
         * @return Array of error message arguments.
         */
        public Object[] getMessageArguments() {
            return messageArguments.clone();
        }

    }

}
