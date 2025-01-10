package org.cox;

import org.cox.token.Token;
import org.cox.utils.Cox;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Token> tokens = Cox.readTokens("10 * (12 + 3) ;");
        tokens.forEach(System.out::println);
    }
}