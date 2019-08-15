package org.checkstyle.suppressionxpathfilter.npathcomplexity;

public class InputXpathRegressionNPathComplexityOne {
    public void test() { //warn
        while (true) {
            if (1 > 0) {

            } else {

            }
        }
    }
}
