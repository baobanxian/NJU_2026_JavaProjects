package edu.nju.software.token;

import edu.nju.software.stream.CharLookaheadStream;

import java.util.ArrayList;

import org.apache.commons.text.StringEscapeUtils;

public class Lexer {

    private static boolean isSpecialCharacter(char c) {
        return c == '(' || c == ')' || c == '"' || Character.isWhitespace(c);
    }

    /**
     * 词法分析：将字符流解析为 Token 列表。
     *
     * TODO: YOUR CODE HERE
     *
     * 实现步骤提示：
     * 1. 循环读取字符，跳过空白字符
     * 2. 遇到 '(' → 加入 LeftParen.create()
     * 3. 遇到 ')' → 加入 RightParen.create()
     * 4. 遇到 '"' → 继续读取直到遇到未转义的 '"'，处理 \\ 和 \" 转义，
     * 用 StringEscapeUtils.unescapeJava() 处理转义序列，加入 StringToken.create(...)
     * 5. 遇到数字或负号+数字 → 贪心读取所有数字字符，加入 IntegerToken.create(...)
     * 6. 其他字符 → 贪心读取直到遇到特殊字符，加入 SymbolToken.create(...)
     *
     * 可用工具：
     * - input.eof() 判断流是否结束
     * - input.consume() 读取并消耗下一个字符
     * - input.lookahead(1) 预览下一个字符（不消耗）
     * - Character.isWhitespace(c) 判断空白
     * - Character.isDigit(c) 判断数字
     * - Integer.parseInt(str) 字符串转整数
     * - StringBuilder 逐字符构建字符串
     * - isSpecialCharacter(c) 判断是否为特殊字符（已实现）
     *
     * 注意：需要在 pom.xml 中添加 commons-text 依赖后才能使用 StringEscapeUtils
     */
    public static ArrayList<Token> lex(CharLookaheadStream input) {
        ArrayList<Token> tokens = new ArrayList<>();

        // TODO: YOUR CODE HERE
        while (!input.eof()) {
            char c = input.lookahead(1);
            if (Character.isWhitespace(c)) {
                input.consume();
            } else if (c == '(') {
                input.consume();
                tokens.add(LeftParen.create());
            } else if (c == ')') {
                input.consume();
                tokens.add(RightParen.create());
            } else if (c == '"') {
                input.consume();
                StringBuilder sb = new StringBuilder();
                while (!input.eof()) {
                    char next = input.lookahead(1);
                    if (next == '\\') {
                        sb.append(input.consume());
                        if (!input.eof()) {
                            sb.append(input.consume());
                        }
                    } else if (next == '"') {
                        input.consume();
                        break;
                    } else {
                        sb.append(input.consume());
                    }
                }
                tokens.add(StringToken.create(StringEscapeUtils.unescapeJava(sb.toString())));
            } else if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (!input.eof() && Character.isDigit(input.lookahead(1))) {
                    sb.append(input.consume());
                }
                tokens.add(IntegerToken.create(Integer.parseInt(sb.toString())));
            } else if (c == '-') {
                input.consume();
                if (!input.eof() && Character.isDigit(input.lookahead(1))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append('-');
                    while (!input.eof() && Character.isDigit(input.lookahead(1))) {
                        sb.append(input.consume());
                    }
                    tokens.add(IntegerToken.create(Integer.parseInt(sb.toString())));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append('-');
                    while (!input.eof() && !isSpecialCharacter(input.lookahead(1))) {
                        sb.append(input.consume());
                    }
                    tokens.add(SymbolToken.create(sb.toString()));
                }
            } else {
                StringBuilder sb = new StringBuilder();
                while (!input.eof() && !isSpecialCharacter(input.lookahead(1))) {
                    sb.append(input.consume());
                }
                tokens.add(SymbolToken.create(sb.toString()));
            }

        }
        return tokens;
    }

    /**
     * 扩展 1：统计 Token 列表中各类型的数量。
     * 返回格式：`整数:N 符号:N 字符串:N 左括号:N 右括号:N`
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * - 遍历 tokens，用 instanceof 判断每个 Token 的类型
     * - 维护五个计数器：integers, symbols, strings, leftParens, rightParens
     * - 返回格式严格按照：`整数:N 符号:N 字符串:N 左括号:N 右括号:N`
     */
    public static String countTokens(ArrayList<Token> tokens) {
        // TODO: YOUR CODE HERE
        int integers = 0, symbols = 0, strings = 0, leftParens = 0, rightParens = 0;

        for (Token t : tokens) {
            if (t instanceof IntegerToken)
                integers++;
            else if (t instanceof SymbolToken)
                symbols++;
            else if (t instanceof StringToken)
                strings++;
            else if (t instanceof LeftParen)
                leftParens++;
            else if (t instanceof RightParen)
                rightParens++;
        }

        return String.format("整数:%d 符号:%d 字符串:%d 左括号:%d 右括号:%d",
                integers, symbols, strings, leftParens, rightParens);
    }

    /**
     * 扩展 2：将 Token 列表转换回字符串形式，Token 之间用空格分隔。
     * IntegerToken → 数字字符串；StringToken → "value"；SymbolToken → 符号名；
     * LeftParen → "("；RightParen → ")"
     *
     * 示例：[LeftParen, SymbolToken(+), IntegerToken(1), IntegerToken(2), RightParen]
     * → "( + 1 2 )"
     *
     * TODO: YOUR CODE HERE
     *
     * 提示：
     * - 使用 StringBuilder 拼接结果
     * - Token 之间用单个空格分隔（第一个 token 前无空格）
     * - 访问 IntegerToken 的值：((IntegerToken) t).value
     * - 访问 StringToken 的值：((StringToken) t).value
     * - 访问 SymbolToken 的名字：((SymbolToken) t).name
     */
    public static String tokensToString(ArrayList<Token> tokens) {
        // TODO: YOUR CODE HERE
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sb.append(" "); // Token 之间用单个空格分隔
            }

            Token t = tokens.get(i);
            if (t instanceof LeftParen) {
                sb.append("(");
            } else if (t instanceof RightParen) {
                sb.append(")");
            } else if (t instanceof IntegerToken) {
                sb.append(((IntegerToken) t).value);
            } else if (t instanceof StringToken) {
                sb.append("\"").append(((StringToken) t).value).append("\"");
            } else if (t instanceof SymbolToken) {
                sb.append(((SymbolToken) t).name);
            }
        }

        return sb.toString();
    }
}
