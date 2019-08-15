package org.checkstyle.suppressionxpathfilter.explicitinitialization;

public class InputXpathRegressionExplicitTwo {
    private int a;

    private Object bar = null; //warn
}
