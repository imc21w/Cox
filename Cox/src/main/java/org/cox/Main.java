package org.cox;

import org.cox.utils.Cox;

public class Main {
    public static void main(String[] args) {
        Cox.run("print 10 * (12 + 3) ; print 10 / 3 + 1; print 5.00 == 5.0000000000001;");
    }
}