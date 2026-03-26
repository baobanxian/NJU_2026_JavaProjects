package edu.nju.software.ast;

import java.util.Objects;

public class LInteger extends LObject {
    public final int value;

    public LInteger(int val) {
        value = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LInteger)) return false;
        LInteger lInteger = (LInteger) o;
        return value == lInteger.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public String toString() {
        return Integer.toString(value);
    }
}