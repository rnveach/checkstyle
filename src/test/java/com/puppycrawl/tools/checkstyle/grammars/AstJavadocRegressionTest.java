package com.puppycrawl.tools.checkstyle.grammars;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;

public class AstJavadocRegressionTest extends BaseCheckTestSupport {
    @Override
    protected String getPath(String filename) throws IOException {
        return super.getPath("grammars" + File.separator + filename);
    }

    @Override
    protected String getNonCompilablePath(String filename) throws IOException {
        return super.getNonCompilablePath("grammars" + File.separator + filename);
    }

    @Test
    public void testEmptyTree() throws Exception {
        verifyNode(getPath("InputRegressionEmptyNode.txt"), getPath("InputRegressionEmpty.javadoc"));
    }

    @Test
    public void testTextTree() throws Exception {
        verifyNode(getPath("InputRegressionTextNode.txt"), getPath("InputRegressionText.javadoc"));
    }

    @Test
    public void testHtmlTree() throws Exception {
        verifyNode(getPath("InputRegressionHtmlNode.txt"), getPath("InputRegressionHtml.javadoc"));
    }
}
