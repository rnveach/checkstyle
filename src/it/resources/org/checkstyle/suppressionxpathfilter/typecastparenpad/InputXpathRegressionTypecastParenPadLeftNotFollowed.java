package org.checkstyle.suppressionxpathfilter.typecastparenpad;

public class InputXpathRegressionTypecastParenPadLeftNotFollowed {
    Object bad = (Object )null;//warn
    Object good = ( Object )null;
}
