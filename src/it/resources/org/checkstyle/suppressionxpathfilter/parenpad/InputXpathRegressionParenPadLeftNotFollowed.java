package org.checkstyle.suppressionxpathfilter.parenpad;

public class InputXpathRegressionParenPadLeftNotFollowed {
    void method() {
        if (false ) {//warn
        }
        if ( true ) {
        }
    }
}
