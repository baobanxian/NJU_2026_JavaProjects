import edu.nju.software.ast.*;
import edu.nju.software.eval.Evaluator;
import edu.nju.software.stream.CharLookaheadStream;
import edu.nju.software.token.Lexer;
import org.junit.Assert;
import org.junit.Test;

public class ControlTest {
    private void testFor(String input, LObject output) {
        Assert.assertEquals(output, Evaluator.eval(Parser.parse(Lexer.lex(new CharLookaheadStream(input)))));
    }

    // ── do 测试 ─────────────────────────────

    @Test
    public void testDo() {
        testFor("(do (* 1 2) (- 1 2) (+ 1 2))", new LInteger(3));
    }

    @Test
    public void testDoSingle() {
        testFor("(do 42)", new LInteger(42));
    }

    // ── if 测试 ─────────────────────────────

    @Test
    public void testIf_zero() {
        testFor("(if 0 1 (+ 1 -1))", new LInteger(0));
    }

    @Test
    public void testIf_true() {
        testFor("(if 1 5 10)", new LInteger(5));
    }

    @Test
    public void testIf_nil() {
        testFor("(if nil 10 20)", new LInteger(20));
    }

    // ── fn* 测试 ────────────────────────────

    @Test
    public void testFn_identity() {
        testFor("((fn* (a) a) 123)", new LInteger(123));
    }

    @Test
    public void testFn_add() {
        testFor("((fn* (a b) (+ a b)) 3 4)", new LInteger(7));
    }

    @Test
    public void testFn_closure() {
        testFor("(let* ((x 10) (f (fn* (y) (+ x y)))) (f 5))", new LInteger(15));
    }

    // ── 综合 ────────────────────────────────

    @Test
    public void testComplex() {
        testFor("(do 2 3 ((fn* (a b c) (* (+ a b) c)) (if (do 0) 1 2) 3 4))", new LInteger(20));
    }

    @Test
    public void testEnvClosure() {
        testFor("(let* ((f (fn* (b) b)) (b 0)) (f 0))", new LInteger(0));
    }

    // ── let/let* 回归 ──────────────────────

    @Test
    public void testLet() {
        testFor("(let ((x 1)) x)", new LInteger(1));
    }

    @Test
    public void testLetStar() {
        testFor("(let* ((x (+ 1 2)) (y (* 2 x))) y)", new LInteger(6));
    }

    // ── 扩展1：cond ────────────────────────

    @Test
    public void test_ext1_cond_first_true() {
        testFor("(cond (1 10) (1 20) (else 30))", new LInteger(10));
    }

    @Test
    public void test_ext1_cond_else() {
        testFor("(cond (0 10) (0 20) (else 30))", new LInteger(30));
    }

    @Test
    public void test_ext1_cond_no_match() {
        testFor("(cond (0 10) (0 20))", new LList());
    }

    @Test
    public void test_ext1_cond_middle() {
        testFor("(cond (0 10) (1 20) (else 30))", new LInteger(20));
    }

    // ── 扩展2：when ────────────────────────

    @Test
    public void test_ext2_when_true() {
        testFor("(when 1 (+ 1 2) (* 3 4))", new LInteger(12));
    }

    @Test
    public void test_ext2_when_false() {
        testFor("(when 0 (+ 1 2) (* 3 4))", new LList());
    }
}