package edu.nju.software.ast;

import java.util.function.Function;

public class LFunction extends LObject {
    public final Function<LList, LObject> function;

    public LFunction(Function<LList, LObject> function) {
        this.function = function;
    }

    public LObject call(LList l) {
        return function.apply(l);
    }
}
