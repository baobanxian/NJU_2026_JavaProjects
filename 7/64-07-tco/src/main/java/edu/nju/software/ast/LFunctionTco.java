package edu.nju.software.ast;

import java.util.ArrayList;

import edu.nju.software.eval.Environment;

public class LFunctionTco extends LObject {
    private final LObject params;
    private final LObject ast;
    private final Environment env;

    public LFunctionTco(LObject params, LObject ast, Environment env) {
        this.params = params;
        this.ast = ast;
        this.env = env;
    }

    public LObject getAst() {
        return ast;
    }

    public Environment getEnv() {
        return env;
    }

    public Environment genEnv(ArrayList<LObject> args) {
        Environment newEnv = new Environment(this.env);
        LList paramsList = (LList) this.params;
        for (int i = 0; i < paramsList.content.size(); i++) {
            LSymbol paramName = (LSymbol) paramsList.content.get(i);
            LObject argValue = args.get(i);
            newEnv.define(paramName, argValue);
        }

        return newEnv;
    }
}