package edu.nju.software.ast;

import java.util.Objects;

public class LAtom extends LObject {
    private LObject ref;

    public LAtom(LObject ref) {
        this.ref = ref;
    }

    public LObject getRef() {
        return ref;
    }

    public void setRef(LObject ref) {
        this.ref = ref;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LAtom)) return false;
        LAtom lAtom = (LAtom) o;
        return Objects.equals(ref, lAtom.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ref);
    }

    @Override
    public String toString() {
        return "LAtom(" + ref + ')';
    }
}
