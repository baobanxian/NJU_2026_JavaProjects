package edu.nju.software.token;

public class LeftParen extends Token {
    private static final LeftParen INSTANCE = new LeftParen();

    private LeftParen() {}

    public static LeftParen create() {
        return INSTANCE;
    }
}
