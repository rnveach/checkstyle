package org.checkstyle.suppressionxpathfilter.nestedtrydepth;

public class InputXpathRegressionNestedTryDepth {
    public void test() {
        try {
            try {
                try { //warn

                } catch (Exception e) {}
            } catch (Exception e) {}
        } catch (Exception e) {}
    }
}
