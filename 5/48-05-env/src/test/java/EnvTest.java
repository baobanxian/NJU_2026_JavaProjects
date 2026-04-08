import edu.nju.software.ast.*;
import edu.nju.software.eval.Evaluator;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

/**
 * 明测试用例 — Evaluator (Environment)
 */
public class EnvTest {
    private void testFor(String input, LObject output) {
        Assert.assertEquals(output, Evaluator.eval(Parser.parse(Lexer.lex(new CharLookaheadStream(input)))));
    }

    // ── let 基础 ───────────────────────────────

    @Test
    public void testLet() {
        testFor("(let ((x 1)) x)", new LInteger(1));
    }

    @Test
    public void testCalculated() {
        testFor("(let ((x (+ 1 2))) x)", new LInteger(3));
    }

    @Test
    public void testLetMultiBindings() {
        testFor("(let ((x 2) (y 3)) (* x y))", new LInteger(6));
    }

    // ── let* 基础 ──────────────────────────────

    @Test
    public void testLetStar() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) y)", new LInteger(6));
    }

    @Test
    public void testCalculation() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) (- y x))", new LInteger(3));
    }

    // ── 嵌套环境 ───────────────────────────────

    @Test
    public void testNested() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) (let ((x 10) (z (* x y))) z))", new LInteger(18));
    }

    @Test
    public void testList() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) (let ((z (cons y (cons x nil)))) z))",
                new LList(new LInteger(6), new LInteger(3)));
    }

    @Test
    public void testShadowing() {
        testFor("(let ((x 1)) (let ((x 2)) x))", new LInteger(2));
    }

    // ── 扩展1：if 条件求值 ─────────────────────

    @Test
    public void test_ext1_if_true() {
        testFor("(if (cons 1 nil) 10 20)", new LInteger(10));
    }

    @Test
    public void test_ext1_if_false() {
        testFor("(if nil 10 20)", new LInteger(20));
    }

    @Test
    public void test_ext1_if_empty_list() {
        testFor("(if () 10 20)", new LInteger(20));
    }

    @Test
    public void test_ext1_if_integer() {
        testFor("(if 1 10 20)", new LInteger(10));
    }

    @Test
    public void test_ext1_if_with_let() {
        testFor("(let ((x 5)) (if x (+ x 1) 0))", new LInteger(6));
    }

    // ── 扩展2：begin 顺序求值 ──────────────────

    @Test
    public void test_ext2_begin_simple() {
        testFor("(begin 1 2 3)", new LInteger(3));
    }

    @Test
    public void test_ext2_begin_exprs() {
        testFor("(begin (+ 1 2) (* 3 4))", new LInteger(12));
    }

    @Test
    public void test_ext2_begin_single() {
        testFor("(begin 42)", new LInteger(42));
    }
}
