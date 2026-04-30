package edu.nju.software.eval;

import edu.nju.software.ast.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Project 07 — 尾递归优化（TCO）
 *
 * 你需要实现：
 * 1. eval 方法 — 核心求值（带 while(true) 循环实现 TCO）
 * 2. handleDef — def 具名函数定义
 * 3. handleLetStar — let* 绑定（TCO 版：返回 AST 而非 eval）
 * 4. handleDo — do 顺序执行（TCO 版：返回最后一个 AST 而非 eval）
 * 5. handleIF — if 条件（TCO 版：返回分支 AST 而非 eval）
 * 6. handleFnStar — fn* 闭包创建（使用 LFunctionTco）
 * 7. 扩展1：= 和 < 比较函数（添加到 GLOBALS）
 * 8. 扩展2：defn 语法糖
 */
public class Evaluator {
    private static final Environment GLOBALS = new Environment();
    private static final LSymbol SYMBOL_LET = new LSymbol("let");
    private static final LSymbol SYMBOL_LET_STAR = new LSymbol("let*");
    private static final LSymbol SYMBOL_DO = new LSymbol("do");
    private static final LSymbol SYMBOL_IF = new LSymbol("if");
    private static final LSymbol SYMBOL_FN_STAR = new LSymbol("fn*");
    private static final LSymbol SYMBOL_DEF = new LSymbol("def");
    private static final LSymbol SYMBOL_DEFN = new LSymbol("defn");

    static {
        GLOBALS.define(new LSymbol("+"), new LFunction(2,
                list -> new LInteger(((LInteger) list.content.get(1)).value + ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("-"), new LFunction(2,
                list -> new LInteger(((LInteger) list.content.get(1)).value - ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("*"), new LFunction(2,
                list -> new LInteger(((LInteger) list.content.get(1)).value * ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("/"), new LFunction(2,
                list -> new LInteger(((LInteger) list.content.get(1)).value / ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("nil"), new LList());
        GLOBALS.define(new LSymbol("cons"), new LFunction(2, list -> {
            LObject toAdd = list.content.get(1);
            ArrayList<LObject> newList = new ArrayList<>();
            newList.add(toAdd);
            newList.addAll(((LList) list.content.get(2)).content);
            return new LList(newList);
        }));

        // 扩展1 — TODO 7: 添加 = 比较函数
        // 接受两个 LInteger，相等返回 LInteger(1)，否则返回 LInteger(0)
        // 提示：参照 + 的写法，用三元运算符 a == b ? 1 : 0
        GLOBALS.define(new LSymbol("="), new LFunction(2, list -> {
            int v1 = ((LInteger) list.content.get(1)).value;
            int v2 = ((LInteger) list.content.get(2)).value;
            return new LInteger(v1 == v2 ? 1 : 0);
        }));

        // 扩展1 — TODO 7: 添加 < 比较函数
        // 接受两个 LInteger，第一个小于第二个返回 LInteger(1)，否则返回 LInteger(0)
        GLOBALS.define(new LSymbol("<"), new LFunction(2, list -> {
            int v1 = ((LInteger) list.content.get(1)).value;
            int v2 = ((LInteger) list.content.get(2)).value;
            return new LInteger(v1 < v2 ? 1 : 0);
        }));
    }

    // ── 已提供：handleLet（普通 let，不做 TCO）───────────────
    private static LObject handleLet(LList list, Environment env) {
        final LList bindings = (LList) list.content.get(1);
        final Environment newEnv = new Environment(env);
        for (LObject bindingObj : bindings.content) {
            LList binding = (LList) bindingObj;
            newEnv.define((LSymbol) binding.content.get(0), eval(binding.content.get(1), env));
        }
        return eval(list.content.get(2), newEnv);
    }

    /**
     * let* 绑定（TCO 版）。
     * 与普通 let* 不同：不 eval body，而是返回 body 的 AST，
     * 由外层 while(true) 继续迭代求值。
     *
     * 注意：let* 的绑定直接在当前 env 中 define（不创建新环境）。
     */
    private static LObject handleLetStar(LList list, Environment env) {
        // TODO 1: 实现 handleLetStar（TCO 版）
        // 提示：
        // 遍历 bindings，在 env 中 define
        // return list.content.get(2); ← 返回 AST 而不是 eval(...)
        final LList bindings = (LList) list.content.get(1);
        for (LObject bindingObj : bindings.content) {
            LList binding = (LList) bindingObj;
            env.define((LSymbol) binding.content.get(0), eval(binding.content.get(1), env));
        }
        return list.content.get(2);
    }

    /**
     * do 顺序执行（TCO 版）。
     * 执行所有中间表达式，但最后一个不 eval，返回其 AST。
     */
    private static LObject handleDo(LList list, Environment env) {
        // TODO 2: 实现 handleDo（TCO 版）
        // 提示：
        // for i=1 to size-2: eval(list.content.get(i), env) ← 执行并丢弃
        // return list.content.get(size-1); ← 返回最后一个的 AST
        for (int i = 1; i < list.content.size() - 1; i++) {
            eval(list.content.get(i), env);
        }
        return list.content.get(list.content.size() - 1);
    }

    /**
     * if 条件（TCO 版）。
     * 求值条件，但不 eval 分支，返回分支的 AST。
     */
    private static LObject handleIF(LList list, Environment env) {
        // TODO 3: 实现 handleIF（TCO 版）
        // 提示：
        // eval 条件 list.content.get(1)
        // 假 → return list.content.get(3) ← AST
        // 真 → return list.content.get(2) ← AST
        LObject conditionResult = eval(list.content.get(1), env);
        boolean isTruthy = true;
        if (conditionResult instanceof LInteger) {
            if (((LInteger) conditionResult).value == 0) {
                isTruthy = false;
            }
        } else if (conditionResult instanceof LList) {
            if (((LList) conditionResult).content.isEmpty()) {
                isTruthy = false;
            }
        }
        if (isTruthy) {
            return list.content.get(2);
        } else {
            return list.content.size() > 3 ? list.content.get(3) : new LList();
        }
    }

    /**
     * fn* 闭包创建。
     * 返回 LFunctionTco（而非 LFunction），存储 params/ast/env。
     */
    private static LFunctionTco handleFnStar(LList list, Environment env) {
        // TODO 4: 实现 handleFnStar
        // 提示：
        LList params = (LList) list.content.get(1);
        LObject body = list.content.get(2);
        Environment captured = env.deepClone();
        return new LFunctionTco(params, body, captured);
    }

    /**
     * 核心求值方法（TCO 版）。
     *
     * 关键变化：eval 内部有 while(true) 循环。
     * 对于 let* / do / if，不递归调用 eval，而是修改 obj 然后 continue 循环。
     * 对于 LFunctionTco 调用，修改 obj 和 env 然后 continue。
     */
    private static LObject eval(LObject obj, Environment env) {
        // TODO 5: 实现 eval（TCO 版）
        //
        // 结构：
        // while (true) {
        // if (obj instanceof LSymbol) return env.lookup(...)
        // else if (obj instanceof LList) {
        // 空列表 → return obj
        // def → handleDef, return
        // defn → 扩展2: 转为 def + fn* // 扩展2
        // let → return handleLet(...) // let 不做 TCO
        // let* → obj = handleLetStar(...); continue
        // do → obj = handleDo(...); continue
        // if → obj = handleIF(...); continue
        // fn* → return handleFnStar(...)
        // 其他 → {
        // eval 第一个元素判断类型：
        // 如果是 LFunctionTco → eval 所有参数，设置 obj=f.getAst(), env=f.genEnv(args), continue
        // 如果是 LFunction → eval 所有元素，调用 call，return
        // }
        // } else return obj
        // }
        //
        // def 的实现：
        // LSymbol name = (LSymbol) list.content.get(1);
        // LFunctionTco f = handleFnStar((LList) list.content.get(2), GLOBALS);
        // env.define(name, f);
        // f.getEnv().define(name, f); // 让函数能调用自己（自引用）
        // return f;
        //
        // defn 的实现（扩展2）：
        // (defn name (params) body) 转为 (def name (fn* (params) body))
        // LSymbol name = (LSymbol) list.content.get(1);
        // LList params = (LList) list.content.get(2);
        // LObject body = list.content.get(3);
        // 构造 fnList = new LList(SYMBOL_FN_STAR, params, body)
        // 构造 defList = new LList(SYMBOL_DEF, name, fnList)
        // obj = defList; continue ← 重写为 def，下一轮循环处理
        while (true) {
            if (obj instanceof LSymbol) {
                return env.lookup((LSymbol) obj);
            } else if (obj instanceof LList) {
                LList list = (LList) obj;
                if (list.content.isEmpty())
                    return list;

                LObject head = list.content.get(0);

                if (SYMBOL_DEF.equals(head)) {
                    LSymbol name = (LSymbol) list.content.get(1);
                    LFunctionTco f = handleFnStar((LList) list.content.get(2), GLOBALS);
                    env.define(name, f);
                    f.getEnv().define(name, f);
                    return f;
                }

                if (SYMBOL_DEFN.equals(head)) {
                    LSymbol name = (LSymbol) list.content.get(1);
                    LList params = (LList) list.content.get(2);
                    LObject body = list.content.get(3);
                    ArrayList<LObject> fnParts = new ArrayList<>();
                    fnParts.add(SYMBOL_FN_STAR);
                    fnParts.add(params);
                    fnParts.add(body);
                    LList fnList = new LList(fnParts);
                    ArrayList<LObject> defParts = new ArrayList<>();
                    defParts.add(SYMBOL_DEF);
                    defParts.add(name);
                    defParts.add(fnList);
                    obj = new LList(defParts);
                    continue;
                }

                if (SYMBOL_LET.equals(head)) {
                    return handleLet(list, env);
                }

                if (SYMBOL_LET_STAR.equals(head)) {
                    obj = handleLetStar(list, env);
                    continue;
                }

                if (SYMBOL_DO.equals(head)) {
                    obj = handleDo(list, env);
                    continue;
                }

                if (SYMBOL_IF.equals(head)) {
                    obj = handleIF(list, env);
                    continue;
                }

                if (SYMBOL_FN_STAR.equals(head)) {
                    return handleFnStar(list, env);
                }

                LObject func = eval(head, env);
                if (func instanceof LFunctionTco) {
                    LFunctionTco f = (LFunctionTco) func;
                    ArrayList<LObject> args = new ArrayList<>();
                    for (int i = 1; i < list.content.size(); i++) {
                        args.add(eval(list.content.get(i), env));
                    }
                    env = f.genEnv(args);
                    obj = f.getAst();
                    continue;
                } else if (func instanceof LFunction) {
                    LFunction f = (LFunction) func;
                    ArrayList<LObject> evaluated = new ArrayList<>();
                    for (LObject item : list.content) {
                        evaluated.add(eval(item, env));
                    }
                    return f.call(new LList(evaluated));
                } else {
                    throw new RuntimeException("Invalid function call");
                }
            } else {
                return obj;
            }
        }
    }

    public static LObject eval(LObject obj) {
        return eval(obj, GLOBALS);
    }
}