package org.cox;

import org.cox.utils.Cox;

public class Main {
    public static void main(String[] args) {
        Cox.run("let a; let b; a = b = 10 ;print a;");
    }
}