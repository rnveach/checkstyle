////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractPathTestSupport;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.AbstractFrame;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.ClassFrame;
import com.puppycrawl.tools.checkstyle.utils.FrameTrackingUtil.FrameType;

public class FrameTackingUtilTest extends AbstractPathTestSupport {

    private static final String LF_REGEX = "\n";

    private static final String CRLF_REGEX = "\r\n";

    private static final Comparator<DetailAST> astComparator = new Comparator<DetailAST>() {
        @Override
        public int compare(DetailAST o1, DetailAST o2) {
            int d = Integer.compare(o1.getLineNo(), o2.getLineNo());

            if (d == 0)
                d = Integer.compare(o1.getColumnNo(), o2.getColumnNo());

            return d;
        }
    };

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/coding/requirethis";
    }

    @Test
    public void test1() throws Exception {
        final FrameTrackingUtil input = get("InputRequireThisEnumInnerClassesAndBugs.java");

        Assert.assertEquals(readFile(getPath("InputRequireThisEnumInnerClassesAndBugs.txt")),
                getFrameDisplay(input));
    }

    @Test
    public void test2() throws Exception {
        final FrameTrackingUtil input = get("InputRequireThisAllowLambdaParameters.java");

        Assert.assertEquals(readFile(getPath("InputRequireThisAllowLambdaParameters.txt")),
                getFrameDisplay(input));
    }

    private static String getFrameDisplay(FrameTrackingUtil input) {
        String result = "";
        final TreeMap<DetailAST, AbstractFrame> sorted = new TreeMap<>(astComparator);
        sorted.putAll(input.getFrames());

        for (Entry<DetailAST, AbstractFrame> entry : sorted.entrySet()) {
            final DetailAST frameAst = entry.getKey();
            final AbstractFrame frame = entry.getValue();
            final FrameType frameType = frame.getType();

            if (frame.getParent() == null) {
                result += "------------------------------\n";
            }

            result += frameAst.getLineNo() + ":" + frameAst.getColumnNo() + " - "
                    + frame.getType().toString();

            switch (frameType) {
            case CLASS_FRAME:
            case CTOR_FRAME:
            case METHOD_FRAME:
                result += " (" + frame.getFrameName() + ")";
                break;
            case BLOCK_FRAME:
            case CATCH_FRAME:
            case FOR_FRAME:
            default:
                break;
            }

            result += "\n";

            if (frameType == FrameType.METHOD_FRAME) {
                result += printAstNames("Parameters", frame.getVarIdents());
            } else {
                result += printAstNames("Local Variables", frame.getVarIdents());
            }

            if (frameType == FrameType.CLASS_FRAME) {
                result += printAstNames("Static Members", ((ClassFrame) frame).getStaticMembers());
                result += printAstNames("Static Methods", ((ClassFrame) frame).getStaticMethods());
                result += printAstNames("Instance Members",
                        ((ClassFrame) frame).getInstanceMembers());
                result += printAstNames("Instance Methods",
                        ((ClassFrame) frame).getInstanceMethods());
            }

        }

        return result;
    }

    private static String printAstNames(String sectionName, Set<DetailAST> variables) {
        String result = "";

        if (!variables.isEmpty()) {
            result += "    " + sectionName + ": ";

            final TreeSet<DetailAST> sorted = new TreeSet<DetailAST>(astComparator);
            boolean first = true;

            sorted.addAll(variables);

            for (DetailAST ast : sorted) {
                if (first)
                    first = false;
                else
                    result += ", ";

                result += ast.getText();
            }

            result += "\n";
        }

        return result;
    }

    private FrameTrackingUtil get(String fileName) throws Exception {
        final FrameTrackingUtil result = new FrameTrackingUtil();
        final DetailAST rootAST = parseFile(new File(getPath(fileName)), JavaParser.Options.WITH_COMMENTS);

        result.reset(rootAST);

        return result;
    }

    /**
     * Reads the contents of a file.
     * 
     * @param filename
     *            the name of the file whose contents are to be read
     * @return contents of the file with all {@code \r\n} replaced by {@code \n}
     * @throws IOException
     *             if I/O exception occurs while reading
     */
    protected static String readFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8)
                .replaceAll(CRLF_REGEX, LF_REGEX);
    }

    /**
     * Parse a file and return the parse tree.
     * 
     * @param file
     *            the file to parse.
     * @param withComments
     *            true to include comment nodes to the tree
     * @return the root node of the parse tree.
     * @throws IOException
     *             if the file could not be read.
     * @throws CheckstyleException
     *             if the file is not a Java source.
     */
    private static DetailAST parseFile(File file, JavaParser.Options withComments) throws IOException,
            CheckstyleException {
        final FileText text = new FileText(file.getAbsoluteFile(), System.getProperty(
                "file.encoding", StandardCharsets.UTF_8.name()));
        return parseFileText(text, withComments);
    }

    /**
     * Parse a text and return the parse tree.
     * 
     * @param text
     *            the text to parse.
     * @param withComments
     *            true to include comment nodes to the tree
     * @return the root node of the parse tree.
     * @throws CheckstyleException
     *             if the file is not a Java source.
     */
    private static DetailAST parseFileText(FileText text, JavaParser.Options withComments)
            throws CheckstyleException {
        final FileContents contents = new FileContents(text);
        DetailAST result;
        try {
            result = JavaParser.parse(contents);

            if (withComments == JavaParser.Options.WITH_COMMENTS) {
                result = JavaParser.appendHiddenCommentNodes(result);
            }
        } catch (CheckstyleException ex) {
            final String exceptionMsg = String.format(Locale.ROOT,
                    "%s occurred during the analysis of file %s.", ex.getClass().getSimpleName(),
                    text.getFile().getPath());
            throw new CheckstyleException(exceptionMsg, ex);
        }

        return result;
    }
}
