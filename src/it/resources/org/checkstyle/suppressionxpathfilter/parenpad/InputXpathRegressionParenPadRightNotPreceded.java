package org.checkstyle.suppressionxpathfilter.parenpad;

public class InputXpathRegressionParenPadRightNotPreceded{
    void method() {
        if ( false) {//warn
        }
        if ( true ) {
        }
    }
}
