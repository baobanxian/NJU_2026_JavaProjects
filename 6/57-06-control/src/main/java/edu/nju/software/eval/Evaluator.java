package edu.nju.software.eval;

import edu.nju.software.ast.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.experimental.runners.Enclosed;

/**
 * Project 06 — 控制流
 *
 * 你需要实现：
 *   1. eval(LObject, Environment) 核心方法（含 let/let* 已在 Project05 实现，这里需重新实现）
 *   2. handleDo — do 顺序执行
 *   3. handleIF — if 条件求值
 *   4. handleFnStar — fn* 函数闭包
 *   5. 扩展1：handleCond — cond 多分支条件
 *   6. 扩展2：handleWhen — when 条件执行
 */
public class Evaluator {
    private static final Environment GLOBALS = new Environment();
    private static final LSymbol SYMBOL_LET = new LSymbol("let");
    private static final LSymbol SYMBOL_LET_STAR = new LSymbol("let*");
    private static final LSymbol SYMBOL_DO = new LSymbol("do");
    private static final LSymbol SYMBOL_IF = new LSymbol("if");
    private static final LSymbol SYMBOL_FN_STAR = new LSymbol("fn*");
    private static final LSymbol SYMBOL_COND = new LSymbol("cond");
    private static final LSymbol SYMBOL_WHEN = new LSymbol("when");
    private static final LSymbol SYMBOL_ELSE = new LSymbol("else");

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
    }

    // ── 已提供：handleLet（与 Project05 相同）─────────────────
    private static LObject handleLet(LList list, Environment env, boolean inplace) {
        final LList bindings = (LList) list.content.get(1);
        final Environment newEnv = new Environment(env);
        for (LObject bindingObj : bindings.content) {
            LList binding = (LList) bindingObj;
            newEnv.define((LSymbol) binding.content.get(0),
                    eval(binding.content.get(1), inplace ? newEnv : env));
        }
        return eval(list.content.get(2), newEnv);
    }

    /**
     * 处理 do 语句：(do expr1 expr2 ... exprN)
     * 依次执行所有表达式，返回最后一个的结果。
     *
     * 示例：(do (* 1 2) (- 1 2) (+ 1 2)) → 3
     */
    private static LObject handleDo(LList list, Environment env) {
        // TODO 1: 实现 handleDo
        // 提示：
        //   从 i=1 到 size-2 执行 eval(list.content.get(i), env)（结果丢弃）
        //   返回 eval(list.content.get(size-1), env)
        int size = list.content.size();
        for (int i = 1; i < size - 1; i++) {
            eval(list.content.get(i), env);
        }
        return eval(list.content.get(size - 1),env);
    }

    /**
     * 处理 if 语句：(if cond then else)
     * 先求值 cond，若结果为 LList() 或 LInteger(0)，执行 else 分支，否则执行 then 分支。
     *
     * 示例：(if 0 1 2) → 2     (if 1 10 20) → 10
     */
    private static LObject handleIF(LList list, Environment env) {
        // TODO 2: 实现 handleIF
        // 提示：
        //   LObject cond = eval(list.content.get(1), env);
        //   判断 cond 是否为"假"：cond.equals(new LList()) || cond.equals(new LInteger(0))
        //   假 → eval(list.content.get(3), env)
        //   真 → eval(list.content.get(2), env)
        LObject cond = eval(list.content.get(1), env);
        if (cond.equals(new LList()) || cond.equals(new LInteger(0))) {
            return eval(list.content.get(3), env);
        } else {
            return eval(list.content.get(2), env);
        }
        
    }

    /**
     * 处理 fn* 语句：(fn* (params...) body)
     * 创建函数闭包，捕获当前环境。
     *
     * 示例：((fn* (a) a) 123) → 123
     *       ((fn* (a b) (+ a b)) 3 4) → 7
     *
     * 注意：必须对当前环境进行 deepClone，否则闭包可能引用到后续被修改的环境。
     */
    private static LFunction handleFnStar(LList list, Environment env) {
        // TODO 3: 实现 handleFnStar
        // 提示：
        //   LList params = (LList) list.content.get(1);   // 参数列表 (a b c)
        //   LObject body = list.content.get(2);            // 函数体
        //   Environment captured = env.deepClone();         // 深拷贝环境！
        //   return new LFunction(params.content.size(),
        //       args -> eval(body, new Environment(captured, params, args)));
        LList params = (LList) list.content.get(1);
        LObject body = list.content.get(2);
        Environment captured = env.deepClone();
        return new LFunction(params.content.size(), args -> eval(body, new Environment(captured,params,args)));
    }

    /**
     * 扩展1：处理 cond 多分支条件。
     *
     * 形式：(cond (test1 expr1) (test2 expr2) ... (else exprN))
     *   - 依次求值每个 test，遇到第一个为"真"的，执行并返回对应 expr
     *   - 如果 test 是 else 符号，则无条件执行对应 expr
     *   - 所有 test 都为假且无 else → 返回 LList()
     *
     * 示例：
     *   (cond (0 10) (1 20) (else 30)) → 20
     *   (cond (0 10) (0 20) (else 30)) → 30
     *   (cond (0 10) (0 20))           → ()
     */
    private static LObject handleCond(LList list, Environment env) {
        // TODO 4: 实现 handleCond
        // 提示：
        //   从 i=1 遍历 list.content（每个元素是 LList 形如 (test expr)）
        //   LList clause = (LList) list.content.get(i);
        //   LObject test = clause.content.get(0);
        //   如果 test 是 SYMBOL_ELSE → 直接执行 clause.content.get(1)
        //   否则求值 test，判断是否为"真"（非 LList() 且非 LInteger(0)）
        //   真 → 执行并返回 clause.content.get(1)
        //   遍历完无匹配 → return new LList()
        for (int i = 1; i < list.content.size(); i++) {
            LList clause = (LList) list.content.get(i);
            LObject test = clause.content.get(0);
            LObject expr = clause.content.get(1);
            boolean isTrue = false;
            if (test.equals(SYMBOL_ELSE)) {
                isTrue = true;
            } else {
                LObject testResult = eval(test, env);
                if (!(testResult instanceof LList && ((LList) testResult).content.isEmpty()) &&
                        !(testResult instanceof LInteger && ((LInteger) testResult).value == 0)) {
                    isTrue = true;
                }
            }
            if (isTrue) {
                return eval(expr, env);
            }
        }
        return new LList();
    }

    /**
     * 扩展2：处理 when 条件执行。
     *
     * 形式：(when test expr1 expr2 ... exprN)
     *   - 先求值 test
     *   - 若为"真"，依次执行 expr1 到 exprN，返回最后一个结果
     *   - 若为"假"，返回 LList()（不执行任何表达式）
     *
     * 示例：
     *   (when 1 (+ 1 2) (* 3 4))  → 12
     *   (when 0 (+ 1 2) (* 3 4))  → ()
     */
    private static LObject handleWhen(LList list, Environment env) {
        // TODO 5: 实现 handleWhen
        // 提示：
        //   LObject cond = eval(list.content.get(1), env);
        //   判断是否为假（同 handleIF 的判断逻辑）
        //   假 → return new LList()
        //   真 → 从 i=2 到 size-1 逐个 eval，返回最后一个结果
        LObject cond = eval(list.content.get(1), env);
        if (cond.equals(new LList()) || cond.equals(new LInteger(0))) {
            return new LList();
        } else {
            for (int i = 2; i < list.content.size(); i++) {
                eval(list.content.get(list.content.size() - 1), env);
            }
            return eval(list.content.get(list.content.size() - 1));
        }
    }

    /**
     * 核心求值方法。
     */
    private static LObject eval(LObject obj, Environment env) {
        // TODO 6: 实现 eval
        // 提示：结构与 Project05 相同，新增 do/if/fn*/cond/when 分支
        //   if (obj instanceof LSymbol) → env.lookup
        //   else if (obj instanceof LList) →
        //     空列表 → return obj
        //     let    → handleLet(list, env, false)
        //     let*   → handleLet(list, env, true)
        //     do     → handleDo(list, env)
        //     if     → handleIF(list, env)
        //     fn*    → handleFnStar(list, env)
        //     cond   → handleCond(list, env)       // 扩展1
        //     when   → handleWhen(list, env)       // 扩展2
        //     其他   → eval each element, call function
        //   else → return obj
        if (obj instanceof LSymbol) {
            return env.lookup((LSymbol) obj);
        } else if (obj instanceof LList) {
            LList list = (LList) obj;

            if (list.content.isEmpty()) {
                return list;
            }

            LObject first = list.content.get(0);

            if (first instanceof LSymbol) {
                LSymbol sym = (LSymbol) first;
                if (sym.name.equals(SYMBOL_LET.name)) {
                    return handleLet(list, env, false);
                } else if (sym.name.equals(SYMBOL_LET_STAR.name)) {
                    return handleLet(list, env, true);
                } else if (sym.name.equals(SYMBOL_DO.name)) {
                    return handleDo(list, env);
                } else if (sym.name.equals(SYMBOL_IF.name)) {
                    return handleIF(list, env);
                } else if (sym.name.equals(SYMBOL_FN_STAR.name)) {
                    return handleFnStar(list, env);
                } else if (sym.name.equals(SYMBOL_COND.name)) {
                    return handleCond(list, env);
                } else if (sym.name.equals(SYMBOL_WHEN.name)) {
                    return handleWhen(list, env);
                }
            }

            LObject func = eval(first, env);

            if (!(func instanceof LFunction)) {
                throw new RuntimeException("Calling non-function: " + func);
            }

            ArrayList<LObject> argsList = new ArrayList<>();
            argsList.add(first); // 占位符，匹配 list.content.get(1) 获取第一个参数
            for (int i = 1; i < list.content.size(); i++) {
                argsList.add(eval(list.content.get(i), env));
            }
            return ((LFunction) func).call(new LList(argsList));
        }

        return obj;
        
    }

    public static LObject eval(LObject obj) {
        return eval(obj, GLOBALS);
    }
}