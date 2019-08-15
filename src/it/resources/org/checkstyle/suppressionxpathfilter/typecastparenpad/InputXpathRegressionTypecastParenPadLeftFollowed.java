package org.checkstyle.suppressionxpathfilter.typecastparenpad;

public class InputXpathRegressionTypecastParenPadLeftFollowed {
    Object bad = ( Object)null;//warn
    Object good = (Object)null;
}
