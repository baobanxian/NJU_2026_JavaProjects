package edu.nju.software.token;

import java.util.Objects;

public class StringToken extends Token {
    public final String value;

    private StringToken(String value) {
        this.value = value;
    }

    public static StringToken create(String value) {
        return new StringToken(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringToken)) return false;
        StringToken that = (StringToken) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}