package edu.nju.software.ast;

import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.stream.TokenReadStream;
import edu.nju.software.token.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StrBuilder;

/**
 * 语法分析器：将 Token 列表解析为抽象语法树（AST）。
 *
 * AST 节点层次：
 * LObject（抽象基类）
 * ├── LInteger — 整数字面量
 * ├── LString — 字符串字面量
 * ├── LSymbol — 符号（变量名、运算符等）
 * └── LList — 列表/S-表达式
 *
 * 解析规则：
 * - LeftParen → 开始一个列表，递归解析子元素，直到 RightParen
 * - IntegerToken → LInteger
 * - StringToken → LString
 * - SymbolToken → LSymbol
 */
public class Parser {

    /**
     * 将 Token 列表解析为一个 AST 节点。
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：创建 TokenReadStream，调用 parseForm
     */
    public static LObject parse(ArrayList<Token> tokens) {
        TokenReadStream rdr = new TokenReadStream(tokens);
        return parseForm(rdr);
    }

    /**
     * 根据下一个 Token 决定解析为列表还是原子。
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * - rdr.peek() 预览下一个 Token
     * - 若为 LeftParen → 调用 parseList(rdr)
     * - 否则 → 调用 parseAtom(rdr)
     */
    static LObject parseForm(TokenReadStream rdr) {
        Token next = rdr.peek();
        if (next instanceof LeftParen) {
            return parseList(rdr);
        }
        return parseAtom(rdr);
    }

    /**
     * 解析一个列表（S-表达式）。
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * 1. 调用 rdr.next() 消耗左括号 '('
     * 2. 循环：调用 rdr.peek()，若不是 RightParen 则调用 parseForm 加入列表
     * 3. 调用 rdr.next() 消耗右括号 ')'
     * 4. 返回 new LList(lst)
     */
    static LObject parseList(TokenReadStream rdr) {
        rdr.next();
        ArrayList<LObject> lst = new ArrayList<>();
        while (rdr.peek() != null && !(rdr.peek() instanceof RightParen)) {
            lst.add(parseForm(rdr));
        }
        rdr.next();
        return new LList(lst);
    }

    /**
     * 解析一个原子（非列表节点）。
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * - 调用 rdr.next() 取出一个 Token
     * - 用 instanceof 判断类型，分别创建 LString / LInteger / LSymbol
     * - 访问字段：((IntegerToken) t).value、((StringToken) t).value、((SymbolToken)
     * t).name
     */
    static LObject parseAtom(TokenReadStream rdr) {
        Token temp = rdr.next();
        if (temp instanceof IntegerToken) {
            return new LInteger(((IntegerToken) temp).value);
        } else if (temp instanceof StringToken) {
            return new LString(((StringToken) temp).value);
        } else if (temp instanceof SymbolToken) {
            return new LSymbol(((SymbolToken) temp).name);
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // 扩展 1：将 AST 转换回 Lisp 字符串表示
    // -----------------------------------------------------------------------

    /**
     * 将 AST 节点转换回 Lisp 表达式字符串。
     *
     * 转换规则：
     *   LInteger(n)   → "n"（如 "42"、"-1"）
     *   LString(s)    → "\"s\""（含双引号，如 "\"hello\""）
     *   LSymbol(name) → name（如 "+"）
     *   LList([])     → "()"
     *   LList([...])  → "(el1 el2 ...)"，元素间用空格分隔
     *
     * 示例：
     *   astToString(parse(lex("(+ 1 2)")))    → "(+ 1 2)"
     *   astToString(new LString("hi"))         → "\"hi\""
     *   astToString(new LList())               → "()"
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     *   - 用 instanceof 判断类型
     *   - 访问 LInteger 的值：((LInteger) ast).value
     *   - 访问 LString 的值：((LString) ast).value
     *   - 访问 LSymbol 的名称：((LSymbol) ast).name
     *   - 访问 LList 的子节点列表：((LList) ast).getContent()
     *   - 
     */
    public static String astToString(LObject ast) {
        if (ast instanceof LInteger) {
            return String.valueOf(((LInteger) ast).value);
        } else if (ast instanceof LString) {
            return "\"" + ((LString) ast).value + "\"";
        } else if (ast instanceof LSymbol) {
            return ((LSymbol) ast).name;
        } else if (ast instanceof LList) {
            List<LObject> content = ((LList) ast).getContent();
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < content.size(); i++) {
                sb.append(astToString(content.get(i)));
                if (i < content.size() - 1) {
                    sb.append(" ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
        return "";
    }

    // -----------------------------------------------------------------------
    // 扩展 2：统计 AST 中原子节点的数量
    // -----------------------------------------------------------------------

    /**
     * 递归统计 AST 中原子（非列表）节点的总数。
     *
     * 规则：
     * LInteger / LString / LSymbol → 计 1
     * LList([]) → 计 0
     * LList([...]) → 各子节点 countAtoms 之和（递归）
     *
     * 示例：
     * countAtoms(parse(lex("(+ 1 2)"))) → 3 ("+", 1, 2)
     * countAtoms(parse(lex("(+ (- 1 2) 3)"))) → 5 ("+", "-", 1, 2, 3)
     * countAtoms(new LList()) → 0
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * - 若 ast 是 LList，遍历其 getContent() 并累加 countAtoms(child)
     * - 否则（原子）返回 1
     */
    public static int countAtoms(LObject ast) {
        if (ast instanceof LList) {
            int sum = 0;
            for (LObject child : ((LList) ast).getContent()) {
                sum += countAtoms(child);
            }
            return sum;
        } else {
            return 1;
        }
    }
}
