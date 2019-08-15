package org.checkstyle.suppressionxpathfilter.parenpad;

public class InputXpathRegressionParenPadRightPreceded {
    void method() {
        if (false ) {//warn
        }
        if (true) {
        }
    }
}
