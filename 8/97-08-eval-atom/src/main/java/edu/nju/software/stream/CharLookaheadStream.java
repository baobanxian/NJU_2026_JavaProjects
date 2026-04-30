package edu.nju.software.stream;

public class CharLookaheadStream {
    private final char[] array;
    private int pos = 0;

    public CharLookaheadStream(char[] array) {
        this.array = new char[array.length];
        System.arraycopy(array, 0, this.array, 0, array.length);
    }

    public CharLookaheadStream(String str) {
        this(str.toCharArray());
    }

    private void boundCheck(int target) {
        if (target >= array.length) throw new IndexOutOfBoundsException();
    }

    public char consume() {
        boundCheck(pos);
        return array[pos++];
    }

    public char lookahead(int p) {
        if (p < 1) throw new IllegalArgumentException();
        int aheadPos = pos + p - 1;
        boundCheck(aheadPos);
        return array[aheadPos];
    }

    public boolean eof() {
        return pos == array.length;
    }
}
