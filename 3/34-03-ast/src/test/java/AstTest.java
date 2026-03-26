import edu.nju.software.ast.*;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class AstTest {

    private LObject parse(String input) {
        return Parser.parse(Lexer.lex(new CharLookaheadStream(input)));
    }

    private void testFor(String input, LObject output) {
        Assert.assertEquals(output.toString(), parse(input).toString());
    }

    // -----------------------------------------------------------------------
    // 基础解析测试
    // -----------------------------------------------------------------------

    @Test
    public void testAtom() {
        testFor("   114514   ", new LInteger(114514));
    }

    @Test
    public void testAdd() {
        ArrayList<LObject> lst = new ArrayList<>(Arrays.asList(new LSymbol("+"), new LInteger(1), new LInteger(2)));
        testFor("(+ 1 2)", new LList(lst));
    }

    @Test
    public void testNeg() {
        ArrayList<LObject> lst = new ArrayList<>(Arrays.asList(new LSymbol("-"), new LInteger(1), new LInteger(-2)));
        testFor("(- 1 -2)", new LList(lst));
    }

    // -----------------------------------------------------------------------
    // 扩展 1：astToString 测试
    // -----------------------------------------------------------------------

    @Test
    public void test_ext1_integer() {
        Assert.assertEquals("42", Parser.astToString(new LInteger(42)));
    }

    @Test
    public void test_ext1_neg_integer() {
        Assert.assertEquals("-7", Parser.astToString(new LInteger(-7)));
    }

    @Test
    public void test_ext1_symbol() {
        Assert.assertEquals("nil", Parser.astToString(new LSymbol("nil")));
    }

    @Test
    public void test_ext1_string() {
        Assert.assertEquals("\"hello\"", Parser.astToString(new LString("hello")));
    }

    @Test
    public void test_ext1_empty_list() {
        Assert.assertEquals("()", Parser.astToString(new LList()));
    }

    @Test
    public void test_ext1_simple_list() {
        Assert.assertEquals("(+ 1 2)", Parser.astToString(parse("(+ 1 2)")));
    }

    // -----------------------------------------------------------------------
    // 扩展 2：countAtoms 测试
    // -----------------------------------------------------------------------

    @Test
    public void test_ext2_integer() {
        Assert.assertEquals(1, Parser.countAtoms(new LInteger(99)));
    }

    @Test
    public void test_ext2_symbol() {
        Assert.assertEquals(1, Parser.countAtoms(new LSymbol("+")));
    }

    @Test
    public void test_ext2_string() {
        Assert.assertEquals(1, Parser.countAtoms(new LString("hi")));
    }

    @Test
    public void test_ext2_empty_list() {
        Assert.assertEquals(0, Parser.countAtoms(new LList()));
    }

    @Test
    public void test_ext2_simple_list() {
        // (+ 1 2) → symbols: "+", integers: 1, 2 → 3 atoms
        Assert.assertEquals(3, Parser.countAtoms(parse("(+ 1 2)")));
    }
}
