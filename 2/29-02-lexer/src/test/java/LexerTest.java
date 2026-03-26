import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class LexerTest {

    private void testFor(String input, Token[] output) {
        Assert.assertArrayEquals(output, Lexer.lex(new CharLookaheadStream(input)).toArray());
    }

    private ArrayList<Token> lex(String input) {
        return Lexer.lex(new CharLookaheadStream(input));
    }

    // -----------------------------------------------------------------------
    // 基础词法分析测试
    // -----------------------------------------------------------------------

    @Test
    public void testAdd() {
        testFor("(+ 1 2)",
                new Token[]{LeftParen.create(), SymbolToken.create("+"),
                        IntegerToken.create(1), IntegerToken.create(2), RightParen.create()});
    }

    @Test
    public void testNeg() {
        testFor("(- 1 -2)",
                new Token[]{LeftParen.create(), SymbolToken.create("-"),
                        IntegerToken.create(1), IntegerToken.create(-2), RightParen.create()});
    }

    @Test
    public void testString() {
        testFor("(cons \"test\" \"for \\\\\\\"strings\\\"\\\\\")",
                new Token[]{LeftParen.create(), SymbolToken.create("cons"),
                        StringToken.create("test"),
                        StringToken.create("for \\\"strings\"\\"), RightParen.create()});
    }

    @Test
    public void testNested() {
        testFor("(print (append (list 1 2) (list 3 4)))",
                new Token[]{LeftParen.create(), SymbolToken.create("print"), LeftParen.create(),
                        SymbolToken.create("append"), LeftParen.create(), SymbolToken.create("list"),
                        IntegerToken.create(1), IntegerToken.create(2), RightParen.create(),
                        LeftParen.create(), SymbolToken.create("list"),
                        IntegerToken.create(3), IntegerToken.create(4), RightParen.create(),
                        RightParen.create(), RightParen.create()});
    }

    // -----------------------------------------------------------------------
    // 扩展 1：countTokens 测试
    // -----------------------------------------------------------------------

    @Test
    public void test_ext1_count_basic() {
        // (+ 1 2)：整数2 符号1 字符串0 左括号1 右括号1
        ArrayList<Token> tokens = lex("(+ 1 2)");
        Assert.assertEquals("整数:2 符号:1 字符串:0 左括号:1 右括号:1",
                Lexer.countTokens(tokens));
    }

    @Test
    public void test_ext1_count_empty() {
        // 空 token 列表
        ArrayList<Token> tokens = new ArrayList<>();
        Assert.assertEquals("整数:0 符号:0 字符串:0 左括号:0 右括号:0",
                Lexer.countTokens(tokens));
    }

    @Test
    public void test_ext1_count_with_string() {
        // (cons "hello" 42)：整数1 符号1 字符串1 左括号1 右括号1
        ArrayList<Token> tokens = lex("(cons \"hello\" 42)");
        Assert.assertEquals("整数:1 符号:1 字符串:1 左括号:1 右括号:1",
                Lexer.countTokens(tokens));
    }

    @Test
    public void test_ext1_count_only_symbol() {
        // 单个符号
        ArrayList<Token> tokens = lex("nil");
        Assert.assertEquals("整数:0 符号:1 字符串:0 左括号:0 右括号:0",
                Lexer.countTokens(tokens));
    }

    // -----------------------------------------------------------------------
    // 扩展 2：tokensToString 测试
    // -----------------------------------------------------------------------

    @Test
    public void test_ext2_toString_simple() {
        // (+ 1 2) → "( + 1 2 )"
        ArrayList<Token> tokens = lex("(+ 1 2)");
        Assert.assertEquals("( + 1 2 )", Lexer.tokensToString(tokens));
    }

    @Test
    public void test_ext2_toString_symbol_only() {
        // nil → "nil"
        ArrayList<Token> tokens = lex("nil");
        Assert.assertEquals("nil", Lexer.tokensToString(tokens));
    }

    @Test
    public void test_ext2_toString_with_neg() {
        // (- 1 -2) → "( - 1 -2 )"
        ArrayList<Token> tokens = lex("(- 1 -2)");
        Assert.assertEquals("( - 1 -2 )", Lexer.tokensToString(tokens));
    }

    @Test
    public void test_ext2_toString_with_string() {
        // (cons "hi" 0) → "( cons "hi" 0 )"
        ArrayList<Token> tokens = lex("(cons \"hi\" 0)");
        Assert.assertEquals("( cons \"hi\" 0 )", Lexer.tokensToString(tokens));
    }

    @Test
    public void test_ext2_toString_empty() {
        // 空列表 → ""
        ArrayList<Token> tokens = new ArrayList<>();
        Assert.assertEquals("", Lexer.tokensToString(tokens));
    }
}
