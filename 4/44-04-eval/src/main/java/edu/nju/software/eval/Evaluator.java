package edu.nju.software.eval;

import edu.nju.software.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project 04 — 求值器（Evaluator）
 *
 * 将 AST 求值为结果。你需要实现：
 * 1. eval 方法（核心）
 * 2. 内建函数 -、*、/、cons（globals 中）
 * 3. 扩展1：evalToString — 将求值结果转为可读字符串
 * 4. 扩展2：内建函数 car、cdr — 列表头/尾操作
 */
public class Evaluator {
    private static final HashMap<LSymbol, LObject> globals = new HashMap<>();

    static {
        // ── 已提供：加法函数（作为示例）──────────────────────────
        globals.put(new LSymbol("+"), new LFunction(2,
                list -> new LInteger(
                        ((LInteger) list.content.get(1)).value
                                + ((LInteger) list.content.get(2)).value)));

        // TODO 1: 实现减法函数 "-"
        // 接受两个 LInteger，返回第一个减去第二个的结果
        // 提示：参照上面 "+" 的写法，将 + 改为 -
        globals.put(new LSymbol("-"), new LFunction(2,
                list -> new LInteger(
                        ((LInteger) list.content.get(1)).value
                                - ((LInteger) list.content.get(2)).value)));

        // TODO 2: 实现乘法函数 "*"
        globals.put(new LSymbol("*"), new LFunction(2,
                list -> new LInteger(
                        ((LInteger) list.content.get(1)).value
                                * ((LInteger) list.content.get(2)).value)));

        // TODO 3: 实现除法函数 "/" （整数除法）
        globals.put(new LSymbol("/"), new LFunction(2,
                list -> new LInteger(
                        ((LInteger) list.content.get(1)).value
                                / ((LInteger) list.content.get(2)).value)));
        // ── 已提供：nil 空列表常量 ────────────────────────────
        globals.put(new LSymbol("nil"), new LList());

        // TODO 4: 实现 cons 函数
        // 接受两个参数：一个 LObject 和一个 LList
        // 返回一个新列表，内容是将第一个参数接在第二个参数（列表）前面
        // 提示：
        // LObject toAdd = list.content.get(1); // 第一个参数
        // LList oldList = (LList) list.content.get(2); // 第二个参数（列表）
        // 创建新 ArrayList，先 add(toAdd)，再 addAll(oldList.content)
        // return new LList(newList);
        globals.put(new LSymbol("cons"), new LFunction(2, list -> {
            LObject toAdd = list.content.get(1);
            LList oldList = (LList) list.content.get(2);
            ArrayList<LObject> newList = new ArrayList<>();
            newList.add(toAdd);
            newList.addAll(oldList.content);
            return new LList(newList);
        }));

        // ── 扩展2：TODO 5 & 6 ─────────────────────────────
        // TODO 5: 实现 car 函数
        // 接受一个参数（LList），返回列表的第一个元素
        // 示例：(car (cons 1 (cons 2 nil))) → 1
        // 提示：((LList) list.content.get(1)).content.get(0)
        globals.put(new LSymbol("car"), new LFunction(1, list -> {
            LList temp = (LList) list.content.get(1);
            return temp.content.get(0);
        }));
        // TODO 6: 实现 cdr 函数
        // 接受一个参数（LList），返回列表去掉第一个元素后的剩余部分
        // 示例：(cdr (cons 1 (cons 2 nil))) → (2)
        // 提示：用 subList(1, size) 获取子列表，再包装为新 LList
        globals.put(new LSymbol("cdr"), new LFunction(1, list -> {
            LList temp = (LList) list.content.get(1);
            List<LObject> newlist = temp.content.subList(1, temp.content.size());
            return new LList(new ArrayList<>(newlist));
        }));
    }

    /**
     * 对 AST 节点求值。
     *
     * 规则：
     * 1. LInteger / LString → 返回自身（原子）
     * 2. LSymbol → 在 globals 中查找对应的值
     * 3. LList（空）→ 返回自身
     * 4. LList（非空）→ 对每个元素求值，第一个元素作为函数，调用其 call 方法
     *
     * @param obj 待求值的 AST 节点
     * @return 求值结果
     */
    public static LObject eval(LObject obj) {
        // TODO 7: 实现 eval 方法
        // 提示：
        // if (obj instanceof LSymbol) {
        // 在 globals 中查找，找不到则 throw new RuntimeException("Unknown symbol: " + ...)
        // } else if (obj instanceof LList) {
        // LList list = (LList) obj;
        // if (list.content.isEmpty()) return obj;
        // 对 list.content 中每个元素调用 eval，组成新列表 evaluated
        // 将 evaluated 的第一个元素转为 LFunction，调用 call(evaluated)
        // } else {
        // return obj; // LInteger, LString 等原子直接返回
        // }
        if (obj instanceof LSymbol) {
            LObject value = globals.get(obj);
            if (value == null) {
                // 找不到符号时抛出异常，使用 toString() 获取符号名
                throw new RuntimeException("Unknown symbol: " + obj.toString());
            }
            return value;

        } else if (obj instanceof LList) {
            LList list = (LList) obj;
            // 空列表直接返回自身
            if (list.content.isEmpty()) {
                return obj;
            }

            // 核心逻辑：对列表中的所有元素逐个进行 eval 求值
            ArrayList<LObject> evaluatedArgs = new ArrayList<>();
            for (LObject item : list.content) {
                evaluatedArgs.add(eval(item));
            }

            // 重新包装成求值后的新列表
            LList evaluated = new LList(evaluatedArgs);
            LObject first = evaluated.content.get(0);

            // 确保第一个元素是函数才能调用
            if (first instanceof LFunction) {
                return ((LFunction) first).call(evaluated);
            } else {
                // 如果不是函数，必须抛出异常（这行代码能解决 Java 编译缺少 return 的报错）
                throw new RuntimeException("First element is not a function: " + first.toString());
            }

        } else {
            // LInteger, LString 等原子类型，直接返回自身
            return obj;
        }
    }
    /**
     * 扩展1：将求值结果转换为可读字符串。
     *
     * 转换规则：
     * LInteger(42) → "42"
     * LString("hello") → "\"hello\""
     * LSymbol("+") → "+"
     * LList() → "()"
     * LList(LInteger(1), LInteger(2)) → "(1 2)"
     *
     * @param obj 求值结果
     * @return 可读字符串
     */
    public static String evalToString(LObject obj) {
        // TODO 8: 实现 evalToString
        // 提示：
        // 用 instanceof 判断类型
        // 对 LList 递归调用 evalToString 处理每个子元素
        // 元素之间用空格分隔，外层加括号

        if (obj instanceof LList) {
            LList list = (LList) obj;
            if (list.content.isEmpty()) {
                return "()";
            }
            else {

                StringBuilder sb = new StringBuilder("(");
                for (int i = 0; i < list.content.size(); i++) {
                    sb.append(evalToString(list.content.get(i)));
                    if (i < list.content.size() - 1) {
                        sb.append(" ");
                    }
                }
                sb.append(")");
                return sb.toString();
            }
            
        } else if (obj instanceof LInteger) {
            return String.valueOf(((LInteger) obj).value);

        } else if (obj instanceof LString) {
            return "\"" + ((LString) obj).value + "\"";

        } else
            return obj.toString();
    }
}
