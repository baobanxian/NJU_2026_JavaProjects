package edu.nju.software.eval;

import edu.nju.software.ast.*;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Project 08 — Eval与Atom
 *
 * 你需要在 GLOBALS 中添加以下内建函数：
 * 1. read-string — 将字符串解析为 LObject
 * 2. eval — 将 LObject 作为代码在全局环境下执行
 * 3. atom — 创建可变引用
 * 4. atom? — 判断是否为 LAtom
 * 5. deref — 解引用，获取 atom 当前引用的对象
 * 6. reset! — 重置 atom 的引用
 * 7. swap! — 用函数更新 atom 的引用
 * 8. 扩展1：list — 从参数创建列表
 * 9. 扩展2：nth — 按下标取列表元素
 */
public class Evaluator {
    private static final Environment GLOBALS = new Environment();
    private static final LSymbol SYMBOL_LET = new LSymbol("let");
    private static final LSymbol SYMBOL_LET_STAR = new LSymbol("let*");
    private static final LSymbol SYMBOL_DO = new LSymbol("do");
    private static final LSymbol SYMBOL_IF = new LSymbol("if");
    private static final LSymbol SYMBOL_FN_STAR = new LSymbol("fn*");
    private static final LSymbol SYMBOL_DEF = new LSymbol("def");

    static {
        // ── 已提供：算术函数和 cons/nil ────────────────
        GLOBALS.define(new LSymbol("+"), new LFunction(
                list -> new LInteger(((LInteger) list.content.get(1)).value + ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("-"), new LFunction(
                list -> new LInteger(((LInteger) list.content.get(1)).value - ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("*"), new LFunction(
                list -> new LInteger(((LInteger) list.content.get(1)).value * ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("/"), new LFunction(
                list -> new LInteger(((LInteger) list.content.get(1)).value / ((LInteger) list.content.get(2)).value)));
        GLOBALS.define(new LSymbol("nil"), new LList());
        GLOBALS.define(new LSymbol("cons"), new LFunction(list -> {
            LObject toAdd = list.content.get(1);
            ArrayList<LObject> newList = new ArrayList<>();
            newList.add(toAdd);
            newList.addAll(((LList) list.content.get(2)).content);
            return new LList(newList);
        }));

        // ── TODO 1: read-string 函数 ────────────────
        // 接受一个 LString 参数，将其内容解析为 LObject
        // 提示：
        // String str = ((LString) list.content.get(1)).value;
        // return Parser.parse(Lexer.lex(new CharLookaheadStream(str)));
        GLOBALS.define(new LSymbol("read-string"), new LFunction(list -> {
            String str = ((LString) list.content.get(1)).value;
            return Parser.parse(Lexer.lex(new CharLookaheadStream(str)));
        }));

        // ── TODO 2: eval 函数 ────────────────────────
        // 接受一个参数，将其作为代码在全局环境下执行
        // 提示：return eval(list.content.get(1));
        GLOBALS.define(new LSymbol("eval"), new LFunction(list -> {
            return eval(list.content.get(1));
        }));

        // ── TODO 3: atom 函数 ────────────────────────
        // 接受一个参数，创建并返回一个新的 LAtom
        // 提示：return new LAtom(list.content.get(1));
        GLOBALS.define(new LSymbol("atom"), new LFunction(list -> {
            return new LAtom(list.content.get(1));
        }));

        // ── TODO 4: atom? 函数 ───────────────────────
        // 接受一个参数，判断是否为 LAtom，是返回 LInteger(1)，否返回 LInteger(0)
        GLOBALS.define(new LSymbol("atom?"), new LFunction(list -> {
            if (list.content.get(1) instanceof LAtom) {
                return new LInteger(1);
            } else {
                return new LInteger(0);
            }
        }));

        // ── TODO 5: deref 函数 ───────────────────────
        // 接受一个 LAtom 参数，返回其当前引用的对象
        // 提示：((LAtom) list.content.get(1)).getRef()
        GLOBALS.define(new LSymbol("deref"), new LFunction(list -> {
            return ((LAtom) list.content.get(1)).getRef();
        }));

        // ── TODO 6: reset! 函数 ──────────────────────
        // 接受一个 LAtom 和一个新值，将 atom 的引用设为新值，返回新值
        // 提示：atom.setRef(newRef); return newRef;
        GLOBALS.define(new LSymbol("reset!"), new LFunction(list -> {
            LAtom atom = (LAtom) list.content.get(1);
            LObject newRef = list.content.get(2);
            atom.setRef(newRef);
            return newRef;
        }));

        // ── TODO 7: swap! 函数 ──────────────────────
        // 接受一个 LAtom、一个函数、和若干额外参数
        // 用 atom 当前值作为第一个参数 + 额外参数调用函数
        // 将结果设为 atom 的新引用，并返回结果
        //
        // 提示：
        // LAtom atom = (LAtom) list.content.get(1);
        // LObject func = list.content.get(2);
        // 构建参数列表：[func, atom.getRef(), extras...]
        // 判断 func 是 LFunction 还是 LFunctionTco，分别调用
        // atom.setRef(result); return result;
        GLOBALS.define(new LSymbol("swap!"), new LFunction(list -> {
            LAtom atom = (LAtom) list.content.get(1);
            LObject func = list.content.get(2);

            ArrayList<LObject> argsList = new ArrayList<>();
            argsList.add(func);
            argsList.add(atom.getRef());
            for (int i = 3; i < list.content.size(); i++) {
                argsList.add(list.content.get(i));
            }
            LList args = new LList(argsList);

            LObject result;
            if (func instanceof LFunction) {
                result = ((LFunction) func).call(args);
            } else {
                LFunctionTco f = (LFunctionTco) func;
                result = eval(f.getAst(), f.genEnv(args));
            }

            atom.setRef(result);
            return result;
        }));

        // ── 扩展1 — TODO 8: list 函数 ───────────────
        // 接受任意多个参数，返回由这些参数组成的列表
        // 示例：(list 1 2 3) → LList(1, 2, 3)
        // 提示：list.content.subList(1, list.content.size()) 获取所有参数
        GLOBALS.define(new LSymbol("list"), new LFunction(list -> {
            return new LList(new ArrayList<>(list.content.subList(1, list.content.size())));
        }));

        // ── 扩展2 — TODO 9: nth 函数 ────────────────
        // 接受一个 LList 和一个 LInteger（下标），返回列表中对应位置的元素
        // 示例：(nth (list 1 2 3) 1) → LInteger(2)
        // 提示：((LList) list.content.get(1)).content.get(((LInteger)
        // list.content.get(2)).value)
        GLOBALS.define(new LSymbol("nth"), new LFunction(list -> {
            return ((LList) list.content.get(1)).content.get(((LInteger) list.content.get(2)).value);
        }));
    }

    // ── 以下方法全部已实现（与 Project07 相同）─────────

    private static LObject handleLetStar(LList list, Environment env) {
        final LList bindings = (LList) list.content.get(1);
        for (LObject bindingObj : bindings.content) {
            LList binding = (LList) bindingObj;
            env.define((LSymbol) binding.content.get(0), eval(binding.content.get(1), env));
        }
        return list.content.get(2);
    }

    private static LObject handleLet(LList list, Environment env) {
        final LList bindings = (LList) list.content.get(1);
        final Environment newEnv = new Environment(env);
        for (LObject bindingObj : bindings.content) {
            LList binding = (LList) bindingObj;
            newEnv.define((LSymbol) binding.content.get(0), eval(binding.content.get(1), env));
        }
        return eval(list.content.get(2), newEnv);
    }

    private static LObject handleDo(LList list, Environment env) {
        for (int i = 1; i < list.content.size() - 1; i++) {
            eval(list.content.get(i), env);
        }
        return list.content.get(list.content.size() - 1);
    }

    private static LObject handleIF(LList list, Environment env) {
        final LObject condition = eval(list.content.get(1), env);
        if (condition.equals(new LList()) || condition.equals(new LInteger(0))) {
            return list.content.get(3);
        } else {
            return list.content.get(2);
        }
    }

    private static LFunctionTco handleFnStar(LList list, Environment env) {
        final LList params = (LList) list.content.get(1);
        final LObject body = list.content.get(2);
        Environment captured = env.deepClone();
        return new LFunctionTco(params, body, captured);
    }

    private static LObject eval(LObject obj, Environment env) {
        while (true) {
            if (obj instanceof LSymbol)
                return env.lookup((LSymbol) obj);
            else if (obj instanceof LList) {
                LList list = (LList) obj;
                if (list.content.isEmpty()) {
                    return obj;
                } else if (SYMBOL_DEF.equals(list.content.get(0))) {
                    LSymbol name = (LSymbol) list.content.get(1);
                    LFunctionTco res = new LFunctionTco(list.content.get(2), list.content.get(3),
                            new Environment(GLOBALS));
                    env.define(name, res);
                    res.getEnv().define(name, res);
                    return res;
                } else if (SYMBOL_LET.equals(list.content.get(0))) {
                    return handleLet(list, env);
                } else if (SYMBOL_LET_STAR.equals(list.content.get(0))) {
                    obj = handleLetStar(list, env);
                } else if (SYMBOL_DO.equals(list.content.get(0))) {
                    obj = handleDo(list, env);
                } else if (SYMBOL_IF.equals(list.content.get(0))) {
                    obj = handleIF(list, env);
                } else if (SYMBOL_FN_STAR.equals(list.content.get(0))) {
                    return handleFnStar(list, env);
                } else {
                    if (!(eval(list.content.get(0), env) instanceof LFunctionTco)) {
                        Environment finalEnv = env;
                        LList evaluated = new LList(
                                list.content.stream().map(o -> eval(o, finalEnv)).collect(Collectors.toList()));
                        return ((LFunction) evaluated.content.get(0)).call(evaluated);
                    }
                    LFunctionTco f = (LFunctionTco) eval(list.content.get(0), env);
                    Environment finalEnv = env;
                    LList evaluated = new LList(
                            list.content.stream().map(o -> eval(o, finalEnv)).collect(Collectors.toList()));
                    obj = f.getAst();
                    env = f.genEnv(evaluated);
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