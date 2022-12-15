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

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.internal.utils.TestUtil;

public class BlockCommentPositionTest extends AbstractModuleTestSupport {

    @Test
    public void testPrivateConstr() throws Exception {
        assertWithMessage("Constructor is not private")
                .that(TestUtil.isUtilsClassHasPrivateConstructor(BlockCommentPosition.class))
                .isTrue();
    }

    @Test
    public void testJavaDocsRecognition() throws Exception {
        final List<BlockCommentPositionTestMetadata> metadataList = Arrays.asList(
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnClass.java",
                        BlockCommentPosition::getOnClass, 3),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnMethod.java",
                        BlockCommentPosition::getOnMethod, 6),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnField.java",
                        BlockCommentPosition::getOnField, 3),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnEnum.java",
                        BlockCommentPosition::getOnEnum, 3),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnConstructor.java",
                        BlockCommentPosition::getOnConstructor, 5),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnInterface.java",
                        BlockCommentPosition::getOnInterface, 3),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnAnnotation.java",
                        BlockCommentPosition::getOnAnnotationDef, 3),
                new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnEnumMember.java",
                        BlockCommentPosition::getOnEnumConstant, 2),
                new BlockCommentPositionTestMetadata(
                        "InputBlockCommentPositionOnAnnotationField.java",
                        BlockCommentPosition::getOnAnnotationField, 4),
                new BlockCommentPositionTestMetadata(
                        "inputs/normal/package-info.java",
                        BlockCommentPosition::getOnPackage, 1),
                new BlockCommentPositionTestMetadata(
                        "inputs/annotation/package-info.java",
                        BlockCommentPosition::getOnPackage, 1)
        );

        for (BlockCommentPositionTestMetadata metadata : metadataList) {
            final DetailAST ast = JavaParser.parseFile(new File(getPath(metadata.getFileName())),
                JavaParser.Options.WITH_COMMENTS);
            final int matches = getJavadocsCount(ast, metadata.getAssertion());
            assertWithMessage("Invalid javadoc count")
                    .that(matches)
                    .isEqualTo(metadata.getMatchesNum());
        }
    }

    @Test
    public void testJavaDocsRecognitionNonCompilable() throws Exception {
        final List<BlockCommentPositionTestMetadata> metadataList = Arrays.asList(
            new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnRecord.java",
                BlockCommentPosition::getOnRecord, 3),
            new BlockCommentPositionTestMetadata("InputBlockCommentPositionOnCompactCtor.java",
                BlockCommentPosition::getOnCompactConstructor, 3)
        );

        for (BlockCommentPositionTestMetadata metadata : metadataList) {
            final DetailAST ast = JavaParser.parseFile(
                new File(getNonCompilablePath(metadata.getFileName())),
                    JavaParser.Options.WITH_COMMENTS);
            final int matches = getJavadocsCount(ast, metadata.getAssertion());
            assertWithMessage("Invalid javadoc count")
                    .that(matches)
                    .isEqualTo(metadata.getMatchesNum());
        }
    }

    private static int getJavadocsCount(DetailAST detailAST,
                                        Function<DetailAST, DetailAST> assertion) {
        int matchFound = 0;
        DetailAST node = detailAST;
        while (node != null) {
            if (node.getType() == TokenTypes.BLOCK_COMMENT_BEGIN
                    && JavadocUtil.isJavadocComment(node)
                    && JavadocUtil.getAssociatedJavadocTarget(node) != null) {
                if (assertion.apply(node) == null) {
                    throw new IllegalStateException("Position of comment is defined correctly");
                }
                matchFound++;
            }
            matchFound += getJavadocsCount(node.getFirstChild(), assertion);
            node = node.getNextSibling();
        }
        return matchFound;
    }

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/utils/blockcommentposition";
    }

    private static final class BlockCommentPositionTestMetadata {

        private final String fileName;
        private final Function<DetailAST, DetailAST> assertion;
        private final int matchesNum;

        private BlockCommentPositionTestMetadata(String fileName, Function<DetailAST,
                DetailAST> assertion, int matchesNum) {
            this.fileName = fileName;
            this.assertion = assertion;
            this.matchesNum = matchesNum;
        }

        public String getFileName() {
            return fileName;
        }

        public Function<DetailAST, DetailAST> getAssertion() {
            return assertion;
        }

        public int getMatchesNum() {
            return matchesNum;
        }

    }

}
