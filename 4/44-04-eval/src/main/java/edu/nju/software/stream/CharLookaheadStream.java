package edu.nju.software.stream;

import java.util.Arrays;

public class CharLookaheadStream {
    private final char[] value;
    private int position;

    public CharLookaheadStream(String input) {
        this.value = Arrays.copyOf(input.toCharArray(), input.length());
        this.position = 0;
    }

    public char consume() {
        return value[position++];
    }

    public int lookahead(int p) {
        int index = position + p - 1;
        if (index >= value.length) return -1;
        return value[index];
    }

    public boolean eof() {
        return position >= value.length;
    }
}
