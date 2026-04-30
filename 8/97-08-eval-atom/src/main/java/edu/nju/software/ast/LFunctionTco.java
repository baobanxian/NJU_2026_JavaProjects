package edu.nju.software.ast;

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

    public Environment genEnv(LList args) {
        return new Environment(env, (LList) params, args);
    }
}
