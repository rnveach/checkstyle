package org.checkstyle.suppressionxpathfilter.genericwhitespace;

import java.util.Collections;

public class InputXpathRegressionProcessStartOne {
    public<E> void bad() {//warn
    }
    public <E> void good() {
    }
}
