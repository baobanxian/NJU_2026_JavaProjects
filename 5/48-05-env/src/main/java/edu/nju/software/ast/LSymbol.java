package edu.nju.software.ast;

import java.util.Objects;

public class LSymbol extends LObject {
    public final String name;

    public LSymbol(String val) {
        name = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LSymbol)) return false;
        LSymbol lSymbol = (LSymbol) o;
        return Objects.equals(name, lSymbol.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}