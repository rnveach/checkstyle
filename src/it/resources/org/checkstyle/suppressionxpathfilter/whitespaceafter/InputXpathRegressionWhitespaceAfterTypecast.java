package org.checkstyle.suppressionxpathfilter.whitespaceafter;

public class InputXpathRegressionWhitespaceAfterTypecast {
    Object bad = (Object)null; //warn
    Object good = (Object) null;
}
