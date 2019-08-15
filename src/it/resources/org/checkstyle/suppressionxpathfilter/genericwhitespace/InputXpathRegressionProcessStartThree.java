package org.checkstyle.suppressionxpathfilter.genericwhitespace;

import java.util.Collections;

public class InputXpathRegressionProcessStartThree {
    < E> void bad() {//warn
    }
    <E> void good() {
    }
}
