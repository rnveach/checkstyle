/*
NPathComplexity
max = 20


*/

package com.puppycrawl.tools.checkstyle.checks.metrics.npathcomplexity;

public class InputNPathComplexityCheckCount{
    public void method() { // violation 'NPath Complexity is 30 (max allowed is 20)'
        try {}
        catch (IllegalArgumentException ex) {}
        try {}
        catch (IllegalArgumentException ex) {}
        try {}
        catch (IllegalArgumentException ex) {}
        try {}
        catch (IllegalArgumentException ex) {}
    }

    int method2() throws InterruptedException {
        // violation above 'NPath Complexity is 72 (max allowed is 20)'
        int x = 1;
        int a = 2;
        while (true) {
            try {
                if (x > 0) {
                    break;
                } else if (x < 0) {
                    ;
                } else {
                    break;
                }
                switch (a)
                {
                case 0:
                    break;
                default:
                    break;
                }
            }
            catch (Exception e)
            {
                break;
            }
        }

        synchronized (this) {
            do {
                x = 2;
            } while (x == 2);
        }

        this.wait(666);

        for (int k = 0; k < 1; k++) {
            String innerBlockVariable = "";
        }

        if (System.currentTimeMillis() > 1000)
            return 1;
        else
            return 2;
    }
}
