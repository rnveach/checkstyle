package org.checkstyle.suppressionxpathfilter.genericwhitespace;

import java.io.Serializable;

public class InputXpathRegressionProcessSingleGenericTwo {
    <E>void bad() {//warn
    }
    <E> void good() {
    }
}
