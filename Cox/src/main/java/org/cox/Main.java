package org.cox;

import org.cox.utils.Cox;

public class Main {
    public static void main(String[] args) {
        Object evaluate = Cox.evaluate("10 * (12 + 3) ;");
        System.out.println(evaluate);
    }
}