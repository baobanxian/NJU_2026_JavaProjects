package edu.nju.software.stream;

import edu.nju.software.token.Token;

import java.util.ArrayList;

public class TokenReadStream {
    private final ArrayList<Token> value;
    private Integer position;

    public TokenReadStream(ArrayList<Token> val) {
        value = val;
        position = 0;
    }

    public Token next() {
        return value.get(position++);
    }

    public Token peek() {
        return value.get(position);
    }
}