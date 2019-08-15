package org.checkstyle.suppressionxpathfilter.parenpad;

public class InputXpathRegressionParenPadLeftFollowed {
    void method() {
        if ( false) {//warn
        }
        if (true) {
        }
    }
}
