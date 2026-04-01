package edu.nju.software.token;

import java.util.Objects;

public class IntegerToken extends Token {
    public final int value;

    private IntegerToken(int value) {
        this.value = value;
    }

    public static IntegerToken create(int value) {
        return new IntegerToken(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerToken)) return false;
        IntegerToken that = (IntegerToken) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
