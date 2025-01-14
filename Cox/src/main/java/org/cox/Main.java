package org.cox;

import org.cox.utils.Cox;

import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("code.txt")){
            Cox.run(new String(inputStream.readAllBytes()));
        }
    }
}