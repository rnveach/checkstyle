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
}
