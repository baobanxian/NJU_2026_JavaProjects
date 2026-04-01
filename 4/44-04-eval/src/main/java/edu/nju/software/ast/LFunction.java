package edu.nju.software.ast;

import java.util.function.Function;

public class LFunction extends LObject {
    public final int ary;
    public final Function<LList, LObject> function;

    public LFunction(int ary, Function<LList, LObject> function) {
        this.ary = ary;
        this.function = function;
    }

    public LObject call(LList l) {
        if (l.content.size() != ary + 1) throw new IllegalArgumentException();
        return function.apply(l);
    }
}
