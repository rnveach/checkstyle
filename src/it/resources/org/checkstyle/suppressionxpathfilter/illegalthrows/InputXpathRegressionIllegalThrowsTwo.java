package org.checkstyle.suppressionxpathfilter.illegalthrows;

public class InputXpathRegressionIllegalThrowsTwo {
    public void methodOne() throws NullPointerException
    {
    }

    public void methodTwo() throws java.lang.Error //warn
    {
    }
}
