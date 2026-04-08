package edu.nju.software.eval;

import edu.nju.software.ast.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Project 05 — 环境与绑定
 *
 * 你需要实现：
 * 1. eval(LObject, Environment) 核心方法
 * 2. let 普通绑定
 * 3. let* 特殊绑定
 * 4. 扩展1：if 条件求值
 * 5. 扩展2：begin 顺序求值
 */
public class Evaluator {
    private static final Environment GLOBALS = new Environment();
    private static final LSymbol SYMBOL_LET = new LSymbol("let");
    private static final LSymbol SYMBOL_LET_STAR = new LSymbol("let*");
    private static final LSymbol SYMBOL_IF = new LSymbol("if");
    private static final LSymbol SYMBOL_BEGIN = new LSymbol("begin");

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

    /**
     * 在给定环境下对 AST 节点求值。
     *
     * 规则：
     * 1. LInteger / LString → 返回自身
     * 2. LSymbol → 在环境中查找 env.lookup(symbol)
     * 3. LList（空）→ 返回自身
     * 4. LList 第一个元素为 let → 处理普通绑定
     * 5. LList 第一个元素为 let* → 处理特殊绑定
     * 6. LList 第一个元素为 if → 处理条件求值（扩展1）
     * 7. LList 第一个元素为 begin → 处理顺序求值（扩展2）
     * 8. LList（其他）→ 对每个元素求值，第一个作为函数调用
     */
    private static LObject eval(LObject obj, Environment env) {
        // TODO: 实现 eval 方法
        //
        // 提示（伪代码）：
        // if (obj instanceof LSymbol)
        // return env.lookup((LSymbol) obj);
        // else if (obj instanceof LList)
        // LList list = (LList) obj;
        // if (list.content.isEmpty()) return obj;
        // else if (第一个元素是 let) → 调用 handleLet(list, env, false)
        // else if (第一个元素是 let*) → 调用 handleLet(list, env, true)
        // else if (第一个元素是 if) → 调用 handleIf(list, env) // 扩展1
        // else if (第一个元素是 begin) → 调用 handleBegin(list, env) // 扩展2
        // else → 对每个元素 eval，第一个作为 LFunction 调用 call
        // else
        // return obj;
        if (obj instanceof LSymbol)
            return env.lookup((LSymbol) obj);
        else if (obj instanceof LList) {
            LList list = (LList) obj;
            if (list.content.isEmpty())
                return obj;
            else if (list.content.get(0).equals(SYMBOL_LET)) {
                return handleLet(list, env, false);
            } else if (list.content.get(0).equals(SYMBOL_LET_STAR)) {
                return handleLet(list, env, true);
            } else if (list.content.get(0).equals(SYMBOL_IF)) {
                return handleIf(list, env);
            } else if (list.content.get(0).equals(SYMBOL_BEGIN)) {
                return handleBegin(list, env);
            } else {
                ArrayList<LObject> evaledList = list.content.stream()
                        .map(item -> eval(item, env))
                        .collect(Collectors.toCollection(ArrayList::new));
                LFunction func = (LFunction) evaledList.get(0);
                return func.call(new LList(evaledList));
            }
        }
        return obj;
    }

    /**
     * 处理 let / let* 绑定。
     *
     * 形式：(let ((var1 prog1) (var2 prog2) ...) body)
     *
     * @param list    完整的 let 表达式
     * @param env     当前环境
     * @param inplace true=let*（在新环境中求值绑定），false=let（在旧环境中求值绑定）
     */
    private static LObject handleLet(LList list, Environment env, boolean inplace) {
        // TODO: 实现 handleLet
        //
        // 提示：
        // 1. LList bindings = (LList) list.content.get(1); // 绑定列表
        // 2. Environment newEnv = new Environment(env); // 创建子环境
        // 3. 遍历 bindings.content 中每个 binding（也是 LList）：
        // - LSymbol name = (LSymbol) binding.content.get(0);
        // - LObject value = eval(binding.content.get(1), inplace ? newEnv : env);
        // ↑ let 用 env 求值，let* 用 newEnv 求值（这是两者的唯一区别！）
        // - newEnv.define(name, value);
        // 4. return eval(list.content.get(2), newEnv); // 在新环境中执行 body
        LList bindings = (LList) list.content.get(1);
        Environment newEnv = new Environment(env);
        for (LObject binding : bindings.content) {
            LList bindingList = (LList) binding;
            LSymbol name = (LSymbol) bindingList.content.get(0);
            LObject value = eval(bindingList.content.get(1), inplace ? newEnv : env);
            newEnv.define(name, value);
        }
        return eval(list.content.get(2), newEnv);
    }

    /**
     * 扩展1：处理 if 条件求值。
     *
     * 形式：(if cond then else)
     * - 先在当前环境下求值 cond
     * - 如果结果不是空列表（即不是"假"），求值并返回 then
     * - 否则，求值并返回 else
     *
     * 示例：
     * (if (cons 1 nil) 10 20) → 10 （非空列表 = 真）
     * (if nil 10 20) → 20 （nil = 空列表 = 假）
     */
    private static LObject handleIf(LList list, Environment env) {
        // TODO: 实现 handleIf
        //
        // 提示：
        // LObject cond = eval(list.content.get(1), env);
        // boolean isFalse = (cond instanceof LList) && ((LList)
        // cond).content.isEmpty();
        // if (!isFalse) return eval(list.content.get(2), env); // then 分支
        // else return eval(list.content.get(3), env); // else 分支
        LObject cond = eval(list.content.get(1), env);
        boolean isFalse = (cond instanceof LList) && ((LList) cond).content.isEmpty();
        if (!isFalse)
            return eval(list.content.get(2), env);
        else
            return eval(list.content.get(3), env);
    }

    /**
     * 扩展2：处理 begin 顺序求值。
     *
     * 形式：(begin expr1 expr2 ... exprN)
     * - 在当前环境下依次求值每个表达式
     * - 返回最后一个表达式的结果
     *
     * 示例：
     * (begin 1 2 3) → 3
     * (begin (+ 1 2) (* 3 4)) → 12
     */
    private static LObject handleBegin(LList list, Environment env) {
        // TODO: 实现 handleBegin
        //
        // 提示：
        // LObject result = new LList(); // 默认空
        // for (int i = 1; i < list.content.size(); i++) {
        // result = eval(list.content.get(i), env);
        // }
        // return result;
        LObject result = new LList();
        for (int i = 1; i < list.content.size(); i++) {
            result = eval(list.content.get(i), env);
        }
        return result;
    }

    public static LObject eval(LObject obj) {
        return eval(obj, GLOBALS);
    }
}
