package edu.nju.software.stream;

import edu.nju.software.token.Token;

import java.util.ArrayList;

public class TokenReadStream {
    private final ArrayList<Token> value;
    private int position;

    public TokenReadStream(ArrayList<Token> tokens) {
        this.value = tokens;
        this.position = 0;
    }

    public Token next() {
        return value.get(position++);
    }

    public Token peek() {
        if (position >= value.size()) return null;
        return value.get(position);
    }
}
