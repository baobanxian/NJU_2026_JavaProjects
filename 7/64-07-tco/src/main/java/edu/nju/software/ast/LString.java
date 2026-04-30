package edu.nju.software.ast;

import java.util.Objects;

public class LString extends LObject {
    public final String value;

    public LString(String val) {
        value = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LString)) return false;
        LString lString = (LString) o;
        return Objects.equals(value, lString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}