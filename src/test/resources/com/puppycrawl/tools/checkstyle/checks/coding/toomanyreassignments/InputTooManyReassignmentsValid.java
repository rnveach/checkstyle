package com.puppycrawl.tools.checkstyle.checks.coding.toomanyreassignments;

public class InputTooManyReassignmentsValid {
    public void method1() {
    }

    public void method2() {
        int a;
        a = 0;
        int b = 0;
    }

    int field;

    public void method3() {
        int field;
        field = 1;
        field = 2;
        this.field = 1;
        this.field = 2;
    }

    public void method4() {
        {
            int a = 0;
            a = 1;
            a = 2;
        }
        {
            int a = 0;
            a = 1;
            a = 2;
        }
    }
}
