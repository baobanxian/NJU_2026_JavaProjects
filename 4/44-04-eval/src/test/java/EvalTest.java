import edu.nju.software.ast.*;
import edu.nju.software.eval.Evaluator;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

/**
 * 明测试用例 — Evaluator
 *
 * 提交前请确保所有明测试通过：mvn test -Dtest=EvalTest
 */
public class EvalTest {
    private LObject evalInput(String input) {
        return Evaluator.eval(Parser.parse(Lexer.lex(new CharLookaheadStream(input))));
    }

    private void testFor(String input, LObject output) {
        Assert.assertEquals(output, evalInput(input));
    }

    // ── eval 基础测试 ──────────────────────────

    @Test
    public void testAtom() {
        testFor("15", new LInteger(15));
    }

    @Test
    public void testEmpty() {
        testFor("()", new LList());
    }

    @Test
    public void testAdd() {
        testFor("(+ 1 2)", new LInteger(3));
    }

    @Test
    public void testSub() {
        testFor("(- 5 3)", new LInteger(2));
    }

    @Test
    public void testMul() {
        testFor("(* 4 3)", new LInteger(12));
    }

    @Test
    public void testDiv() {
        testFor("(/ 7 2)", new LInteger(3));
    }

    @Test
    public void testNil() {
        testFor("nil", new LList());
    }

    @Test
    public void testCons() {
        testFor("(cons 3 nil)", new LList(new LInteger(3)));
    }

    @Test
    public void testNested() {
        testFor("(cons 5 (cons 3 nil))", new LList(new LInteger(5), new LInteger(3)));
    }

    // ── 扩展1：evalToString 测试 ────────────────

    @Test
    public void test_ext1_integer() {
        Assert.assertEquals("42", Evaluator.evalToString(new LInteger(42)));
    }

    @Test
    public void test_ext1_string() {
        Assert.assertEquals("\"hello\"", Evaluator.evalToString(new LString("hello")));
    }

    @Test
    public void test_ext1_empty_list() {
        Assert.assertEquals("()", Evaluator.evalToString(new LList()));
    }

    @Test
    public void test_ext1_simple_list() {
        Assert.assertEquals("(1 2 3)", Evaluator.evalToString(
                new LList(new LInteger(1), new LInteger(2), new LInteger(3))));
    }

    // ── 扩展2：car / cdr 测试 ──────────────────

    @Test
    public void test_ext2_car() {
        testFor("(car (cons 1 (cons 2 nil)))", new LInteger(1));
    }

    @Test
    public void test_ext2_cdr() {
        testFor("(cdr (cons 1 (cons 2 nil)))", new LList(new LInteger(2)));
    }
}
