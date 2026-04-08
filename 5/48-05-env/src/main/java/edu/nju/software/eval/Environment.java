package edu.nju.software.eval;

import edu.nju.software.ast.LObject;
import edu.nju.software.ast.LSymbol;

import java.util.HashMap;

public class Environment {
    private final Environment parent;
    private final HashMap<LSymbol, LObject> symbols = new HashMap<>();

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(LSymbol symbol, LObject value) {
        symbols.put(symbol, value);
    }

    public LObject lookup(LSymbol symbol) {
        if (symbols.containsKey(symbol)) return symbols.get(symbol);
        if (parent != null) return parent.lookup(symbol);
        throw new IllegalArgumentException("Invalid symbol: " + symbol);
    }
}
