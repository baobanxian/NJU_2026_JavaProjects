package edu.nju.software.token;

import java.util.Objects;

public class SymbolToken extends Token {
    public final String name;

    private SymbolToken(String name) {
        this.name = name;
    }

    public static SymbolToken create(String name) {
        return new SymbolToken(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SymbolToken)) return false;
        SymbolToken that = (SymbolToken) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
