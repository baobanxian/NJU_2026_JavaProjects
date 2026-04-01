package edu.nju.software.token;

public class RightParen extends Token {
    private static final RightParen INSTANCE = new RightParen();

    private RightParen() {}

    public static RightParen create() {
        return INSTANCE;
    }
}
