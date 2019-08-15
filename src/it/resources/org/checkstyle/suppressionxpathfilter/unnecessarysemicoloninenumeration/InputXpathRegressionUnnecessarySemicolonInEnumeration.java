package org.checkstyle.suppressionxpathfilter.unnecessarysemicoloninenumeration;

public class InputXpathRegressionUnnecessarySemicolonInEnumeration {
}

enum Good {
    One, Two
}

enum Bad {
    Third; //warn
}
